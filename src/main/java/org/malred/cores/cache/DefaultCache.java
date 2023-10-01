package org.malred.cores.cache;

public interface DefaultCache {
    // 获取当前计数
    int getCount(String key);

    // 缓存
    public void cacheTo(String key, Object obj);

    // 获取缓存
    public Object getCache(String key);

    // 重置
    public void reset();

    // 计数加1
    void count(String key);
}
