package com.zhiyouxing.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把请求参数拼装成 MyBatis-Plus 查询条件的工具方法。
 */
public class MPUtil {

    private MPUtil() {
    }

    /**
     * 实体中非空字段参与查询：字符串走 like，其余走等值。
     */
    public static <T> QueryWrapper<T> likeOrEq(QueryWrapper<T> wrapper, Object bean) {
        if (bean == null) {
            return wrapper;
        }
        for (Field field : fieldsOf(bean.getClass())) {
            Object value = read(field, bean);
            if (value == null) {
                continue;
            }
            if (value instanceof String text) {
                if (text.isEmpty()) {
                    continue;
                }
                wrapper.like(camelToUnderline(field.getName()), text);
            } else {
                wrapper.eq(camelToUnderline(field.getName()), value);
            }
        }
        return wrapper;
    }

    /**
     * 参数中形如 xxxStart/xxxEnd（或 xxx_start/xxx_end）的键转成区间条件。
     */
    public static <T> QueryWrapper<T> between(QueryWrapper<T> wrapper, Map<String, Object> params) {
        if (params == null) {
            return wrapper;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null || value.toString().isEmpty()) {
                continue;
            }
            if (key.endsWith("_start") || key.endsWith("Start")) {
                wrapper.ge(camelToUnderline(trimSuffix(key, "_start", "Start")), value);
            } else if (key.endsWith("_end") || key.endsWith("End")) {
                wrapper.le(camelToUnderline(trimSuffix(key, "_end", "End")), value);
            }
        }
        return wrapper;
    }

    /**
     * 参数中的 sort/order 转成排序条件。
     */
    public static <T> QueryWrapper<T> sort(QueryWrapper<T> wrapper, Map<String, Object> params) {
        if (params == null) {
            return wrapper;
        }
        Object sort = params.get("sort");
        if (sort == null || sort.toString().isEmpty()) {
            return wrapper;
        }
        boolean isAsc = !"desc".equalsIgnoreCase(String.valueOf(params.get("order")));
        wrapper.orderBy(true, isAsc, camelToUnderline(sort.toString()));
        return wrapper;
    }

    /**
     * 实体中非空字段转成「表别名.列名 -> 值」的等值条件，供 Wrapper#allEq 使用。
     */
    public static Map<String, Object> allEQMapPre(Object bean, String pre) {
        Map<String, Object> map = new HashMap<>();
        if (bean == null) {
            return map;
        }
        String prefix = (pre == null || pre.isEmpty()) ? "" : pre + ".";
        for (Field field : fieldsOf(bean.getClass())) {
            Object value = read(field, bean);
            if (value == null || (value instanceof String text && text.isEmpty())) {
                continue;
            }
            map.put(prefix + camelToUnderline(field.getName()), value);
        }
        return map;
    }

    private static List<Field> fieldsOf(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private static Object read(Field field, Object bean) {
        try {
            field.setAccessible(true);
            return field.get(bean);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static String trimSuffix(String key, String underscoreSuffix, String camelSuffix) {
        return key.endsWith(underscoreSuffix)
                ? key.substring(0, key.length() - underscoreSuffix.length())
                : key.substring(0, key.length() - camelSuffix.length());
    }

    private static String camelToUnderline(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
