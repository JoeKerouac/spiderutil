package com.joe.spider.util.db;

import java.lang.annotation.*;

/**
 * ResultMapping定义类上需要加上该注解才能被发现
 *
 * @author joe
 * @version 2018.06.07 09:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ResultMappingDefine {
    /**
     * ResultMapping的ID
     *
     * @return ResultMapping的ID，默认为空，采用类名
     */
    String id() default "";
}
