package org.malred.cores.builder.mysql;

import org.malred.utils.Common;
import org.malred.utils.SqlCompareIdentity;
import org.malred.utils.SqlJoinType;

public class MysqlBuilder extends MysqlBuilderImpl {
    String sql;
    String tbName;// 表名
    String joinTb;// 连接的表的名称

    private MysqlBuilder() {
    }

    public static MysqlBuilder build() {
        return new MysqlBuilder();
    }

    public String sql() {
        return this.sql;
    }

    // 自定义基础sql
    public MysqlBuilder base(String U_sql) {
        this.sql = U_sql;
        return this;
    }

    public MysqlBuilder tbName(String tbName) {
        this.tbName = tbName;
        return this;
    }

    public MysqlBuilder select(String tbName, String... columns) {
        this.sql = "select ";
        for (int i = 0; i < columns.length; i++) {
            if (i == columns.length - 1) {
                this.sql += columns[i] + " ";
                break;
            }
            this.sql += columns[i] + ", ";
        }
        this.sql += " from " + tbName;
        return this;
    }

    public MysqlBuilder select(String[] columns) {
        this.sql = "select ";
        for (int i = 0; i < columns.length; i++) {
            if (i == columns.length - 1) {
                this.sql += columns[i] + " ";
                break;
            }
            this.sql += columns[i] + ", ";
        }
        this.sql += " from " + tbName;
        return this;
    }

    public MysqlBuilder select(String tbName) {
        this.sql = "select * from " + tbName;
        return this;
    }

    public MysqlBuilder select() {
        this.sql = "select * from " + tbName;
        return this;
    }

    public MysqlBuilder join(SqlJoinType type, String joinTb) {
        this.joinTb = joinTb;
        sql += " " + Common.JOIN_TYPE[type.ordinal()] + " join " + joinTb;
        return this;
    }

    public MysqlBuilder on(String in_column, SqlCompareIdentity identity, String out_column) {
        sql += " on " + joinTb + "." + in_column +
                Common.Compares[identity.ordinal()]
                + tbName + "." + out_column;
        return this;
    }


    public MysqlBuilder count(String tbName) {
        this.sql = "select count(*) from " + tbName;
        return this;
    }

    public MysqlBuilder count() {
        this.sql = "select count(*) from " + tbName;
        return this;
    }

    public MysqlBuilder where(String column, SqlCompareIdentity join) {
        if (!sql.contains("where")) {
            this.sql += " where " + column + Common.Compares[join.ordinal()] + " ? ";
            return this;
        }
        this.sql += " and " + column + Common.Compares[join.ordinal()] + "? ";
        return this;
    }

    public MysqlBuilder update(String tbName, String... columns) {
        this.sql = "update " + tbName + " set ";
        for (int i = 0; i < columns.length; i++) {
            if (i == columns.length - 1) {
                this.sql += columns[i] + "=? ";
                break;
            }
            this.sql += columns[i] + "=?,";
        }
        return this;
    }

    public MysqlBuilder update(String[] columns) {
        this.sql = "update " + tbName + " set ";
        for (int i = 0; i < columns.length; i++) {
            if (i == columns.length - 1) {
                this.sql += columns[i] + "=? ";
                break;
            }
            this.sql += columns[i] + "=?,";
        }
        return this;
    }

    // 用于设置update
    public MysqlBuilder set(String column) {
        this.sql += " " + column + "=?";
        return this;
    }

    // 用于设置update
    public MysqlBuilder comma() {
        this.sql += ", ";
        return this;
    }

    public MysqlBuilder insert(String tbName, String... params) {
        sql = "insert into " + tbName;
        sql += "(";
        for (int i = 0; i < params.length; i++) {
            if (i == params.length - 1) {
                sql += params[i] + ") ";
                break;
            }
            sql += params[i] + ",";
        }
        sql += "values (";
        for (int i = 0; i < params.length; i++) {
            if (i == params.length - 1) {
                sql += "?)";
                break;
            }
            sql += "?,";
        }
        return this;
    }

    public MysqlBuilder insert(String[] params) {
        sql = "insert into " + tbName;
        sql += "(";
        for (int i = 0; i < params.length; i++) {
            if (i == params.length - 1) {
                sql += params[i] + ") ";
                break;
            }
            sql += params[i] + ",";
        }
        sql += "values (";
        for (int i = 0; i < params.length; i++) {
            if (i == params.length - 1) {
                sql += "?)";
                break;
            }
            sql += "?,";
        }
        return this;
    }

    public MysqlBuilder delete(String tbName) {
        sql = "delete from " + tbName;
        return this;
    }

    public MysqlBuilder delete() {
        sql = "delete from " + tbName;
        return this;
    }
}
