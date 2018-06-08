package com.joe.spider.util.db;

import com.joe.utils.common.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * mybatis配置
 *
 * @author joe
 * @version 2018.06.08 10:42
 */
@ToString
public class DBConfig {
    /**
     * 要扫描的包的集合，会自动找到这些包下的ResultMap、mapper、TypeAlias
     */
    @Getter
    @Setter
    private String[] scanPackage;
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
     * 类型别名集合
     */
    @Getter
    private final Map<String, Class<?>> typeAlias;

    public DBConfig() {
        this(null, null, null);
    }

    public DBConfig(DataSource dataSource, String id, String... packages) {
        this.dataSource = dataSource;
        this.id = id;
        this.scanPackage = packages;
        this.typeAlias = new HashMap<>();
    }

    /**
     * 添加一个类型别名
     *
     * @param name 类型别名
     * @param type 类型
     */
    public void addTypeAlias(String name, Class<?> type) {
        if (StringUtils.isEmpty(name) || type == null) {
            throw new NullPointerException("类型别名或者类型不能为null");
        }
        typeAlias.put(name, type);
    }
}
