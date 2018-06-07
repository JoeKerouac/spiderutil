package com.joe.spider.util.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
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
    private String url = "jdbc:mysql://dbserver:9999/movie?characterEncoding=utf-8&allowMultiQueries=true";
    /**
     * 数据库用户名
     */
    private String username = "root";
    /**
     * 数据库密码
     */
    private String password = "Qwer1@34";

    @Test
    public void dbTest() throws Exception {
        SqlSessionFactory sqlSessionFactory = DBUtil.build(url, username, password, String.valueOf(System
                .currentTimeMillis()), "com.joe.spider");
        Dao dao = sqlSessionFactory.openSession().getMapper(Dao.class);

        List<History> histories = dao.selectAllHistory();
        Assert.assertNotEquals(0, histories.size());
    }

    @Mapper
    interface Dao {
        /**
         * 查找最多10条history
         * @return
         * 最多10条history
         */
        @ResultMap("default.History")
        @Select("select * from history limit 0 , 10")
        List<History> selectAllHistory();
    }

    /**
     * 定义在其他类中必须是public static，基本类型必须是对应的包装类型
     */
    @ResultMapDefine
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class History {
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
}
