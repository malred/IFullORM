package org.malred.annotations.cache;

import org.malred.enums.CacheType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GlobalCacheType {
    // 缓存类型: redis/memory
    CacheType value();

    // 全局缓存刷新阈值
    int count();
}
