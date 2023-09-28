package org.malred.utils;

public class SqlBuilder {
    String sql;
    String tbName;// 表名

    private SqlBuilder() {
    }

    public static SqlBuilder build() {
        return new SqlBuilder();
    }

    public String sql() {
        return this.sql;
    }

    // 自定义基础sql
    public SqlBuilder base(String U_sql) {
        this.sql = U_sql;
        return this;
    }

    public SqlBuilder tbName(String tbName) {
        this.tbName = tbName;
        return this;
    }

    public SqlBuilder select(String tbName, String... columns) {
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

    public SqlBuilder select(String[] columns) {
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

    public SqlBuilder count(String tbName) {
        this.sql = "select count(*) from " + tbName;
        return this;
    }

    public SqlBuilder count() {
        this.sql = "select count(*) from " + tbName;
        return this;
    }

    public SqlBuilder select(String tbName) {
        this.sql = "select * from " + tbName;
        return this;
    }

    public SqlBuilder select() {
        this.sql = "select * from " + tbName;
        return this;
    }

    public SqlBuilder update(String tbName, String... columns) {
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

    public SqlBuilder update(String[] columns) {
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

    public SqlBuilder where(String column, SqlCompareIdentity join) {
        if (!sql.contains("where")) {
            this.sql += " where " + column + Common.Compares[join.ordinal()] + " ? ";
            return this;
        }
        this.sql += " and " + column + Common.Compares[join.ordinal()] + "? ";
        return this;
    }

    public SqlBuilder insert(String tbName, String... params) {
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

    public SqlBuilder insert(String[] params) {
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

    public SqlBuilder delete(String tbName) {
        sql = "delete from " + tbName;
        return this;
    }

    public SqlBuilder delete() {
        sql = "delete from " + tbName;
        return this;
    }
}
