package com.joe.spider.util.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定义在其他类中必须是static的
 *
 * @author joe
 * @version 2018.06.08 10:31
 */
@ResultMapDefine
@Data
@AllArgsConstructor
@NoArgsConstructor
public class History {
    private String id;
    private boolean contain;
    private String ip;
    private String time;
    /**
     * 数据库中该字段名为keyboard，映射到pojo中为key
     */
    @Property("keyboard")
    private String key;
}
