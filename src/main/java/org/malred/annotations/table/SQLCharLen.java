package org.malred.annotations.table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// 建表时指定varchar长度
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLCharLen {
    int value();
}
