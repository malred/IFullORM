package org.malred.annotations.table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// 表示主键自增(仅当主键是int类型)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoIncrement {
}
