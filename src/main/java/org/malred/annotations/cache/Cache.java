package org.malred.annotations.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    // 是否开启缓存
    boolean useCache() default false;
    // 多少次执行后重新获取数据
    int count() default 7;
}
