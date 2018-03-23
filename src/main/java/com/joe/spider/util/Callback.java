package com.joe.spider.util;

/**
 * 网络请求回调
 *
 * @author joe
 * @version 2017.12.24 14:47
 */
public interface Callback {
    /**
     * 当网络请求完毕后回调该方法
     *
     * @param result 网络请求的结果
     */
    void exec(String result);
}
