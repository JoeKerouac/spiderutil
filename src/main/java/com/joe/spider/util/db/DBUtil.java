package com.joe.spider.util.db;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.joe.utils.scan.ClassScanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库工具
 *
 * @author joe
 * @version 2017.12.24 16:27
 */
@Slf4j
public class DBUtil {
    /**
     * 通过数据库配置构建SqlSessionFactory
     *
     * @param url      数据库url（完整的URL，例如jdbc:mysql://example.com/test?characterEncoding=utf8）
     * @param username 数据库用户名
     * @param password 数据库密码
     * @param id       SqlSessionFactory的ID
     * @param packages 要扫描的包的集合（会自动将带扫描的包集合下面带{@link com.joe.spider.util.db.Mapper}注解的类，将该类注册为mapper
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory build(String url, String username, String password, String id, String... packages) {
        DruidProp druidProp = new DruidProp();
        druidProp.setUrl(url);
        druidProp.setUsername(username);
        druidProp.setPassword(password);
        return build(id, buildDatasource(druidProp), packages);
    }


    /**
     * 通过数据源构建SqlSessionFactory
     *
     * @param id         SqlSessionFactory的ID
     * @param dataSource 数据源
     * @param packages   要扫描的包的集合（会自动将带扫描的包集合下面带{@link com.joe.spider.util.db.Mapper}注解的类，将该类注册为mapper
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory build(String id, DataSource dataSource, String... packages) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment(id, transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        //说明：必须先扫描ResultMap，然后扫描Mapper，不然如果Mapper中使用ResultMap将会报错
        //扫描ResultMap
        scanResutlMap(configuration, packages);
        //扫描mapper
        scanMapper(configuration, packages);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        return sqlSessionFactory;
    }

    /**
     * 扫描指定包下的mapper
     *
     * @param configuration configuration
     * @param packages      包集合
     */
    private static void scanMapper(Configuration configuration, String... packages) {
        ClassScanner scanner = ClassScanner.getInstance();
        log.debug("开始扫描包{}下的Mapper列表", packages);
        List<Class<?>> mappers = scanner.scan(Collections.singletonList(clazz -> {
            boolean flag = clazz.getAnnotation(Mapper.class) == null;
            if (!flag) {
                log.debug("Class [{}] 将被加入Mapper列表", clazz);
            }
            return flag;
        }), packages);
        log.debug("扫描到的Mapper列表为：{}", mappers);
        mappers.forEach(configuration::addMapper);
    }

    /**
     * 扫描指定包下的ResultMap
     *
     * @param configuration configuration
     * @param packages      包集合
     */
    private static void scanResutlMap(Configuration configuration, String... packages) {
        ClassScanner scanner = ClassScanner.getInstance();
        log.debug("开始扫描包{}下的ResultMap列表", packages);
        List<Class<?>> resultMaps = scanner.scan(Collections.singletonList(clazz -> {
            boolean flag = clazz.getAnnotation(ResultMapDefine.class) == null;
            if (!flag) {
                log.debug("Class [{}] 将被加入ResultMap列表", clazz);
            }
            return flag;
        }), packages);
        log.debug("扫描到的ResultMap列表为：{}", resultMaps);
        resultMaps.parallelStream().forEach(clazz -> {
            ResultMap resultMap = ResultMapHelper.build(clazz, configuration);
            if (resultMap != null) {
                configuration.addResultMap(resultMap);
            }
        });
    }

    /**
     * 通过配置构建数据源
     *
     * @param druidProp 数据源配置
     */
    public static DataSource buildDatasource(DruidProp druidProp) {
        log.info("初始化数据源..................................");
        DruidDataSource dataSource = new DruidDataSource();

        log.info("数据源URL：" + druidProp.getUrl());
        dataSource.setUrl(druidProp.getUrl());
        log.info("数据源用户名：" + druidProp.getUsername());
        dataSource.setUsername(druidProp.getUsername());
        dataSource.setPassword(druidProp.getPassword());
        log.info("数据源最大活动连接数：" + druidProp.getMaxActive());
        dataSource.setMaxActive(druidProp.getMaxActive());
        log.info("数据源最小空闲连接数：" + druidProp.getMinIdle());
        dataSource.setMinIdle(druidProp.getMinIdle());
        log.info("数据源初始大小：" + druidProp.getInitialSize());
        dataSource.setInitialSize(druidProp.getInitialSize());
        log.info("数据源验证SQL语句：" + druidProp.getValidationQuery());
        dataSource.setValidationQuery(druidProp.getValidationQuery());
        log.info("数据源是否在获取连接时测试：" + druidProp.isTestOnBorrow());
        dataSource.setTestOnBorrow(druidProp.isTestOnBorrow());
        log.info("数据源是否在返回连接时测试：" + druidProp.isTestOnReturn());
        dataSource.setTestOnReturn(druidProp.isTestOnReturn());
        log.info("数据源连接是否在空闲时测试：" + druidProp.isTestWhileIdle());
        dataSource.setTestWhileIdle(druidProp.isTestWhileIdle());
        log.info("数据源最大等待时间：" + druidProp.getMaxWait());
        dataSource.setMaxWait(druidProp.getMaxWait());
        dataSource.setPoolPreparedStatements(druidProp.isPoolPreparedStatements());

        try {
            dataSource.setFilters("slf4j");
            log.info("数据源日志采集使用slf4j........................");
        } catch (Exception e) {
            log.error("数据源异常啦.........................." + e);
        }

        List<Filter> filters = new ArrayList<>();
        filters.add(new Slf4jLogFilter());
        dataSource.setProxyFilters(filters);
        log.info("数据源初始化完成..................................");

        log.info("数据源初始化完成..................................");
        return dataSource;
    }
}
