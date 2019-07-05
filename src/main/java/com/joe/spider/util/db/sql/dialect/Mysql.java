package com.joe.spider.util.db.sql.dialect;

import com.joe.spider.util.db.sql.CountSql;
import com.joe.spider.util.db.sql.PageSql;
import com.joe.utils.common.string.StringUtils;

/**
 * mysql方言
 *
 * @author joe
 * @version 2018.06.22 16:59
 */
public class Mysql implements CountSql, PageSql {
    @Override
    public String count(String sql) {
        if (StringUtils.isEmpty(sql)) {
            throw new NullPointerException("原始sql不能为空");
        }
        return "select count(0) from ( " + sql + ") count_table";
    }

    @Override
    public String page(String sql, int pageNo, int limit) {
        if (StringUtils.isEmpty(sql)) {
            throw new NullPointerException("原始sql不能为空");
        }
        if (pageNo < 0 || limit < 1) {
            throw new IllegalArgumentException("分页参数错误，pageNo不能小于0，limit不能小于1");
        }
        return sql + " limit " + (pageNo * limit) + " , " + limit;
    }
}
