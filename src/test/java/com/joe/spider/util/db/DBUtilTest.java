package com.joe.spider.util.db;

import com.joe.utils.common.BeanUtils;
import com.joe.utils.common.StringUtils;
import lombok.NoArgsConstructor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.PropertyException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 数据库工具测试
 * <p>
 * 说明：需要给定的url中存在一个表叫history，有{@link History History}中的字段
 *
 * @author joe
 * @version 2018.06.07 18:30
 */
public class DBUtilTest {
    /**
     * 数据库连接URL
     */
    static String url = "jdbc:mysql://dbserver:9999/movie?characterEncoding=utf-8&allowMultiQueries=true";
    /**
     * 数据库用户名
     */
    static String username = "root";
    /**
     * 数据库密码
     */
    static String password = "Qwer1@34";

    @Test
    public void dbTest() throws Exception {
        MybatisConfig config = new MybatisConfig(DBUtil.buildDatasource(url, username, password), String.valueOf
                (System.currentTimeMillis()), "com.joe.spider");
        config.addInterceptor(new Page2Plugin());
        config.addInterceptor(new Page1Plugin());

        SqlSessionFactory sqlSessionFactory = DBUtil.build(config);


        Dao dao = sqlSessionFactory.openSession().getMapper(Dao.class);

        List<History> histories = dao.selectAllHistory(true);
        System.out.println(histories.size());
        System.out.println(histories.getClass());
//        Assert.assertNotEquals(0, histories.size());
    }
}


@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler
        .class})})
@NoArgsConstructor
class Page2Plugin implements Interceptor {
    /**
     * 数据库方言
     */
    private String dialect = "";    //数据库方言
    /**
     * 需要拦截的分页sql
     */
    private String pageSqlId = ""; //mapper.xml中需要拦截的ID(正则匹配)

    /**
     * 构造器
     *
     * @param dialect   数据库方言
     * @param pageSqlId 需要拦截的分页sql
     */
    public Page2Plugin(String dialect, String pageSqlId) {
        this.dialect = dialect;
        this.pageSqlId = pageSqlId;
    }


    @Override
    public Object intercept(Invocation ivk) throws Throwable {

        StatementHandler handler = (StatementHandler) ivk.getTarget();
        PreparedStatement statement = (PreparedStatement) ivk.getArgs()[0];

        BoundSql boundSql = handler.getBoundSql();

        BeanUtils.setProperty(boundSql, "sql", "123456789/******/*/*");

        Object obj = ivk.proceed();
        System.out.println("********" + obj.getClass());
        System.out.println("********" + obj);
        return new LinkedList<>();
    }


    public Object plugin(Object arg0) {
        // TODO Auto-generated method stub
        return Plugin.wrap(arg0, this);
    }


    @Override
    public void setProperties(Properties p) {
        dialect = p.getProperty("dialect");
        if (StringUtils.isEmpty(dialect)) {
            try {
                throw new PropertyException("dialect property is not found!");
            } catch (PropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        pageSqlId = p.getProperty("pageSqlId");
        if (StringUtils.isEmpty(pageSqlId)) {
            try {
                throw new PropertyException("pageSqlId property is not found!");
            } catch (PropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}


@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler
        .class})})
@NoArgsConstructor
class Page1Plugin implements Interceptor {
    /**
     * 数据库方言
     */
    private String dialect = "";    //数据库方言
    /**
     * 需要拦截的分页sql
     */
    private String pageSqlId = ""; //mapper.xml中需要拦截的ID(正则匹配)

    /**
     * 构造器
     *
     * @param dialect   数据库方言
     * @param pageSqlId 需要拦截的分页sql
     */
    public Page1Plugin(String dialect, String pageSqlId) {
        this.dialect = dialect;
        this.pageSqlId = pageSqlId;
    }


    @Override
    public Object intercept(Invocation ivk) throws Throwable {
//        StatementHandler handler = (StatementHandler) ivk.getTarget();
//        PreparedStatement statement = (PreparedStatement) ivk.getArgs()[0];
//
//        System.out.println(statement.getClass());
//
//        ParameterHandler parameterHandler = handler.getParameterHandler();
//        parameterHandler.setParameters(statement);
//
//        Connection connection = statement.getConnection();
//
//        System.out.println("param是" + handler.getParameterHandler().getParameterObject().getClass());
//
//
//        BoundSql boundSql = handler.getBoundSql();
//        System.out.println(boundSql.getSql());
//        PreparedStatement preparedStatement = connection.prepareStatement("select count(0) from ( " + boundSql.getSql
//                () + " ) count_table");
//        parameterHandler.setParameters(preparedStatement);
//        ResultSet resultSet = preparedStatement.executeQuery();
//        while (resultSet.next()) {
//            System.out.println("数量是：" + resultSet.getLong(1));
//        }


        Object obj = ivk.proceed();
        System.out.println("-----" + obj.getClass());
        System.out.println("-----" + obj);
        return new CopyOnWriteArrayList<>();
    }


    public Object plugin(Object arg0) {
        // TODO Auto-generated method stub
        return Plugin.wrap(arg0, this);
    }


    @Override
    public void setProperties(Properties p) {
        dialect = p.getProperty("dialect");
        if (StringUtils.isEmpty(dialect)) {
            try {
                throw new PropertyException("dialect property is not found!");
            } catch (PropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        pageSqlId = p.getProperty("pageSqlId");
        if (StringUtils.isEmpty(pageSqlId)) {
            try {
                throw new PropertyException("pageSqlId property is not found!");
            } catch (PropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}