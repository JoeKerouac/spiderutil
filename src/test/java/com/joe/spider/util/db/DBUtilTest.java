package com.joe.spider.util.db;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.joe.spider.util.db.plugin.PagePlugin;
import com.joe.spider.util.db.sql.dialect.Mysql;
import com.joe.utils.data.PageData;

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
    static String             url      = "jdbc:mysql://dbserver:3306/movie?characterEncoding=utf-8";
    /**
     * 数据库用户名
     */
    static String             username = "root";
    /**
     * 数据库密码
     */
    static String             password = "Qwer1@34";
    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() {
        MybatisConfig config = new MybatisConfig(DBUtil.buildDatasource(url, username, password),
            String.valueOf(System.currentTimeMillis()), "com.joe.spider");
        Mysql mysql = new Mysql();
        //设置分页插件，分页插件对以Page结尾的sql的id进行分页
        config.addInterceptor(new PagePlugin(".*Page", mysql, mysql));
        sqlSessionFactory = DBUtil.build(config);
    }

    /**
     * Dao测试
     * @throws Exception Exception
     */
    @Test
    public void dbTest() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Dao dao = session.getMapper(Dao.class);

            List<History> histories = dao.selectAllHistory();
            Assert.assertNotEquals(0, histories.size());
        }
    }

    @Test
    public void doInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Dao dao = session.getMapper(Dao.class);

            History history = new History();
            history.setContain(true);
            history.setId("id123");
            history.setIp("ip123456");
            history.setTime("time123456");
            history.setKey("keyboard123123");
            Assert.assertEquals(dao.insertHistory(history), 1);
        }
    }

    /**
     * 分页插件测试
     *
     * @throws Exception Exception
     */
    @Test
    public void dbPageTest() throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Dao dao = session.getMapper(Dao.class);

            PageData<History> pageData = new PageData<>();
            pageData.setCurrentPage(0);
            pageData.setLimit(3);

            List<History> histories = dao.selectHistoryAndPage("123","123", pageData);
            Assert.assertEquals(pageData.getDatas().size(), histories.size());
        }
    }
}