package org.malred.cores.builder;

import org.malred.cores.builder.mysql.MysqlBuilder;

public interface Builder {
    public String sql();

    public MysqlBuilder base(String U_sql);

    public MysqlBuilder tbName(String tbName);
}
