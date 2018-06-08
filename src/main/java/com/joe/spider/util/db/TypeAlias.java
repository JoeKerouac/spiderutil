package com.joe.spider.util.db;

import java.lang.annotation.*;

/**
 * 类型别名注解
 * <p>
 * 添加该注解的pojo将自动添加到类型别名映射中
 *
 * @author joe
 * @version 2018.06.08 11:57
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TypeAlias {
    /**
     * 类型别名，默认为空使用pojo类的类名
     *
     * @return 类型别名
     */
    String alias() default "";
}
