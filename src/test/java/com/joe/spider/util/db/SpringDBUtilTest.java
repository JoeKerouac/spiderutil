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

/**
 * SpringDBUtil测试
 *
 * @author joe
 * @version 2018.06.08 10:21
 */
@Configuration
public class SpringDBUtilTest {
    private HistoryMapper mapper;
    private ApplicationContext context;
    private Dao dao;

    /**
     * 测试xml和注解混合使用
     */
    @Test
    public void doMixTest() {
        System.out.println(mapper.getHistoryByResultType());
        System.out.println(mapper.getHistoryByResultMap());
        System.out.println(dao.selectAllHistory());
    }

    @Before
    public void init() {
        context = new AnnotationConfigApplicationContext("com.joe.spider");
        mapper = context.getBean(HistoryMapper.class);
        dao = context.getBean(Dao.class);
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(ResourceLoader loader) {
        return SpringDBUtil.buildSqlSessionFactoryBean(DBUtilTest.url, DBUtilTest.username, DBUtilTest.password,
                "123", loader, "com.joe.spider");
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        return SpringDBUtil.buildMapperScannerConfigurer("sqlSessionFactory", "com.joe.spider");
    }
}
