package org.malred.annotations.table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// 表字段不能为空
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNull {
}
