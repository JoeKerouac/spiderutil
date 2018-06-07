package com.joe.spider.util.db;

import com.joe.utils.common.BeanUtils;
import com.joe.utils.common.StringUtils;
import com.joe.utils.common.Tools;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultMapping辅助生成工具
 *
 * @author joe
 * @version 2018.06.06 18:33
 */
@Slf4j
public class ResultMappingHelper {
    /**
     * 构建ResultMap
     *
     * @param resultType    pojo类
     * @param configuration mybatis的configuration
     * @return pojo对应的ResultMap，如果pojo没有带注解ResultMappingDefine或者pojo没有声明任何字段则返回null
     */
    public static ResultMap build(Class<?> resultType, Configuration configuration) {
        log.debug("获取[{}]的ResultMapping", resultType);
        if (!resultType.isAnnotationPresent(ResultMappingDefine.class)) {
            log.warn("类型[{}]不是ResultMapping", resultType.getName());
            return null;
        }


        Field[] fields = BeanUtils.getAllFields(resultType);
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
            if (property == null || StringUtils.isEmpty(property.alias())) {
                name = alias = fieldName;
            } else {
                name = fieldName;
                alias = property.alias();
            }

            ResultMapping.Builder mappingBuilder = new ResultMapping.Builder(configuration, name, alias, field
                    .getType());
            mappings.add(mappingBuilder.build());
        }

        ResultMappingDefine define = resultType.getAnnotation(ResultMappingDefine.class);
        String id = StringUtils.isEmpty(define.id()) ? resultType.getSimpleName() : define.id();
        return new ResultMap.Builder(configuration, id, resultType, mappings).build();
    }
}
