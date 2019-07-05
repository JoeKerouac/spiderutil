package com.joe.spider.util.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.joe.spider.util.db.annotation.Property;
import com.joe.spider.util.db.annotation.ResultMapDefine;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;

import com.joe.utils.common.string.StringUtils;
import com.joe.utils.reflect.ReflectUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * ResultMapping辅助生成工具
 *
 * @author joe
 * @version 2018.06.06 18:33
 */
@Slf4j
public class ResultMapHelper {
    /**
     * ResultMap的默认namespace
     */
    final static String DEFAULT_NAMESPACE = "default.";

    /**
     * 构建ResultMap
     *
     * @param resultType    pojo类
     * @param configuration mybatis的configuration
     * @return pojo对应的ResultMap，如果pojo没有带注解ResultMappingDefine或者pojo没有声明任何字段则返回null
     */
    static ResultMap build(Class<?> resultType, Configuration configuration) {
        log.debug("获取[{}]的ResultMapping", resultType);
        if (!resultType.isAnnotationPresent(ResultMapDefine.class)) {
            log.warn("类型[{}]不是ResultMapping", resultType.getName());
            return null;
        }

        Field[] fields = ReflectUtil.getAllFields(resultType);
        if (fields == null || fields.length == 0) {
            log.warn("类型[{}]没有声明任何字段", resultType);
            return null;
        }

        List<ResultMapping> mappings = new ArrayList<>(fields.length);
        //遍历field，构建mapping
        for (Field field : fields) {
            String fieldName = field.getName();

            if (Modifier.isTransient(field.getModifiers())) {
                log.info("字段[{}]是transient的，忽略该字段", fieldName);
                continue;
            }

            if (Modifier.isStatic(field.getModifiers())) {
                log.info("字段[{}]是static的，忽略该字段", fieldName);
                continue;
            }

            if (Modifier.isFinal(field.getModifiers())) {
                log.info("字段[{}]是final的，忽略该字段", fieldName);
                continue;
            }

            log.debug("构建字段[{}]对应的mapping", fieldName);
            Property property = field.getAnnotation(Property.class);
            String name, alias;
            if (property == null || StringUtils.isEmpty(property.value())) {
                name = alias = fieldName;
            } else {
                name = fieldName;
                alias = property.value();
            }

            ResultMapping.Builder mappingBuilder = new ResultMapping.Builder(configuration, name,
                alias, field.getType());
            mappings.add(mappingBuilder.build());
        }

        ResultMapDefine define = resultType.getAnnotation(ResultMapDefine.class);
        String id = StringUtils.isEmpty(define.value())
            ? DEFAULT_NAMESPACE + resultType.getSimpleName()
            : define.value();
        return buildResultMap(configuration, id, resultType, mappings);
    }

    /**
     * 构建ResultMap
     *
     * @param configuration configuration
     * @param id            ResultMap的ID
     * @param resultType    ResultMap对应的pojo
     * @param mappings      pojo字段的mapping集合
     * @return ResultMap
     */
    static ResultMap buildResultMap(Configuration configuration, String id, Class<?> resultType,
                                    List<ResultMapping> mappings) {
        return new ResultMap.Builder(configuration, id, resultType, mappings).build();
    }
}
