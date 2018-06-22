package com.joe.spider.util.db.plugin;

import com.joe.spider.util.db.exception.NoSupportedException;
import com.joe.spider.util.db.sql.CountSql;
import com.joe.spider.util.db.sql.PageSql;
import com.joe.spider.util.db.sql.dialect.Mysql;
import com.joe.utils.common.BeanUtils;
import com.joe.utils.common.StringUtils;
import com.joe.utils.data.PageData;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 分页插件必须放在第一个插件
 *
 * @author joe
 * @version 2018.06.22 17:09
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler
        .class})})
@Slf4j
@NoArgsConstructor
public class PagePlugin implements Interceptor {
    /**
     * 需要拦截的分页sql id（在mapper中定义的sql的id），使用正则匹配
     */
    private String pageSqlId;
    /**
     * 数据库方言
     */
    private String dialect;
    /**
     * 分页sql
     */
    private PageSql pageSql;
    /**
     * 统计sql
     */
    private CountSql countSql;


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        log.debug("分页插件开始");
        //StatementHandler
        BaseStatementHandler handler = getBaseStatementHandler((StatementHandler) invocation.getTarget());
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
                List<?> list = map.values().parallelStream().filter(data -> (data != null && data.getClass().equals
                        (PageData.class))).limit(1).collect(Collectors.toList());
                if (list.isEmpty()) {
                    throw new IllegalArgumentException("参数中没有PageData");
                }
                pageData = (PageData) list.get(0);
            } else if (parameterObject instanceof PageData) {

                pageData = (PageData) parameterObject;
            } else {
                throw new NoSupportedException("不支持参数类型：" + parameterObject.getClass());
            }

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
        }
        return null;
    }

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
        Object dialectObj = properties.get("dialect");
        dialect = String.valueOf(dialectObj == null ? "mysql" : dialectObj);
        pageSqlId = String.valueOf(properties.get("pageSqlId"));

        log.info("分页插件方言为[{}]，分页sql为[{}]", dialect, pageSqlId);

        if (StringUtils.isEmpty(pageSqlId)) {
            throw new NullPointerException("拦截sql不能为空");
        }

        if ("mysql".equals(dialect)) {
            pageSql = new Mysql();
            countSql = new Mysql();
        } else {
            log.error("分页插件不支持方言[{}]", dialect);
            throw new NoSupportedException("分页插件不支持方言[" + dialect + "]");
        }
    }
}
