package com.joe.spider.util.db;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

import static com.joe.spider.util.db.DBUtil.buildConfiguration;
import static com.joe.spider.util.db.DBUtil.buildDatasource;

/**
 * 集成spring的DButil，如果不需要集成spring只需使用{@link DBUtil DBUtil}
 * <p>
 * 注意：如果使用DBConfig配置mybatis那么就不能使用resource配置，两个只能选一个
 * <p>
 * xml文件中可以使用注解{@link ResultMapDefine ResultMapDefine}定义的ResultMap
 *
 * @author joe
 * @version 2018.06.08 10:13
 */
@Slf4j
public class SpringDBUtil {

    /**
     * 构建SqlSessionFactoryBean（spring中需要使用）采用DBConfig配置而不是xml
     *
     * @param url      数据库URL
     * @param username 数据库用户名
     * @param password 数据库密码
     * @param id       数据库ID
     * @param packages mapper和ResultMap所在的包
     * @return SqlSessionFactoryBean
     */
    public static SqlSessionFactoryBean buildSqlSessionFactoryBean(String url, String username, String password,
                                                                   String id, String...
                                                                           packages) {
        DataSource dataSource = buildDatasource(url, username, password);
        return buildSqlSessionFactoryBean(new DBConfig(dataSource, id, packages));
    }

    /**
     * 构建SqlSessionFactoryBean（spring中需要使用）
     *
     * @param config mybatis配置
     * @return SqlSessionFactoryBean
     */
    public static SqlSessionFactoryBean buildSqlSessionFactoryBean(DBConfig config) {
        return buildSqlSessionFactoryBean(null, config);
    }

    /**
     * 构建SqlSessionFactoryBean（spring中需要使用）
     *
     * @param resourceLocation mybatis的配置文件资源位置（必须可以使用spring的ResourceLoader加载得到）
     * @param resourceLoader   spring资源加载器，用于加载上述资源
     * @return SqlSessionFactoryBean
     */
    public static SqlSessionFactoryBean buildSqlSessionFactoryBean(String resourceLocation, ResourceLoader
            resourceLoader) {
        Resource resource;
        try {
            resource = resourceLoader.getResource(resourceLocation);
        } catch (Exception e) {
            throw new RuntimeException("找不到指定的xml文件，指定xml文件位置为：" + resourceLocation);
        }
        return buildSqlSessionFactoryBean(resource, null);
    }

    /**
     * 构建SqlSessionFactoryBean（spring中需要使用）
     *
     * @param resource mybatis的配置文件资源（可以使用spring的ResourceLoader加载得到）（当有该配置的时候DBConfig会失效）
     * @param config   mybatis配置
     * @return SqlSessionFactoryBean
     */
    static SqlSessionFactoryBean buildSqlSessionFactoryBean(Resource resource, DBConfig
            config) {
        log.info("开始构建SqlSessionFactoryBean");
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(config.getDataSource());
        if (resource == null) {
            log.info("使用注解配置SqlSessionFactoryBean");
            bean.setConfiguration(buildConfiguration(config));
        } else {
            log.info("使用xml配置SqlSessionFactoryBean");
            bean.setConfigLocation(resource);
        }
        return bean;
    }

    /**
     * 构建MapperScannerConfigurer（用于扫描mapper接口）
     *
     * @param sqlSessionFactoryBeanName SqlSessionFactoryBean的名称
     * @param basePackage               要扫描的根目录，该目录下所有接口都会被当做bean处理
     * @return MapperScannerConfigurer
     */
    public static MapperScannerConfigurer buildMapperScannerConfigurer(String sqlSessionFactoryBeanName, String
            basePackage) {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setSqlSessionFactoryBeanName(sqlSessionFactoryBeanName);
        configurer.setBasePackage(basePackage);
        return configurer;
    }
}
