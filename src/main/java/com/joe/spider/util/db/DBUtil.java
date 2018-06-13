package com.joe.spider.util.db;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.joe.spider.util.db.exception.ConfigException;
import com.joe.utils.common.ResourceHelper;
import com.joe.utils.common.StringUtils;
import com.joe.utils.type.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * @param packages 要扫描的包（会自动将带扫描的包下面带{@link com.joe.spider.util.db.Mapper}注解的类，将该类注册为mapper
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory build(String url, String username, String password, String id, String packages) {
        return build(new MybatisConfig(buildDatasource(url, username, password), id, packages), null);
    }

    /**
     * 通过数据库配置构建SqlSessionFactory
     *
     * @param url      数据库url（完整的URL，例如jdbc:mysql://example.com/test?characterEncoding=utf8）
     * @param username 数据库用户名
     * @param password 数据库密码
     * @param id       SqlSessionFactory的ID
     * @param scanner  查找带有指定注解的Class的扫描器（可以为null）
     * @param packages 要扫描的包（会自动将带扫描的包下面带{@link com.joe.spider.util.db.Mapper}注解的类，将该类注册为mapper
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory build(String url, String username, String password, String id,
                                          ClassScannerByAnnotation scanner, String packages) {
        return build(new MybatisConfig(buildDatasource(url, username, password), id, packages), scanner);
    }

    /**
     * 通过数据源构建SqlSessionFactory
     *
     * @param config mybatis配置
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory build(MybatisConfig config) {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(buildConfiguration(config, null));
        return sqlSessionFactory;
    }

    /**
     * 通过数据源构建SqlSessionFactory
     *
     * @param config  mybatis配置
     * @param scanner 查找带有指定注解的Class的扫描器（可以为null）
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory build(MybatisConfig config, ClassScannerByAnnotation scanner) {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(buildConfiguration(config, scanner));
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
     * @param config  mybatis配置
     * @param scanner 查找带有指定注解的Class的扫描器（可以为null，为null时会使用默认的）
     * @return Configuration
     */
    static Configuration buildConfiguration(MybatisConfig config, ClassScannerByAnnotation scanner) {
        log.info("通过DBConfig[{}]构建Configuration", config);
        String scanPackage = config.getScanPackage();
        String configLocation = config.getConfigLocation();

        if (StringUtils.isEmptyAll(scanPackage, configLocation)) {
            throw new ConfigException("scanPackage和configLocation至少指定一个");
        }

        Configuration scanConfiguration = null;
        Configuration xmlConfiguration = null;
        Configuration configuration = null;

        if (!StringUtils.isEmpty(scanPackage)) {
            log.info("当前存在java配置的Configuration");
            configuration = scanConfiguration = buildConfigurationByConfig(config, scanner);
        }

        if (!StringUtils.isEmpty(configLocation)) {
            log.info("当前存在xml配置的Configuration");
            configuration = xmlConfiguration = buildConfigurationByXml(config.getConfigLocation());
        }
        if (xmlConfiguration != null && scanConfiguration != null) {
            //此时configuration指向的是xmlConfiguration
            log.info("同时存在java配置的Configuration和XML配置的Configuration，合并两个Configuration");
            //合并resultMap，由于Configuration放入resultmap的时候会放入两份，所以要先去重
            Map<String, ResultMap> map = new HashMap<>();
            scanConfiguration.getResultMaps().parallelStream().forEach(resultMap -> map.put(resultMap.getId(),
                    resultMap));
            map.values().parallelStream().forEach(configuration::addResultMap);
            //合并的Mapper
            scanConfiguration.getMapperRegistry().getMappers().parallelStream().forEach(configuration
                    .getMapperRegistry()::addMapper);
            //合并别名
            scanConfiguration.getTypeAliasRegistry().getTypeAliases().forEach(configuration.getTypeAliasRegistry()
                    ::registerAlias);
            //使用Config中的Environment
            configuration.setEnvironment(scanConfiguration.getEnvironment());
        }
        return configuration;
    }

    /**
     * 使用java配置构建Configuration
     *
     * @param config  配置文件
     * @param scanner 查找带有指定注解的Class的扫描器（可以为null，为null时会使用默认的）
     * @return Configuration
     */
    private static Configuration buildConfigurationByConfig(MybatisConfig config, ClassScannerByAnnotation scanner) {
        //判断是否提供自定义scanner
        if (scanner == null) {
            scanner = ReflectUtil::getAllAnnotationPresentClass;
        }
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment(config.getId(), transactionFactory, config.getDataSource());
        Configuration configuration = new Configuration(environment);

        String scanPackage = config.getScanPackage();
        String aliasScanPackage = config.getAliasScanPackage();

        //说明：必须先扫描ResultMap，然后扫描Mapper，不然如果Mapper中使用ResultMap将会报错
        //扫描ResultMap
        scanResutlMap(configuration, scanner, scanPackage);
        //扫描mapper
        scanMapper(configuration, scanner, scanPackage);
        //扫描类型别名并注册
        configuration.getTypeAliasRegistry().registerAliases(StringUtils.isEmpty(aliasScanPackage) ? scanPackage :
                aliasScanPackage);
        return configuration;
    }

    /**
     * 使用xml配置文件加载Configuration
     *
     * @param configLocation xml配置文件位置
     * @return Configuration
     */
    private static Configuration buildConfigurationByXml(String configLocation) {
        return new XMLConfigBuilder(ResourceHelper.getResource(configLocation)).parse();
    }

    /**
     * 扫描指定包下的mapper
     *
     * @param configuration configuration
     * @param scanner       查找带有指定注解的Class的扫描器
     * @param packages      包集合
     */
    static void scanMapper(Configuration configuration, ClassScannerByAnnotation scanner, String... packages) {
        List<Class<?>> mappers = scanMapper(false, scanner, packages);
        mappers.forEach(configuration::addMapper);
    }

    /**
     * 扫描指定包下的mapper
     *
     * @param useXml   扫描使用xml编写sql的mapper还是使用使用注解编写sql的mapper，true表示扫描使用xml编写sql的mapper
     * @param scanner  查找带有指定注解的Class的扫描器
     * @param packages 包集合
     * @return 扫描的mapper
     */
    static List<Class<?>> scanMapper(boolean useXml, ClassScannerByAnnotation scanner, String... packages) {
        List<Class<?>> mappers = scanner.scan(Mapper.class, packages);
        mappers = mappers.stream().filter(mapper -> mapper.getAnnotation(Mapper.class).useXml() == useXml).collect
                (Collectors.toList());
        log.debug("扫描到的Mapper列表为：{}", mappers);
        return mappers;
    }

    /**
     * 扫描指定包下的ResultMap
     *
     * @param configuration configuration
     * @param scanner       查找带有指定注解的Class的扫描器
     * @param packages      包集合
     */
    static void scanResutlMap(Configuration configuration, ClassScannerByAnnotation scanner, String... packages) {
        log.debug("开始扫描包{}下的ResultMap列表", packages);
        List<Class<?>> resultMaps = scanner.scan(ResultMapDefine.class, packages);
        log.debug("扫描到的ResultMap列表为：{}", resultMaps);
        resultMaps.parallelStream().forEach(clazz -> {
            ResultMap resultMap = ResultMapHelper.build(clazz, configuration);
            if (resultMap != null) {
                configuration.addResultMap(resultMap);
            }
        });
    }


    /**
     * 注解扫描器，扫描带有指定注解的类
     */
    public interface ClassScannerByAnnotation {
        /**
         * 扫描给定包名集合下的所有带有指定注解的类
         *
         * @param annotation 指定注解
         * @param packages   要扫描的包名
         * @return 给定包名集合下的所有带有指定注解的类
         */
        List<Class<?>> scan(Class<? extends Annotation> annotation, String... packages);
    }
}
