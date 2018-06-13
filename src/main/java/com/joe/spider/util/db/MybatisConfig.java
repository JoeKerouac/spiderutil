package com.joe.spider.util.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.sql.DataSource;

/**
 * mybatis配置
 * <p>
 * 如果{@link #scanPackage scanPackage}和{@link #configLocation configLocation}同时存在那么会合并两个定义
 *
 * @author joe
 * @version 2018.06.08 10:42
 */
@ToString
public class MybatisConfig {
    /**
     * 要扫描的包的集合，会自动找到这些包下的ResultMap、mapper（可以为空，为空时configLocation不能为空，如果为空时将仅采
     * 用本地配置文件配置Configuration）
     *
     * @see #configLocation
     */
    @Getter
    @Setter
    private String scanPackage;
    /**
     * 别名扫描包，会自动将该包下所有除了匿名类、接口、成员类外的所有类注册别名，没有加@Alias注解的将会使用类名（不包括包名）作为别名。
     * <p>
     * 如果该值未设置或者为空那么将会自动使用{@link #scanPackage scanPackage}的值
     *
     * @see org.apache.ibatis.type.TypeAliasRegistry#registerAliases(String)
     */
    @Getter
    @Setter
    private String aliasScanPackage;
    /**
     * 本地的xml配置文件，可以为空（为空时scanPackage不能为空，如果不为空时会采用config类和本地xml文件混合配置的方式配
     * 置Configuration）
     * <p>
     * 支持协议前缀参照{@link com.joe.utils.common.ResourceHelper.ResourceProtocol ResourceProtocol}，如果没有指定协议前缀那么默认认为是classpath
     *
     * @see #scanPackage
     */
    @Getter
    @Setter
    private String configLocation;
    /**
     * 数据源
     */
    @Getter
    @Setter
    private DataSource dataSource;
    /**
     * 使用java配置的环境ID（对应xml配置文件中environment标签的id属性）
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


    public MybatisConfig() {
        this(null, null, null);
    }

    public MybatisConfig(DataSource dataSource, String id, String packages) {
        this.dataSource = dataSource;
        this.id = id;
        this.scanPackage = packages;
        this.aliasScanPackage = packages;
    }
}
