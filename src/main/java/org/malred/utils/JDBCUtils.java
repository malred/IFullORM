package org.malred.utils;


import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
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

    public static Connection getConn() {            // 创建连接池
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        try {
            comboPooledDataSource.setDriverClass(driverName);
            comboPooledDataSource.setJdbcUrl(url);
            comboPooledDataSource.setUser(user);
            comboPooledDataSource.setPassword(password);
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
}
