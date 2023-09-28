package org.malred.utils;

import org.malred.annotations.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class Operate {
    //通用的更新数据库的方法：insert,update,delete 语句时
    public static int update(String sql) throws SQLException {
        //1、获取连接
        Connection conn = JDBCUtils.getConn();
        //2、获取 Statement 对象，这个对象是用来给服务器传 sql 并执行 sql
        Statement st = conn.createStatement();
        //3、执行 sql
        int len = st.executeUpdate(sql);
        //4、释放资源
        JDBCUtils.close(conn, (PreparedStatement) st);
        return len;
    }

    // 通用的更新数据库的方法：insert,update,delete 语句，允许 sql 带?
    public static int update(String sql, Object... args) throws SQLException {
        Connection conn = JDBCUtils.getConn();
        int len = update(conn, sql, args);
        JDBCUtils.close(conn);
        return len;
    }

    // 通用的更新数据库的方法：insert,update,delete 语句，允许 sql 带?
    public static int update(Connection conn, String sql, Object... args) throws SQLException {
        //2、获取 PreparedStatement 对象，这个对象是用来 sql 进行预编译
        PreparedStatement pst = conn.prepareStatement(sql);
        //3、设置 sql 中的?
        if (args != null && args.length > 0) {
            //数组的下标是从 0 开始，？的编号是 1 开始
            for (int i = 0; i < args.length; i++) {
                pst.setObject(i + 1, args[i]);
            }
        }
        //4、执行 sql
        int len = pst.executeUpdate();
        //5、释放资源
        JDBCUtils.close(pst);
        return len;
    }

    //通用的查询方法之一：查询一行，即一个对象

    /**
     * 执行查询操作的 SQL 语句，SQL 可以带参数(?)
     *
     * @param clazz Class 查询的结果需要封装的实体的 Class 类型，例如：学生 Student，商品 Goods,订单 Order
     * @param sql   String 执行查询操作的 SQL 语句
     * @param args  Object... 对应的每个?设置的值，顺序要与?对应
     * @return T 封装了查询结果的实体
     * @throws Exception
     */
    public static <T> T get(Class<T> clazz, String sql, Object... args) throws Exception {
        //1、注册驱动
        //2、获取连接
        Connection conn = JDBCUtils.getConn();
        //3、对 sql 进行预编译
        PreparedStatement pst = conn.prepareStatement(sql);
        //4、设置？
        if (args != null && args.length > 0) {
            //数组的下标是从 0 开始，？的编号是 1 开始
            for (int i = 0; i < args.length; i++) {
                pst.setObject(i + 1, args[i]);
            }
        }
        //5、查询
        ResultSet rs = pst.executeQuery();
        //获取查询的结果集的元数据信息
        ResultSetMetaData rsmd = rs.getMetaData();
        //这是查询的结果集中，一共有几列
        int count = rsmd.getColumnCount();
        T t = clazz.newInstance();//要求这个 Javabean 类型必须有无参构造
        while (rs.next()) {
            /*
             * 问题？
             * （1）sql 语句中查询了几列，每一列是什么属性
             * （2）怎么把这个值设置到 Javabean 的属性中
             */
            //循环每一行有几列
            for (int i = 0; i < count; i++) {
                //第几列的名称
                // String columnName = rsmd.getColumnName(i+1);
                //如果 sql 中没有取别名，那么就是列名，如果有别名，返回的是别名
                String fieldName = rsmd.getColumnLabel(i + 1);
                //该列的值
                // Object value = rs.getObject(columnName);
                Object value = rs.getObject(fieldName);
                //设置 obj 对象的某个属性中
                Field field = clazz.getDeclaredField(fieldName);//JavaBean 的属性名
                field.setAccessible(true);
                field.set(t, value);
            }
        }
        //5、释放资源
        JDBCUtils.close(conn, pst, rs);
        return t;
    }

    //通用的查询方法之二：查询多行，即多个对象
    //Class<T> clazz：用来创建实例对象，获取对象的属性，并设置属性值

    /**
     * 执行查询操作的 SQL 语句，SQL 可以带参数(?)
     *
     * @param clazz Class 查询的结果需要封装的实体的 Class 类型，例如：学生 Student，商品 Goods,订单 Order
     * @param sql   String 执行查询操作的 SQL 语句
     * @param args  Object... 对应的每个?设置的值，顺序要与?对应
     * @return ArrayList<T> 封装了查询结果的集合
     * @throws Exception
     */
    public static <T> ArrayList<T> getList(Class<T> clazz, String sql, Object... args) throws Exception {
        //1、注册驱动，不用了
        //2、获取连接
        Connection conn = JDBCUtils.getConn();
        //3、对 sql 进行预编译
        PreparedStatement pst = conn.prepareStatement(sql);
        //4、对？进行设置值
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                pst.setObject(i + 1, args[i]);
            }
        }
        //5、执行 sql
        ResultSet rs = pst.executeQuery();
        //获取结果集的元数据
        ResultSetMetaData metaData = rs.getMetaData();
        //获取结果中总列数
        int count = metaData.getColumnCount();
        //创建集合对象
        ArrayList<T> list = new ArrayList<T>();
        while (rs.next()) {//遍历的行
            //1、每一行是一个对象
            T obj = clazz.newInstance();
            //2、每一行有很多列
            //for 的作用是为 obj 对象的每一个属性设置值
            for (int i = 0; i < count; i++) {
                //(1)每一列的名称
                String fieldName = metaData.getColumnLabel(i + 1);//获取第几列的名称，如果有别名获取别名，如果没有别名获取列名
                //(2)每一列的值
                Object value = rs.getObject(i + 1);//获取第几列的值
                //(3)获取属性对象
                Field field = clazz.getDeclaredField(fieldName);
                //(4)设置可见性
                field.setAccessible(true);
                //(5)设置属性值
                field.set(obj, value);
            }
            //3、把 obj 对象放到集合中
            list.add(obj);
        }
        //6、释放资源
        JDBCUtils.close(conn, pst, rs);
        //7、返回结果
        return list;
    }

    //通用的查询方法之三：查询单个值
    //单值：select max(salary) from employee; 一行一列
    //select count(*) from t_goods; 一共几件商品
    public static Object getValue(String sql, Object... args) throws Exception {
        //2、获取连接
        Connection conn = JDBCUtils.getConn();
        //3、对 sql 进行预编译
        PreparedStatement pst = conn.prepareStatement(sql);
        //4、对？进行设置值
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                pst.setObject(i + 1, args[i]);
            }
        }
        //5、执行 sql
        ResultSet rs = pst.executeQuery();
        Object value = null;
        if (rs.next()) {//一行
            value = rs.getObject(1);//一列
        }
        //6、释放资源
        JDBCUtils.close(conn, pst, rs);
        return value;
    }

    /**
     * 代理类的方法的实现
     *
     * @param mapperClass 被代理的接口
     * @param type        封装返回的类型(与数据库表对应的实体类)
     * @param <T>
     * @return
     */
    public static <T> T getMapper(Class<?> mapperClass, Class<?> type) {
        // 使用JDK动态代理为Dao接口生成代理对象,并返回
        Object proxyInstance = Proxy.newProxyInstance(
                Operate.class.getClassLoader(),
                new Class[]{mapperClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 拿到表名
                        Repository annotation = mapperClass.getAnnotation(Repository.class);
                        String tbName = annotation.value();
                        // 拼装sql
//                        String sql = (String) Common.DefaultCRUDSql.get(method.getName());
//                        System.out.println(sql);
                        String sql = "";
                        // 默认CRUD接口的代理方法
                        switch (method.getName()) {
                            case "findAll": {
                                sql = SqlBuilder.build()
                                        .tbName(tbName)
                                        .select()
                                        .sql();
                                System.out.println("执行findAll方法");
                                System.out.println("当前执行的sql语句: " + sql);
                                return Operate.getList(type, sql);
                            }
                            case "findById": {
                                sql = SqlBuilder.build()
                                        .tbName(tbName)
                                        .select()
                                        .where("id", SqlCompareIdentity.EQ)
                                        .sql();
                                System.out.println("执行findById方法");
                                System.out.println("当前执行的sql语句: " + sql);
                                return Operate.get(type, sql, args);
                            }
                            case "update": {
                                ParseClazz parseClazz = parseObjectArgs(args);

                                String[] paramNames = new String[parseClazz.params.keySet().size()];
                                for (int i = 0; i < parseClazz.params.keySet().toArray().length; i++) {
                                    paramNames[i] = parseClazz.params.keySet().toArray()[i].toString();
                                }
                                sql = SqlBuilder.build()
                                        .update(tbName, paramNames)
                                        .where(parseClazz.idName, SqlCompareIdentity.EQ)
                                        .sql();

                                System.out.println("执行update方法");
                                System.out.println("当前执行的sql语句: " + sql);

                                String[] paramVals = new String[parseClazz.params.values().size() + 1];
                                for (int i = 0; i < parseClazz.params.values().toArray().length; i++) {
                                    paramVals[i] = parseClazz.params.values().toArray()[i].toString();
//                                    System.out.println(paramVals[i]);
                                }
                                paramVals[paramVals.length - 1] = parseClazz.idVal.toString();
                                return Operate.update(sql, paramVals);
//                                return 1;
                            }
                            case "insert": {
                                ParseClazz parseClazz = parseObjectArgs(args);
                                String[] paramNames = new String[parseClazz.params.keySet().size()];
                                for (int i = 0; i < parseClazz.params.keySet().toArray().length; i++) {
                                    paramNames[i] = parseClazz.params.keySet().toArray()[i].toString();
                                }

                                sql = SqlBuilder.build()
                                        .tbName(tbName)
                                        .insert(paramNames)
                                        .sql();
                                System.out.println("执行insert方法");
                                System.out.println("当前执行的sql语句: " + sql);

                                String[] paramVals = new String[parseClazz.params.values().size()];
                                for (int i = 0; i < parseClazz.params.values().toArray().length; i++) {
                                    paramVals[i] = parseClazz.params.values().toArray()[i].toString();
//                                    System.out.println(paramVals[i]);
                                }
                                return update(sql, paramVals);
                            }
                            case "delete": {
                                sql = SqlBuilder.build()
                                        .tbName(tbName)
                                        .delete()
                                        .where("id", SqlCompareIdentity.EQ)
                                        .sql();
                                System.out.println("执行delete方法");
                                System.out.println("当前执行的sql语句: " + sql);
                                return update(sql, args[0]);
                            }
                        }
                        // 如果都不是上面的,就是用户自己定义的
                        if (method.isAnnotationPresent(Select.class)) {
                            Select selectAnno = method.getAnnotation(Select.class);
                            sql = selectAnno.value();
                            // 判断是查询单个还是多个(返回值类型是List之类的吗)
                            // 这里只是简单判断一下
                            Type genericReturnType = method.getGenericReturnType();
                            // 判断是否进行了泛型类型参数化(是否有泛型)
                            if (genericReturnType instanceof ParameterizedType) {
//                            if (x instanceof Collection< ? >){
//                            }
//                            if (x instanceof Map<?,?>){
//                            }
                                return Operate.getList(type, sql, args);
                            }
                            return Operate.get(type, sql, args);
                        }
                        if (method.isAnnotationPresent(Update.class)) {
                            Update anno = method.getAnnotation(Update.class);
                            sql = anno.value();
                            return update(sql, args);
                        }
                        if (method.isAnnotationPresent(Delete.class)) {
                            Delete anno = method.getAnnotation(Delete.class);
                            sql = anno.value();
                            return update(sql, args);
                        }
                        if (method.isAnnotationPresent(Insert.class)) {
                            Insert anno = method.getAnnotation(Insert.class);
                            sql = anno.value();
                            return update(sql, args);
                        }
                        // 返回值
                        return null;
                    }
                });
        return (T) proxyInstance;
    }


    private static ParseClazz parseObjectArgs(Object[] args) throws IllegalAccessException {
        Object arg = args[0];
        Field[] fields = arg.getClass().getDeclaredFields();
        // 被修改的参数名
//                                String[] paramNames = new String[fields.length];
        // 修改后的参数值
//                                Object[] argVals = new Object[fields.length];
        // <被修改的参数名,修改后的参数值>
        HashMap<String, Object> params = new HashMap<String, Object>();
        String idName = "id";
        Object idVal = null;
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
//                                    System.out.println(fields[i].getName());
//                                    paramNames[i] = fields[i].getName();
//                                    System.out.println(fields[i].get(args[0]));
            // 从args[0] -> 这里是传入实体类(update,insert)
//                                    argVals[i] = fields[i].get(args[0]);
            if (fields[i].getName().contains("id")) {
                idName = fields[i].getName();
                idVal = fields[i].get(args[0]);
                continue;
            }
            if (fields[i].get(args[0]) != null) {
                params.put(fields[i].getName(), fields[i].get(args[0]));
            }
        }
        return new ParseClazz(params, idName, idVal);
    }

    static class ParseClazz {
        HashMap<String, Object> params;
        String idName;
        Object idVal;

        public ParseClazz(HashMap<String, Object> params, String idName, Object idVal) {
            this.params = params;
            this.idName = idName;
            this.idVal = idVal;
        }
    }
}