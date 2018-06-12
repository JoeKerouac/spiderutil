package com.joe.spider.util.db;

import com.joe.utils.common.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.stream.Stream;

import static com.joe.spider.util.db.DBUtil.buildConfiguration;
import static com.joe.spider.util.db.DBUtil.buildDatasource;

/**
 * 集成spring的DButil，如果不需要集成spring只需使用{@link DBUtil DBUtil}
 * <p>
 *
 * @author joe
 * @version 2018.06.08 10:13
 */
@Slf4j
public class SpringDBUtil {
    /**
     * 构建SqlSessionFactoryBean（spring中需要使用）采用DBConfig配置而不是xml（scanPackage和configLocation不能同时为空）
     *
     * @param url            数据库URL
     * @param username       数据库用户名
     * @param password       数据库密码
     * @param id             数据库ID
     * @param loader         ResourceLoader，用于加载mapper.xml
     * @param scanPackage    mapper和ResultMap所在的包
     * @param configLocation 本地xml配置文件
     * @return SqlSessionFactoryBean
     */
    public static SqlSessionFactoryBean buildSqlSessionFactoryBean(String url, String username, String password,
                                                                   String id, ResourceLoader loader, String
                                                                           scanPackage, String configLocation) {
        DataSource dataSource = buildDatasource(url, username, password);
        DBConfig config = new DBConfig(dataSource, id, scanPackage);
        config.setConfigLocation(configLocation);
        return buildSqlSessionFactoryBean(loader, config);
    }


    /**
     * 构建SqlSessionFactoryBean（spring中需要使用）
     *
     * @param loader ResourceLoader，用于加载mapper.xml
     * @param config mybatis配置
     * @return SqlSessionFactoryBean
     */
    public static SqlSessionFactoryBean buildSqlSessionFactoryBean(ResourceLoader loader, DBConfig config) {
        log.info("开始构建SqlSessionFactoryBean");
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(config.getDataSource());

        log.info("使用注解配置SqlSessionFactoryBean");
        Configuration configuration = buildConfiguration(config, null);
        bean.setConfiguration(configuration);

        log.debug("查找mapper文件");
        String mapperLocation = config.getMappersLocation();
        if (!StringUtils.isEmpty(mapperLocation)) {
            log.debug("mapper文件位置为：[{}]", mapperLocation);
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
            try {
                Resource[] resources = resolver.getResources(mapperLocation);
                Stream.of(resources).parallel().forEach(r -> log.debug("添加mapper文件[{}]", r.getDescription()));
                bean.setMapperLocations(resources);
            } catch (IOException e) {
                log.warn("mapper文件[{}]加载失败", mapperLocation, e);
            }
            log.debug("mapper文件加载完成");
        }

        return bean;
    }

    /**
     * 构建MapperScannerConfigurer（用于扫描mapper接口）
     *
     * @param sqlSessionFactoryBeanName SqlSessionFactoryBean的名称
     * @param basePackage               要扫描的根目录，该目录下所有接口都会被当做bean处理（该路径要尽可能精确同时除了mapper接口外尽量不要放置其他接口，不然都会当做mapper
     *                                  来处理生成bean，占用多余内存）
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
