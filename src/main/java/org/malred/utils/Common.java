package org.malred.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Common {
    public static final String[] Compares = new String[]{
            ">", ">=", "<", "<=", "=", "!=", "like", "in"
    };
    public static final String[] JOIN_TYPE = new String[]{
            "left", "right", "full", "inner", "left outer", "right outer"
    };

    //    public static final Map DefaultCRUDSql = new HashMap<String, String>() {{
//        put("findAll", "select * from #tbName");
//        put("findById", "select * from #tbName where id=?");
//        put("remove", "update #tbName set version=? where id=?");
//        put("delete", "delete from #tbName where id=?");
//        put("update", "update #tbName set ");
//        put("insert", "insert into #tbName values ()");
//    }};

    // java date to sql date
    public static java.sql.Date javaDateToSqlDate(java.util.Date date) {
//        Date utilDate = new Date();//util.Date
//        System.out.println("utilDate : " + utilDate);
        //util.Date转sql.Date
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
//        System.out.println("sqlDate : " + sqlDate);
        return sqlDate;
    }

    // 获取当前时间戳 sql timestamp
    public static java.sql.Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }

    public static Date localDateToDate(LocalDateTime time) {
//        LocalDateTime localDateTime = LocalDateTime.parse("2019-11-15T13:15:30");
        Instant instant = time.atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);
        return date;
    }

    public static java.util.Date sqlDateToJavaDate(java.sql.Date sqlDate) {
        java.util.Date javaDate = null;
        if (sqlDate != null) {
            javaDate = new Date(sqlDate.getTime());
        }
        return javaDate;
    }
}
