package com.joe.spider.util.test;

import com.joe.spider.util.Spider;
import com.joe.spider.util.db.DBUtil;
import com.joe.spider.util.db.Mapper;
import com.joe.utils.parse.json.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;

import java.util.List;

/**
 * @author joe
 * @version 2018.03.08 17:34
 */
public class Test {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://ip:port/movie?characterEncoding=utf-8&allowMultiQueries=true";
        String username = "root";
        String password = "";
        SqlSessionFactory sqlSessionFactory = DBUtil.build(url, username, password, String.valueOf(System
                .currentTimeMillis()), "com.joe.spider");
        Dao dao = sqlSessionFactory.openSession().getMapper(Dao.class);
        System.out.println(JsonParser.getInstance().toJson(dao.selectAllHistory()));
    }

    public static void testDB(String url , String username , String password) {
        SqlSessionFactory sqlSessionFactory = DBUtil.build(url, username, password, String.valueOf(System
                .currentTimeMillis()), "com.joe.spider");
        try(SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, TransactionIsolationLevel.NONE)){
            Dao dao = session.getMapper(Dao.class);
            dao.selectAllHistory();
            System.out.println(JsonParser.getInstance().toJson(dao.selectAllHistory()));
            session.commit();
        }
    }


    @Mapper
    interface Dao {
        @ConstructorArgs
        @Select("select id , areaname , shortname , parentid from region")
        List<Region> selectAllRegion();


        @Select("select * from history")
        List<History> selectAllHistory();

    }

    /**
     * 定义在其他类中必须是public static，基本类型必须是对应的包装类型
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class History {
        private String id;
        private boolean contain;
        private String ip;
        private String time;
        private String keyboard;
    }


    @Data
    class Region {
        private String id;
        private String areaname;
        private String shortname;
        private String parentid;
    }


    /**
     * 测试爬虫以及爬虫使用示例
     *
     * @throws Exception 异常
     */
    public static void test() throws Exception {
        Spider spider = new Spider(10);
        spider.addTask("http://127.0.0.1:12345/api/getUsers", System.out::println);
    }
}
