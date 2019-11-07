package com.joe.spider.util;

/**
 * 爬虫异常
 *
 * @author JoeKerouac
 * @version 2019年10月11日 20:29
 */
public class SpiderException extends RuntimeException{

    private static final long serialVersionUID = 7575091623770028527L;

    public SpiderException() {
    }

    public SpiderException(String message) {
        super(message);
    }

    public SpiderException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpiderException(Throwable cause) {
        super(cause);
    }
}
