package com.joe.spider.util.db.exception;

/**
 * 不支持异常
 *
 * @author joe
 * @version 2018.06.22 17:16
 */
public class NoSupportedException extends DBException {
    public NoSupportedException() {
        super();
    }

    public NoSupportedException(String message) {
        super(message);
    }

    public NoSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSupportedException(Throwable cause) {
        super(cause);
    }

    protected NoSupportedException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
