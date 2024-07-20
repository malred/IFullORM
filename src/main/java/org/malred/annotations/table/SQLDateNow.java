package org.malred.annotations.table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

// 指定日期默认值为创建字段的时间
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLDateNow {
}
