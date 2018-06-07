package com.joe.spider.util;

import org.junit.Test;

/**
 * @author joe
 * @version 2018.03.08 17:34
 */
public class SpiderTest {
    /**
     * 测试爬虫以及爬虫使用示例
     *
     * @throws Exception 异常
     */
    @Test
    public void testSpider() throws Exception {
        Spider spider = new Spider(10);
        spider.addTask("http://127.0.0.1:12345/api/getUsers", System.out::println);
    }
}
