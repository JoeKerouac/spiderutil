package com.joe.spider.util.db.sql;

/**
 * 分页sql转换
 *
 * @author joe
 * @version 2018.06.22 16:59
 */
public interface PageSql {
    /**
     * 分页函数，对指定sql分页
     *
     * @param sql    原始sql
     * @param pageNo 要获取的页码，从0开始
     * @param limit  每页最大数量
     * @return 原始sql对应的分页sql
     */
    String page(String sql, int pageNo, int limit);
}
