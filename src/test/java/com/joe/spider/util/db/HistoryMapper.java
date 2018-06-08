package com.joe.spider.util.db;

import java.util.List;

/**
 * @author joe
 * @version 2018.06.08 10:28
 */
public interface HistoryMapper {
    /**
     * 查找最多10条history
     *
     * @return 最多10条history
     */
    List<History> getHistoryByResultMap();

    /**
     * 查找最多10条history
     *
     * @return 最多10条history
     */
    List<History> getHistoryByResultType();
}
