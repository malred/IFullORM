package org.malred.utils;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.malred.annotations.table.*;

import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.sql.*;

public class JDBCUtils {
    static String url;
    // defaultUrl+dbName -> url
    static String defaultUrl = "jdbc:mysql://localhost:3306/";
    static String dbName;
    static String driverName;
    static String defaultDriverName = "com.mysql.cj.jdbc.Driver";
    static String user;
    static String password;
    static ComboPooledDataSource comboPooledDataSource;
    static String schema;

    public static void setDataSource(String url, String driverName, String user, String password) {
        JDBCUtils.url = url;
        JDBCUtils.driverName = driverName;
        JDBCUtils.user = user;
        JDBCUtils.password = password;
    }

    public static void setDataSource(String dbName, String user, String password) {
        JDBCUtils.url = defaultUrl + dbName;
        JDBCUtils.driverName = defaultDriverName;
        JDBCUtils.user = user;
        JDBCUtils.password = password;
    }

    public static void setUrl(String url) {
        JDBCUtils.url = url;
    }

    public static void setDriverName(String driverName) {
        JDBCUtils.driverName = driverName;
    }

    public static void setUser(String user) {
        JDBCUtils.user = user;
    }

    public static void setPassword(String password) {
        JDBCUtils.password = password;
    }

    public static Connection getConn() {
        try {
            if (comboPooledDataSource == null) {
                comboPooledDataSource = new ComboPooledDataSource();
                comboPooledDataSource.setDriverClass(driverName);
                comboPooledDataSource.setJdbcUrl(url);
                comboPooledDataSource.setUser(user);
                comboPooledDataSource.setPassword(password);
            }
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
        try {
            // 四要素 -> 让用户传
//            String url = "jdbc:mysql://localhost:3307/mybatis";
//            String user = "root";
//            String password = "123456";
//            String driverName = "com.mysql.cj.jdbc.Driver";
            //实例化驱动
//            Class.forName(driverName);
            //获取连接
//            Connection conn = DriverManager.getConnection(url, user, password);
            return comboPooledDataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void close(Connection conn, PreparedStatement ps) {
        try {
            if (conn != null) conn.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Connection conn) {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(PreparedStatement ps) {
        try {
            if (ps != null) ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (conn != null) conn.close();
            if (ps != null) ps.close();
            if (rs != null) rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断表结构是否存在
     *
     * @param tableName 所判断的表名
     * @return
     */
    public static boolean ValidateTableExist(String tableName) throws SQLException {
        boolean flag = false;
        try {
            Connection conn = JDBCUtils.getConn();
            DatabaseMetaData meta = conn.getMetaData();
            String type[] = {"TABLE"};
            ResultSet rs = meta.getTables(null, conn.getCatalog(), tableName, type);
            flag = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 转换java类型到sql类型
     *
     * @param type
     * @return
     */
    public static String getSqlType(String type) {
        // map java type to sql type
        String sqlType = "";
        switch (type) {
            case "int":
                sqlType = "INT";
                break;
            case "java.lang.Long":
                sqlType = "BIGINT";
                break;
            case "java.lang.String":
                sqlType = "VARCHAR";
                break;
                /*
                MySQL没有内置的布尔类型。 但是它使用TINYINT(1)。
                为了更方便，MySQL提供BOOLEAN或BOOL作为TINYINT(1)的同义词。
                在MySQL中，0被认为是false，非零值被认为是true。
                要使用布尔文本，可以使用常量TRUE和FALSE来分别计算为1和0
                 */
            case "java.lang.Boolean":
                sqlType = "BOOLEAN";
                break;
            case "boolean":
                sqlType = "BOOLEAN";
                break;
            case "java.util.Date":
                sqlType = "DATETIME";
//                sqlType = "TIMESTAMP";
                break;
        }
        return sqlType;
    }

    /**
     * 根据注解构建建表sql
     *
     * @param name
     * @param aClass
     * @return
     * @throws Exception
     */
    public static String annoSqlPush(String name, Class aClass) throws Exception {
        String sql = "";

        Field field = aClass.getDeclaredField(name);
        System.out.println("field: " + field.getName());
        String typeName = field.getType().getTypeName();

        field.setAccessible(true);
        // 是不是主键
        if (field.isAnnotationPresent(ID.class)) {
            System.out.println(name + "是主键");
            sql += " PRIMARY KEY ";
            // 是否自增
            if (
                    field.isAnnotationPresent(AutoIncrement.class) &&
                            (
                                    getSqlType(typeName).equals("INT") ||
                                            getSqlType(typeName).equals("BIGINT")
                            )
            )
                sql += " AUTO_INCREMENT";
        }
        if (field.isAnnotationPresent(SQLCharLen.class)) {
            sql += "(" + field.getAnnotation(SQLCharLen.class).value() + ")";
        } else {
            if (getSqlType(typeName).equals("VARCHAR")) {
                sql += "(255)";
            }
        }
        if (field.isAnnotationPresent(NotNull.class)) {
            sql += " NOT NULL ";
        }
        // 有没有默认值
        if (field.isAnnotationPresent(SQLDefault.class)) {
            sql += " DEFAULT '" + field.getAnnotation(SQLDefault.class).value() + "'";
        }
        // 如果是时间类型
//        if (getSqlType(typeName).equals("TIMESTAMP")) {
        if (getSqlType(typeName).equals("DATETIME")) {
            // 指定为当前时间
            if (field.isAnnotationPresent(SQLDateNow.class)) {
                sql += " DEFAULT CURRENT_TIMESTAMP() ";
            }
        }
        return sql;
    }

    // 当前的数据库名
    public static void setSchema(String name) {
        schema = name;
    }
}
