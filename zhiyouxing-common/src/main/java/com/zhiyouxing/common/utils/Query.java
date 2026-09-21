package com.zhiyouxing.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.LinkedHashMap;
import java.util.Map;

public class Query<T> extends LinkedHashMap<String, Object> {

    private final Page<T> page;

    public Query(Map<String, Object> params) {
        this.putAll(params);
        long current = firstLong(params, "page", "currPage", 1L);
        long size = firstLong(params, "limit", "pageSize", 10L);
        this.page = new Page<>(current, size);
    }

    public Page<T> getPage() {
        return page;
    }

    private static long firstLong(Map<String, Object> params, String primary, String fallback, long defaultValue) {
        Object value = params.get(primary) != null ? params.get(primary) : params.get(fallback);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
