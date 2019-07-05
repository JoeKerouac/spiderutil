package com.joe.spider.util.db.annotation;

import java.lang.annotation.*;

/**
 * ResultMapping定义类上需要加上该注解才能被发现
 * <p>
 * 该注解定义的ResultMap在xml中也能使用
 *
 * @author joe
 * @version 2018.06.07 09:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface ResultMapDefine {
    /**
     * ResultMapping的ID，如果指定id那么id需要包含namespace，即namespace.id这种形式，如果没有namespace那么将会使
     * 用ResultMap对应的类的完全限定名作为namespace（是否包含namespace是根据id字符串是否包含'.'来判断的）
     *
     * @return ResultMapping的ID，默认为空，使用default作为默认namespace，采用default.类名作为ID，例如com.joe.User默认
     * 对应的ResultMap的id为default.User
     */

    String value() default "";
}
