package org.malred.annotations.table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// 指定表字段默认值
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLDefault {
    String value();
}
