package org.malred.utils;

import java.util.HashMap;
import java.util.Map;

public class Common {
    public static final String[] Compares = new String[]{
            ">", ">=", "<", "<=", "=", "!=", "like", "in"
    };
//    public static final Map DefaultCRUDSql = new HashMap<String, String>() {{
//        put("findAll", "select * from #tbName");
//        put("findById", "select * from #tbName where id=?");
//        put("remove", "update #tbName set version=? where id=?");
//        put("delete", "delete from #tbName where id=?");
//        put("update", "update #tbName set ");
//        put("insert", "insert into #tbName values ()");
//    }};
}
