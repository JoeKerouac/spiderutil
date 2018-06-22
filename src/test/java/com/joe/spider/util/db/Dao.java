package com.joe.spider.util.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
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
    @Select("select * from history where contain=#{contain}")
    List<History> selectAllHistory(@Param("contain") boolean contain);

    /**
     * 查找最多10条history（ResultMap使用注解定义的ResultMap，如果需要也可以使用xml中定义的ResultMap）
     *
     * @return 最多10条history
     */
    @ResultMap("default.History")
    @Select("select * from history")
    List<History> selectAllHistory1();

    /**
     * 插入用户
     *
     * @param user 要插入的用户
     * @return 插入数量
     */
    @Insert("insert into user (id , name) values (#{id} , #{name})")
    long insert(User user);
}
