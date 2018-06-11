package com.joe.spider.util.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.sql.DataSource;

/**
 * mybatis配置
 *
 * @author joe
 * @version 2018.06.08 10:42
 */
@ToString
public class DBConfig {
    /**
     * 要扫描的包的集合，会自动找到这些包下的ResultMap、mapper
     */
    @Getter
    @Setter
    private String scanPackage;
    /**
     * 数据源
     */
    @Getter
    @Setter
    private DataSource dataSource;
    /**
     * ID
     */
    @Getter
    @Setter
    private String id;
    /**
     * mapper文件（xml）位置，默认所有Classpath下边所有后缀为Mapper.xml的文件都认为是Mapper
     */
    @Getter
    @Setter
    private String mappersLocation = "classpath*:**/*Mapper.xml";

    public DBConfig() {
        this(null, null, null);
    }

    public DBConfig(DataSource dataSource, String id, String packages) {
        this.dataSource = dataSource;
        this.id = id;
        this.scanPackage = packages;
    }
}
