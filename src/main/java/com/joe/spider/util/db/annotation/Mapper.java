package com.joe.spider.util.db.annotation;

import java.lang.annotation.*;

/**
 * mapper注解，带有该注解的mapper会被自动发现
 *
 * @author joe
 * @version 2017.12.17 15:06
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapper {
    /**
     * 是否是使用XML编写sql的（暂时无用，请勿赋值）
     *
     * @return 默认不是，即默认认为是使用注解写sql的
     */
    boolean useXml() default false;
}
