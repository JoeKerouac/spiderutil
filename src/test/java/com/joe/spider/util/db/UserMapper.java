package com.joe.spider.util.db;

import java.util.List;

/**
 * @author joe
 * @version 2018.06.08 10:28
 */
public interface UserMapper {
    /**
     * 获取历史记录
     *
     * @return 历史记录
     */
    List<History> getHistoryByResultType();

    /**
     * 获取历史记录
     *
     * @return 历史记录
     */
    List<History> getHistoryByResultMap();

    /**
     * 插入用户
     *
     * @param user 用户
     * @return 插入数量
     */
    long insert(User user);
}
