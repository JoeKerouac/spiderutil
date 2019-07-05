package com.joe.spider.util.db.annotation;

import java.lang.annotation.*;

/**
 * 数据库字段
 *
 * @author joe
 * @version 2018.06.06 18:38
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface Property {
    /**
     * 字段别名（对应的数据库字段名，如果一致可以不用设置）
     *
     * @return 字段别名
     */
    String value() default "";
}
