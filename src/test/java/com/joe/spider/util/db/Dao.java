package com.joe.spider.util.db;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author joe
 * @version 2018.06.08 11:51
 */
@Mapper
public interface Dao {
    /**
     * 查找最多10条history（ResultMap使用注解定义的ResultMap，如果需要也可以使用xml中定义的ResultMap）
     *
     * @return 最多10条history
     */
    @ResultMap("default.History")
    @Select("select * from history limit 0 , 10")
    List<History> selectAllHistory();
}
