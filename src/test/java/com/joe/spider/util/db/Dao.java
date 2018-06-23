package com.joe.spider.util.db;

import com.joe.utils.data.PageData;
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
     * 查找所有history并分页（ResultMap使用注解定义的ResultMap，如果需要也可以使用xml中定义的ResultMap）
     *
     * @param pageData 分页参数，根据PageData的currentPage和limit进行分页并将结果返回（同时会正确设置PageData中的各项参数）
     * @return 最多10条history
     */
    @ResultMap("default.History")
    @Select("select * from history")
    List<History> selectHistoryAndPage(PageData<History> pageData);

    /**
     * 查找最多10条history（ResultMap使用注解定义的ResultMap，如果需要也可以使用xml中定义的ResultMap）
     *
     * @return 最多10条history
     */
    @ResultMap("default.History")
    @Select("select * from history limit 0 , 10")
    List<History> selectAllHistory();

    /**
     * 插入用户
     *
     * @param user 要插入的用户
     * @return 插入数量
     */
    @Insert("insert into user (id , name) values (#{id} , #{name})")
    long insert(User user);
}
