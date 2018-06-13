package com.joe.spider.util.db;

import com.joe.utils.common.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author joe
 * @version 2018.06.13 16:11
 */
@Service
public class UserService {
    @Autowired
    private UserMapper mapper;
    @Autowired
    private Dao dao;

    @Transactional
    public void createUser() {
        System.out.println("插入数量是：" + mapper.insert(new User(Tools.createUUID(), "joe")));
        System.out.println("插入数量是：" + dao.insert(new User(Tools.createUUID(), "joe")));
        throw new RuntimeException("回滚");
    }
}
