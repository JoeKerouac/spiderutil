package com.joe.spider.util.db;

import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static com.joe.spider.util.db.DBUtil.buildDatasource;

/**
 * SpringDBUtil测试
 *
 * @author joe
 * @version 2018.06.08 10:21
 */
@Configuration
@EnableTransactionManagement
public class SpringDBUtilTest {
    private UserMapper mapper;
    private ApplicationContext context;
    private Dao dao;
    private UserService userService;

    /**
     * 测试xml和注解混合使用
     */
    @Test
    public void doMixTest() {
        System.out.println(mapper.getHistoryByResultType());
        System.out.println(mapper.getHistoryByResultMap());
        System.out.println(dao.selectAllHistory());
    }

    /**
     * 测试事务
     */
    @Test
    public void doTransactionalTest() {
        //由于有异常抛出并不会实际的插入数据
        try {
            userService.createUser();
        } catch (RuntimeException e) {

        }
    }

    @Before
    public void init() {
        context = new AnnotationConfigApplicationContext("com.joe.spider");
        mapper = context.getBean(UserMapper.class);
        dao = context.getBean(Dao.class);
        userService = context.getBean(UserService.class);
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DataSource dataSource() {
        DataSource dataSource = buildDatasource(DBUtilTest.url, DBUtilTest.username, DBUtilTest.password);
        return dataSource;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(ResourceLoader loader, DataSource dataSource) {
        MybatisConfig config = new MybatisConfig(dataSource, "dev", "com.joe.spider");
        return SpringDBUtil.buildSqlSessionFactoryBean(loader, config);
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        return SpringDBUtil.buildMapperScannerConfigurer("sqlSessionFactory", "com.joe.spider");
    }
}
