package com.joe.spider.util.db.sql;

/**
 * 统计数量sql
 *
 * @author joe
 * @version 2018.06.22 17:02
 */
public interface CountSql {
    /**
     * 统计数量函数，对指定sql统计结果数量
     *
     * @param sql 原始sql
     * @return 原始sql对应的统计sql
     */
    String count(String sql);
}
