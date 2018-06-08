package com.joe.spider.util.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 数据库工具测试
 * <p>
 * 说明：需要给定的url中存在一个表叫history，有{@link History History}中的字段
 *
 * @author joe
 * @version 2018.06.07 18:30
 */
public class DBUtilTest {
    /**
     * 数据库连接URL
     */
    static String url = "jdbc:mysql://dbserver:9999/movie?characterEncoding=utf-8&allowMultiQueries=true";
    /**
     * 数据库用户名
     */
    static String username = "root";
    /**
     * 数据库密码
     */
    static String password = "Qwer1@34";

    @Test
    public void dbTest() throws Exception {
        SqlSessionFactory sqlSessionFactory = DBUtil.build(url, username, password, String.valueOf(System
                .currentTimeMillis()), "com.joe.spider");
        Dao dao = sqlSessionFactory.openSession().getMapper(Dao.class);

        List<History> histories = dao.selectAllHistory();
        Assert.assertNotEquals(0, histories.size());
    }
}
