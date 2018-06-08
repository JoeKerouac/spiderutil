package com.joe.spider.util.db;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.joe.utils.common.StringUtils;
import com.joe.utils.type.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库工具，如果需要集成spring可以使用{@link SpringDBUtil SpringDBUtil}
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
        return build(new DBConfig(buildDatasource(url, username, password), id, packages));
    }


    /**
     * 通过数据源构建SqlSessionFactory
     *
     * @param config mybatis配置
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory build(DBConfig config) {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(buildConfiguration(config));
        return sqlSessionFactory;
    }

    /**
     * 构建数据源
     *
     * @param url      数据库地址
     * @param username 数据库账号
     * @param password 数据库密码
     * @return 数据源
     */
    public static DataSource buildDatasource(String url, String username, String password) {
        DruidProp druidProp = new DruidProp();
        druidProp.setUrl(url);
        druidProp.setUsername(username);
        druidProp.setPassword(password);
        return buildDatasource(druidProp);
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

    /**
     * 通过数据源构建Configuration
     *
     * @param config mybatis配置
     * @return Configuration
     */
    static Configuration buildConfiguration(DBConfig config) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment(config.getId(), transactionFactory, config.getDataSource());
        Configuration configuration = new Configuration(environment);
        //说明：必须先扫描ResultMap，然后扫描Mapper，不然如果Mapper中使用ResultMap将会报错
        //扫描ResultMap
        scanResutlMap(configuration, config.getScanPackage());
        //扫描mapper
        scanMapper(configuration, config.getScanPackage());
        //扫描类型别名
        scanTypeAlias(config);
        //注册类型别名
        TypeAliasRegistry registry = configuration.getTypeAliasRegistry();
        config.getTypeAlias().forEach(registry::registerAlias);
        return configuration;
    }

    /**
     * 扫描指定包下的mapper
     *
     * @param configuration configuration
     * @param packages      包集合
     */
    static void scanMapper(Configuration configuration, String... packages) {
        List<Class<?>> mappers = scanMapper(false, packages);
        mappers.forEach(configuration::addMapper);
    }

    /**
     * 扫描指定包下的mapper
     *
     * @param useXml   扫描使用xml编写sql的mapper还是使用使用注解编写sql的mapper，true表示扫描使用xml编写sql的mapper
     * @param packages 包集合
     * @return 扫描的mapper
     */
    static List<Class<?>> scanMapper(boolean useXml, String... packages) {
        List<Class<?>> mappers = ReflectUtil.getAllAnnotationPresentClass(Mapper.class, packages);
        mappers = mappers.stream().filter(mapper -> mapper.getAnnotation(Mapper.class).useXml() == useXml).collect
                (Collectors.toList());
        log.debug("扫描到的Mapper列表为：{}", mappers);
        return mappers;
    }

    /**
     * 扫描指定报下的类型别名并加入DBConfig
     *
     * @param config DB配置
     */
    static void scanTypeAlias(DBConfig config) {
        List<Class<?>> classes = ReflectUtil.getAllAnnotationPresentClass(TypeAlias.class, config.getScanPackage());
        classes.parallelStream().forEach(clazz -> {
            TypeAlias typeAlias = clazz.getAnnotation(TypeAlias.class);
            String alias = typeAlias.alias();
            alias = StringUtils.isEmpty(alias) ? clazz.getSimpleName() : alias;
            config.addTypeAlias(alias, clazz);
        });
    }

    /**
     * 扫描指定包下的ResultMap
     *
     * @param configuration configuration
     * @param packages      包集合
     */
    static void scanResutlMap(Configuration configuration, String... packages) {
        log.debug("开始扫描包{}下的ResultMap列表", packages);
        List<Class<?>> resultMaps = ReflectUtil.getAllAnnotationPresentClass(ResultMapDefine.class, packages);
        log.debug("扫描到的ResultMap列表为：{}", resultMaps);
        resultMaps.parallelStream().forEach(clazz -> {
            ResultMap resultMap = ResultMapHelper.build(clazz, configuration);
            if (resultMap != null) {
                configuration.addResultMap(resultMap);
            }
        });
    }
}
