package com.joe.spider.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joe.utils.test.WebBaseTest;

/**
 * @author joe
 * @version 2018.03.08 17:34
 */
public class SpiderTest extends WebBaseTest {

    /**
     * 测试爬虫以及爬虫使用示例
     *
     */
    @Test
    public void testSpider() {
        runCase(() -> {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean flag = new AtomicBoolean(false);
            Spider spider = new Spider(10);
            spider.addTask(getBaseUrl() + "test/hello?name=JoeKerouac", result -> {
                System.out.println(result);
                latch.countDown();
                flag.set("hello : JoeKerouac".equals(result));
            });
            latch.await(10, TimeUnit.SECONDS);
            Assert.assertTrue(flag.get());
            spider.shutdown(true);
        });
    }

    @Controller
    @RequestMapping("test")
    public static class SpringApi {
        @RequestMapping(value = "hello")
        @ResponseBody
        public String helloName(String name) {
            return "hello : " + name;
        }
    }

}
