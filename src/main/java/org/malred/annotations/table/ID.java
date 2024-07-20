package org.malred.annotations.table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// 表示该字段是key
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {
}
