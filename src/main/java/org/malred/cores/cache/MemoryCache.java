package org.malred.cores.cache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache implements DefaultCache {
    // 缓存计数器,到?刷新
    public static Map<String, Integer> selectCounts = new HashMap<>();
    // 缓存select
    public static Map<String, Object> selectCaches = new HashMap<>();

    @Override
    public void reset() {
        selectCaches = new HashMap<>();
        // 重新计数
        selectCounts = new HashMap<>();
    }

    @Override
    public int getCount(String key) {
        return selectCounts.get(key);
    }

    @Override
    public void cacheTo(String key, Object obj) {
        selectCaches.put(key, obj);
    }

    @Override
    public Object getCache(String key) {
        return selectCaches.get(key);
    }

    @Override
    // 计数加1
    public void count(String key) {
        Integer integer = selectCounts.get(key);
        if (integer == null) {
            selectCounts.put(key, 0);
            return;
        }
        selectCounts.put(key, integer + 1);
    }
}
