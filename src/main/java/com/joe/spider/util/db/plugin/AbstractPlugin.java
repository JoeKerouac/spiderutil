package com.joe.spider.util.db.plugin;

import com.joe.spider.util.db.exception.NoSupportedException;
import com.joe.spider.util.db.sql.CountSql;
import com.joe.spider.util.db.sql.PageSql;
import com.joe.spider.util.db.sql.dialect.Mysql;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.data.PageData;
import com.joe.utils.reflect.BeanUtils;
import com.joe.utils.reflect.clazz.ClassUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author JoeKerouac
 * @version 2019年07月03日 15:04
 */
@Slf4j
@NoArgsConstructor
public class AbstractPlugin implements Interceptor {
    /**
     * 需要拦截的分页sql id（在mapper中定义的sql的id），使用正则匹配
     */
    private String   pageSqlId;
    /**
     * 分页sql
     */
    private PageSql pageSql;
    /**
     * 统计sql
     */
    private CountSql countSql;

    /**
     * 分页插件构造器
     *
     * @param pageSqlId 需要拦截的分页sql id（在mapper中定义的sql的id），使用正则匹配
     * @param pageSql   分页sql方言实现
     * @param countSql  统计sql方言实现
     */
    public AbstractPlugin(String pageSqlId, PageSql pageSql, CountSql countSql) {
        this.pageSqlId = pageSqlId;
        this.pageSql = pageSql;
        this.countSql = countSql;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        log.debug("分页插件开始");
        //StatementHandler
        BaseStatementHandler handler = getBaseStatementHandler(
                (StatementHandler) invocation.getTarget());
        //MappedStatement
        MappedStatement mappedStatement = getMappedStatement(handler);

        //sql-id，定义在mapper中的slq语句的id
        String sqlId = mappedStatement.getId();

        if (sqlId.matches(pageSqlId)) {
            //参数处理器
            ParameterHandler parameterHandler = handler.getParameterHandler();
            //BoundSql
            BoundSql boundSql = handler.getBoundSql();
            //要分页的sql
            String sql = boundSql.getSql();
            //PreparedStatement
            Statement statement = (Statement) invocation.getArgs()[0];
            //参数
            Object parameterObject = parameterHandler.getParameterObject();
            //连接
            Connection connection = statement.getConnection();
            //结果集处理器
            ResultSetHandler resultSetHandler = BeanUtils.getProperty(handler, "resultSetHandler");

            log.debug("sql-id [{}] 对应的sql [{}] 需要分页", sqlId, sql);
            PageData<?> pageData;
            if (parameterObject instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) parameterObject;
                List<?> list = map.values().parallelStream()
                        .filter(data -> (data != null && data.getClass().equals(PageData.class)))
                        .limit(1).collect(Collectors.toList());
                if (list.isEmpty()) {
                    throw new IllegalArgumentException("参数中没有PageData，需要包含PageData");
                }
                pageData = (PageData) list.get(0);
            } else if (parameterObject instanceof PageData) {
                pageData = (PageData) parameterObject;
            } else {
                throw new NoSupportedException("不支持参数类型：" + parameterObject.getClass());
            }

            int limit = pageData.getLimit();
            log.debug("准备生成分页sql和统计sql");
            String pageSqlStr = pageSql.page(sql, pageData.getCurrentPage(), pageData.getLimit());
            String countSqlStr = countSql.count(sql);

            log.debug("生成的分页sql为[{}]，统计sql为[{}]", pageData, countSqlStr);

            PreparedStatement pageStatement = connection.prepareStatement(pageSqlStr);
            PreparedStatement countStatement = connection.prepareStatement(countSqlStr);

            log.debug("为分页sql和统计sql设置参数");
            parameterHandler.setParameters(pageStatement);
            parameterHandler.setParameters(countStatement);

            log.debug("准备统计数量");
            ResultSet resultSet = countStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            log.debug("统计数量为：{}", count);

            log.debug("准备执行分页查询");
            pageStatement.execute();
            List<?> result = resultSetHandler.handleResultSets(pageStatement);
            log.debug("分页查询执行结果为：{}", result);

            int pageCount = count % limit == 0 ? (count / limit) : (count / limit + 1);
            BeanUtils.setProperty(pageData, "datas", result);
            pageData.setTotal(count);
            pageData.setTotalPage(pageCount);
            pageData.setHasNext(pageCount > pageData.getCurrentPage());
            log.debug("分页结果为：[{}]", pageData);
            return result;
        }
        return invocation.proceed();
    }

    /**
     * 从StatementHandler中获取BaseStatementHandler（主要为了从中获取ParameterHandler，用于后续处理sql的参数）
     *
     * @param statementHandler StatementHandler
     * @return BaseStatementHandler
     */
    private BaseStatementHandler getBaseStatementHandler(StatementHandler statementHandler) {
        if (statementHandler instanceof BaseStatementHandler) {
            return (BaseStatementHandler) statementHandler;
        } else if (statementHandler instanceof RoutingStatementHandler) {
            return BeanUtils.getProperty(statementHandler, "delegate");
        } else {
            throw new NoSupportedException("不支持的StatementHandler类型：" + statementHandler.getClass());
        }
    }

    /**
     * 获取StatementHandler中的MappedStatement
     *
     * @param handler StatementHandler
     * @return StatementHandler中的MappedStatement
     */
    private MappedStatement getMappedStatement(StatementHandler handler) {
        MappedStatement mappedStatement;
        if (handler instanceof BaseStatementHandler) {
            mappedStatement = BeanUtils.getProperty(handler, "mappedStatement");
        } else if (handler instanceof RoutingStatementHandler) {
            return getMappedStatement(BeanUtils.getProperty(handler, "delegate"));
        } else {
            throw new NoSupportedException("不支持的StatementHandler类型：" + handler.getClass());
        }
        return mappedStatement;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String pageSqlStr = String.valueOf(properties.get("pageSql"));
        String countSqlStr = String.valueOf(properties.get("countSql"));
        pageSqlId = String.valueOf(properties.get("pageSqlId"));

        log.info("分页插件分页sql实现为[{}]，统计sql实现为[{}]，分页sql的id正则为[{}]", pageSqlStr, countSqlStr,
                pageSqlId);

        if (StringUtils.isEmpty(pageSqlId)) {
            throw new NullPointerException("拦截sql不能为空");
        }

        if ("mysql".equals(pageSqlStr) || "null".equals(pageSqlStr)) {
            pageSqlStr = Mysql.class.getName();
        }

        if ("mysql".equals(countSqlStr) || "null".equals(countSqlStr)) {
            countSqlStr = Mysql.class.getName();
        }

        try {
            if (pageSql == null) {
                pageSql = (PageSql) ClassUtils.loadClass(pageSqlStr).newInstance();
            }
            if (countSql == null) {
                countSql = (CountSql) ClassUtils.loadClass(countSqlStr).newInstance();
            }
        } catch (Exception e) {
            throw new NoSupportedException("不支持的方言实现：" + pageSqlStr + " 或 " + countSqlStr, e);
        }
    }
}
