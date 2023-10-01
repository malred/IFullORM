package org.malred.cores;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.malred.annotations.cache.Cache;
import org.malred.annotations.cache.RedisConfig;
import org.malred.annotations.table.Entity;
import org.malred.annotations.table.Repository;
import org.malred.annotations.table.ScanEntity;
import org.malred.annotations.sql.Delete;
import org.malred.annotations.sql.Insert;
import org.malred.annotations.sql.Select;
import org.malred.annotations.sql.Update;
import org.malred.cores.cache.DefaultCache;
import org.malred.annotations.cache.GlobalCacheType;
import org.malred.cores.cache.MemoryCache;
import org.malred.cores.cache.RedisCache;
import org.malred.enums.CacheType;
import org.malred.enums.SqlCompareIdentity;
import org.malred.utils.*;
import org.malred.cores.builder.mysql.MysqlBuilder;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.lang.reflect.*;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Operate {
    // 最大查询次数,到了就重新查询并cache
    static int globalCacheQueryMaxCount = 7;
    // 缓存计数器,到7刷新
//    public static Map<String, Integer> selectCounts = new HashMap<>();
    // 缓存select
//    public static Map<String, Object> selectCaches = new HashMap<>();
    // 缓存类型
    static CacheType globalCacheType;
    // 默认缓存对象
    static DefaultCache cache = new MemoryCache();
    // 用户注解定义的方法的缓存对象
    static DefaultCache annotationMethodCache = new MemoryCache();
    // 生成方法的缓存对象
    static DefaultCache genMethodCache = new MemoryCache();
    // 表名-实体类
    static HashMap<String, Class<?>> entitys = new HashMap<>();
    // 表名 - <gen方法名-属性名>
    static Map<String, Map<String, String>> methodMap = new HashMap<>();
    // 表名 - upt使用的params名
    static Map<String, String[]> paramsMap = new HashMap<>();
    // 用于生成代码的信息
    static List<genClass> genClasses = new ArrayList<>();
    private static boolean isUseCache = false;

    public static int getGlobalCacheQueryMaxCount() {
        return globalCacheQueryMaxCount;
    }
//    private static Operate instance;
//
//    private Operate() {
//    }

    public static void scan(Class<?> clazz) throws IOException, ClassNotFoundException {
        if (clazz.isAnnotationPresent(ScanEntity.class)) {
            ScanEntity annotation = clazz.getAnnotation(ScanEntity.class);
            for (int i = 0; i < annotation.value().length; i++) {
                // 根据路径获取class
                List<Class<?>> classes = LoadUtils.loadClass(annotation.value()[i]);
                // 参数名 - 参数类型
                Map<String, String> paramGenMap = null;
                Map<String, String> uptParams = null;
                // 存入map
                for (Class<?> aClass : classes) {
//                    String simpleName = aClass.getSimpleName();
                    if (aClass.isAnnotationPresent(Entity.class)) {
                        Entity entityAnno = aClass.getAnnotation(Entity.class);
                        entitys.put(entityAnno.value(), aClass);
                        // 生成方法名
//                        Class<?> aClass = entitys.get(tbName);
                        String tbName = entityAnno.value();
                /*
                    id
                    username
                    password
                    gender
                    addr
                    ---------
                    find_by_id_gen
                    find_by_username_gen
                    find_by_password_gen
                    find_by_gender_gen
                    find_by_addr_gen

                    update_by_id_gen
                    update_by_username_gen
                    update_by_password_gen
                    update_by_gender_gen
                    update_by_addr_gen

                    delete_by_id_gen
                    delete_by_username_gen
                    delete_by_password_gen
                    delete_by_gender_gen
                    delete_by_addr_gen
                 */
                        // key方法名 - val属性名
                        Map<String, String> methodList = new HashMap<>();

                        paramGenMap = new HashMap<>();

                        Field[] declaredFields = aClass.getDeclaredFields();
                        String[] params = new String[declaredFields.length];
                        for (int j = 0; j < declaredFields.length; j++) {
                            declaredFields[j].setAccessible(true);
                            String name = declaredFields[j].getName();
                            String methodNameFind = "find_by_" + name + "_gen";
                            String methodNameUpt = "update_by_" + name + "_gen";
                            String methodNameDel = "delete_by_" + name + "_gen";

                            methodList.put(methodNameFind, name);
                            methodList.put(methodNameUpt, name);
                            methodList.put(methodNameDel, name);
                            // 用于update设置set字段
                            params[j] = declaredFields[j].getName();

                            // 用于生成代码
//                            System.out.println(declaredFields[j].getType().getSimpleName());
//                            if (!declaredFields[j].getName().contains("id")) {
                            paramGenMap.put(name, declaredFields[j].getType().getSimpleName());
//                            }
                        }
//                System.out.println(methodList);
                        methodMap.put(tbName, methodList);
                        paramsMap.put(tbName, params);

                        uptParams = new HashMap<>();
                        for (String s : paramGenMap.keySet()) {
                            if (!s.contains("id")) {
                                uptParams.put(s, paramGenMap.get(s));
                            }
                        }
//                        System.out.println(aClass.getTypeName());
//                        System.out.println(aClass.getName());
                        String entityFullName = aClass.getName();
                        String entityName = aClass.getSimpleName();
                        genClass genClass = new genClass(uptParams, entityFullName, entityName);
                        genClasses.add(genClass);
                    }
                }
            }
        }
//        System.out.println(entitys);
        if (clazz.isAnnotationPresent(GlobalCacheType.class)) {
            // 记录全局的缓存类型: memory/redis/...
            GlobalCacheType annotation = clazz.getAnnotation(GlobalCacheType.class);
            globalCacheType = annotation.value();
            switch (globalCacheType) {
                case memory:
                    cache = new MemoryCache();
                    break;
                case redis:
                    String host = "localhost";
                    int port = 6379;
                    if (clazz.isAnnotationPresent(RedisConfig.class)) {
                        RedisConfig redisAnno = clazz.getAnnotation(RedisConfig.class);
                        host = redisAnno.host();
                        port = redisAnno.port();
                    }
//                    Jedis jedis = new Jedis(host, port);
//                    cache = new RedisCache(jedis);
                    cache = new RedisCache(host, port);
                    break;
            }
        } else {
            // 默认为内存cache
            cache = new MemoryCache();
        }
    }

    public static void gen() throws IOException {
        for (int i = 0; i < genClasses.size(); i++) {
            genClass genClass = genClasses.get(i);
            GenUtils.genMethodRepository(genClass.entityName, genClass.entityFullName, genClass.uptParams);
        }
    }

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
     * @param <T>
     * @return
     */
    public static <T> T getMapper(Class<?> mapperClass) {
        // 开始执行的时间
        Instant start = Instant.now();

        // 使用JDK动态代理为Dao接口生成代理对象,并返回
        Object proxyInstance = Proxy.newProxyInstance(Operate.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //获取方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                Class<?> type = null;
                if (genericReturnType instanceof ParameterizedType) {
                    Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                    for (Type parameterType : actualTypeArguments) {
//                        System.out.println(parameterType);
                        type = (Class<?>) parameterType;
                    }
                } else {
                    type = (Class<?>) genericReturnType;
                }
                // 拿到表名
                Repository annotation = mapperClass.getAnnotation(Repository.class);
                String tbName = annotation.value();
                // 拼装sql
//                        String sql = (String) Common.DefaultCRUDSql.get(method.getName());
//                        System.out.println(sql);
                String sql = "";

                String uuid = UUID.randomUUID().toString();

                // 默认CRUD接口的代理方法
                // 默认CRUD接口的代理方法
                switch (method.getName()) {
                    case "findAll": {
//                        System.out.println(isUseCache);
//                        System.out.println(selectCaches.get(tbName + ".findAll"));
                        String cacheName = tbName + ".findAll";
//                        if (isUseCache && selectCaches.get(cacheName) != null
//                                && (selectCounts.get(cacheName) % 7) != 0) {
                        if (isUseCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % globalCacheQueryMaxCount) != 0) {
                            System.out.println(uuid + ": 执行findAll方法");
                            System.out.println(uuid + ": 从缓存中读取");
//                            selectCounts.put(cacheName, selectCounts.get(cacheName) + 1);
                            cache.count(cacheName);
//                            System.out.println(selectCounts.get(cacheName));
//                            return selectCaches.get(cacheName);

                            // 执行结束
                            Instant end = Instant.now();
                            Duration between = Duration.between(start, end);
                            System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());
                            Object res = cache.getCache(cacheName);
//                            if (globalCacheType == CacheType.redis) {
//                                List objs = new ArrayList();
//                                for (Object s : (List) res) {
//                                    GsonBuilder gsonBuilder = new GsonBuilder();
//                                    // 设置日期转换格式
//                                    gsonBuilder.setDateFormat("yyyy-MM--dd");
//                                    Gson gson = gsonBuilder.create();
//                                    //解析对象：第一个参数：待解析的字符串 第二个参数结果数据类型的Class对象
//                                    objs.add(gson.fromJson(s.toString(), type));
//                                }
//                            }
                            return res;
                        }
                        sql = MysqlBuilder.build().tbName(tbName).select().sql();
                        System.out.println(uuid + ": 执行findAll方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);
                        Object obj = Operate.getList(type, sql);

                        // false -> 不存不取; true -> 为null,存,不为null,取
                        // 使用缓存才存入map
                        if (isUseCache) {
                            System.out.println(uuid + ": 缓存查询结果");
//                            selectCounts.put(cacheName, 1);
//                            selectCaches.put(cacheName, obj);
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, obj);
                        }
//                        System.out.println(selectCaches.get(tbName + ".findAll"));

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
                    case "findById": {
                        String cacheName = tbName + ".findById";
//                        if (isUseCache && selectCaches.get(cacheName) != null
//                                && (selectCounts.get(cacheName) % 7) != 0) {
                        if (isUseCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % globalCacheQueryMaxCount) != 0) {
                            System.out.println(uuid + ": 执行findById方法");
                            System.out.println(uuid + ": 从缓存中读取");
                            // 计数+1
//                            selectCounts.put(cacheName, selectCounts.get(cacheName) + 1);
                            cache.count(cacheName);
//                            System.out.println(selectCounts.get(cacheName));
//                            return selectCaches.get(cacheName);

                            Instant end = Instant.now();
                            Duration between = Duration.between(start, end);
                            System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                            return cache.getCache(cacheName);
                        }
                        sql = MysqlBuilder.build().tbName(tbName).select().where("id", SqlCompareIdentity.EQ).sql();
                        System.out.println(uuid + ": 执行findById方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);
                        Object res = Operate.get(type, sql, args);
                        // 使用缓存才存入map
                        if (isUseCache) {
                            System.out.println(uuid + ": 缓存查询结果");
//                            selectCounts.put(cacheName, 1);
//                            selectCaches.put(cacheName, res);
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, res);
                        }

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    case "update": {
                        ParseClazz parseClazz = parseObjectArgs(args);

                        String[] paramNames = new String[parseClazz.params.keySet().size()];
                        for (int i = 0; i < parseClazz.params.keySet().toArray().length; i++) {
                            paramNames[i] = parseClazz.params.keySet().toArray()[i].toString();
                        }
                        sql = MysqlBuilder.build().update(tbName, paramNames).where(parseClazz.idName, SqlCompareIdentity.EQ).sql();

                        System.out.println(uuid + ": 执行update方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);

                        String[] paramVals = new String[parseClazz.params.values().size() + 1];
                        for (int i = 0; i < parseClazz.params.values().toArray().length; i++) {
                            paramVals[i] = parseClazz.params.values().toArray()[i].toString();
//                                    System.out.println(paramVals[i]);
                        }
                        // 拼接上id
                        paramVals[paramVals.length - 1] = parseClazz.idVal.toString();
                        int res = Operate.update(sql, paramVals);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    case "insert": {
                        ParseClazz parseClazz = parseObjectArgs(args);
                        String[] paramNames = new String[parseClazz.params.keySet().size()];
                        for (int i = 0; i < parseClazz.params.keySet().toArray().length; i++) {
                            paramNames[i] = parseClazz.params.keySet().toArray()[i].toString();
                        }

                        sql = MysqlBuilder.build().tbName(tbName).insert(paramNames).sql();
                        System.out.println(uuid + ": 执行insert方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);

                        String[] paramVals = new String[parseClazz.params.values().size()];
                        for (int i = 0; i < parseClazz.params.values().toArray().length; i++) {
                            paramVals[i] = parseClazz.params.values().toArray()[i].toString();
//                                    System.out.println(paramVals[i]);
                        }
                        int res = update(sql, paramVals);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    case "delete": {
                        sql = MysqlBuilder.build().tbName(tbName).delete().where("id", SqlCompareIdentity.EQ).sql();
                        System.out.println(uuid + ": 执行delete方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);
                        int res = update(sql, args[0]);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                }

                // 如果都不是上面的,就是用户自己定义的
                if (method.isAnnotationPresent(Select.class)) {
                    String cacheName = tbName + "." + method.getName();
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Select selectAnno = method.getAnnotation(Select.class);
                    sql = selectAnno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);

                    // 默认为全局的阈值
                    int countToReCache = globalCacheQueryMaxCount;
                    Cache cacheAnno = method.getAnnotation(Cache.class);
                    boolean annoUseCache = false;
                    // 多少次查询后重新缓存
                    if (method.isAnnotationPresent(Cache.class)) {
                        countToReCache = cacheAnno.count();
                        annoUseCache = cacheAnno.useCache();
                    }
                    // 可以注解方法单独开启cache
                    // 也可以是跟随全局一起
                    boolean useCache = isUseCache || annoUseCache;
                    // 从缓存读取
                    if (useCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % countToReCache) != 0) {
                        System.out.println(uuid + ": 从缓存中读取");
                        // 有没有在cache注解定义缓存的地方,如果没有或者和默认的一样,就用默认(全局type)
                        cache.count(cacheName);
                        // 执行结束
                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return cache.getCache(cacheName);
                    }
                    // 判断是查询单个还是多个(返回值类型是List之类的吗)
                    // 这里只是简单判断一下
//                            Type genericReturnType = method.getGenericReturnType();
                    // 判断是否进行了泛型类型参数化(是否有泛型)
                    if (genericReturnType instanceof ParameterizedType) {
//                            if (x instanceof Collection< ? >){
//                            }
//                            if (x instanceof Map<?,?>){
//                            }
                        Object res = Operate.getList(type, sql, args);

                        if (useCache) {
                            System.out.println(uuid + ": 缓存查询结果");
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, res);
                        }

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    Object res = Operate.get(type, sql, args);

                    if (useCache) {
                        System.out.println(uuid + ": 缓存查询结果");
                        cache.count(cacheName);
                        cache.cacheTo(cacheName, res);
                    }

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }
                if (method.isAnnotationPresent(Update.class)) {
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Update anno = method.getAnnotation(Update.class);
                    sql = anno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);
                    int res = update(sql, args);

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }
                if (method.isAnnotationPresent(Delete.class)) {
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Delete anno = method.getAnnotation(Delete.class);
                    sql = anno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);
                    int res = update(sql, args);

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }
                if (method.isAnnotationPresent(Insert.class)) {
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Insert anno = method.getAnnotation(Insert.class);
                    sql = anno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);
                    int res = update(sql, args);

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }

                // 如果不是上面的, 就走我们根据entity创建的方法
                Class<?> aClass = entitys.get(tbName);
                Map<String, String> methodList = methodMap.get(tbName);
                String[] params = paramsMap.get(tbName);

                System.out.println("执行根据实体类字段自动生成的方法: " + method.getName());

                String cacheName = tbName + "." + method.getName();
                if (isUseCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % globalCacheQueryMaxCount) != 0) {
                    System.out.println(uuid + ": 从缓存中读取");
                    cache.count(cacheName);

                    // 执行结束
                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return cache.getCache(cacheName);
                }
                // 方法名和我们指定的被代理的gen方法名一致,就进入代理实现
                if (methodList.containsKey(method.getName())) {
//                    System.out.println(methodList.get(method.getName()));
                    if (method.getName().contains("find")) {
                        sql = MysqlBuilder.build().tbName(tbName).select().where(methodList.get(method.getName()), SqlCompareIdentity.EQ).sql();
                        System.out.println("当前sql: " + sql);
                        Object obj = getList(aClass, sql, args);

                        if (isUseCache) {
                            System.out.println(uuid + ": 缓存查询结果");
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, obj);
                        }

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
                    if (method.getName().contains("update")) {
                        MysqlBuilder builder = MysqlBuilder.build().base("update tb_user set");
                        // update set xxx=?
                        List<String> setParams = new ArrayList<>();
                        for (int i = 0; i < params.length; i++) {
                            // 不等于作为条件的字段
                            if (!params[i].contains("id") && !methodList.get(method.getName()).equals(params[i])) {
                                setParams.add(params[i]);
                            }
                        }
                        for (int i = 0; i < setParams.size(); i++) {
                            if (i == setParams.size() - 1) {
                                // set x=?
                                builder.set(setParams.get(i));
                                break;
                            }
                            // set x=?,
                            builder.set(setParams.get(i)).comma();
                        }
                        sql = builder.where(methodList.get(method.getName()), SqlCompareIdentity.EQ).sql();
                        System.out.println("当前sql: " + sql);
                        int obj = update(sql, args);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
                    if (method.getName().contains("delete")) {
                        sql = MysqlBuilder.build().tbName(tbName).delete().where(methodList.get(method.getName()), SqlCompareIdentity.EQ).sql();
                        System.out.println("当前sql: " + sql);
                        int obj = update(sql, args);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
                }
                // 返回值
                return null;
            }
        });
        return (T) proxyInstance;
    }

    /**
     * 代理类的方法的实现
     *
     * @param mapperClass 被代理的接口
     * @param type1       BaseRepository<T>封装返回的类型(与数据库表对应的实体类)
     * @param <T>
     * @return
     */
    public static <T> T getMapper(Class<?> mapperClass, Class<?> type1) {
        // 使用JDK动态代理为Dao接口生成代理对象,并返回
        Object proxyInstance = Proxy.newProxyInstance(Operate.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 开始执行的时间
                Instant start = Instant.now();

                // 用户定义的方法的返回类型
                Class<?> type = null;
                //获取方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                // 如果是findAll和findById,拿到的是T,会报错
                if (!method.getName().equals("findAll") && !method.getName().equals("findById")) {
                    if (genericReturnType instanceof ParameterizedType) {
                        Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                        for (Type parameterType : actualTypeArguments) {
                            System.out.println(parameterType);
                            type = (Class<?>) parameterType;
                        }
                    } else {
                        type = (Class<?>) genericReturnType;
                    }
                }

                // 拿到表名
                Repository annotation = mapperClass.getAnnotation(Repository.class);
                String tbName = annotation.value();
                // 拼装sql
//                        String sql = (String) Common.DefaultCRUDSql.get(method.getName());
//                        System.out.println(sql);

                String sql = "";

                String uuid = String.valueOf(UUID.randomUUID());

                // 默认CRUD接口的代理方法
                switch (method.getName()) {
                    case "findAll": {
//                        System.out.println(isUseCache);
//                        System.out.println(selectCaches.get(tbName + ".findAll"));
                        String cacheName = tbName + ".findAll";
//                        if (isUseCache && selectCaches.get(cacheName) != null
//                                && (selectCounts.get(cacheName) % 7) != 0) {
                        if (isUseCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % globalCacheQueryMaxCount) != 0) {
                            System.out.println(uuid + ": 执行findAll方法");
                            System.out.println(uuid + ": 从缓存中读取");
//                            selectCounts.put(cacheName, selectCounts.get(cacheName) + 1);

                            cache.count(cacheName);

                            if (globalCacheType == CacheType.redis) {
                                List caches = (List) cache.getCache(cacheName);
                                ArrayList res = new ArrayList();
                                for (Object s : caches) {
                                    GsonBuilder gsonBuilder = new GsonBuilder();
                                    // 设置日期转换格式
                                    gsonBuilder.setDateFormat("yyyy-MM--dd");
                                    Gson gson = gsonBuilder.create();
                                    //解析对象：第一个参数：待解析的字符串 第二个参数结果数据类型的Class对象
                                    res.add(gson.fromJson(s.toString(), type1));
                                }

                                cache.count(cacheName);
//                            System.out.println(selectCounts.get(cacheName));
//                            return selectCaches.get(cacheName);

                                // 执行结束
                                Instant end = Instant.now();
                                Duration between = Duration.between(start, end);
                                System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                                return res;
                            }

                            Object res = cache.getCache(cacheName);

                            // 执行结束
                            Instant end = Instant.now();
                            Duration between = Duration.between(start, end);
                            System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                            return res;
                        }

                        sql = MysqlBuilder.build().tbName(tbName).select().sql();
                        System.out.println(uuid + ": 执行findAll方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);
                        Object obj = Operate.getList(type1, sql);

                        // false -> 不存不取; true -> 为null,存,不为null,取
                        // 使用缓存才存入map
                        if (isUseCache) {
                            System.out.println(uuid + ": 缓存查询结果");
//                            selectCounts.put(cacheName, 1);
//                            selectCaches.put(cacheName, obj);
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, obj);
                        }

                        cache.count(cacheName);
//                        System.out.println(selectCaches.get(tbName + ".findAll"));

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
                    case "findById": {
                        String cacheName = tbName + ".findById";
//                        if (isUseCache && selectCaches.get(cacheName) != null
//                                && (selectCounts.get(cacheName) % 7) != 0) {
                        if (isUseCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % globalCacheQueryMaxCount) != 0) {
                            System.out.println(uuid + ": 执行findById方法");
                            System.out.println(uuid + ": 从缓存中读取");

                            cache.count(cacheName);

                            if (globalCacheType == CacheType.redis) {
                                GsonBuilder gsonBuilder = new GsonBuilder();
                                // 设置日期转换格式
                                gsonBuilder.setDateFormat("yyyy-MM--dd");
                                Gson gson = gsonBuilder.create();
                                List<String> cache1 = (List<String>) cache.getCache(cacheName);

                                Instant end = Instant.now();
                                Duration between = Duration.between(start, end);
                                System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                                return gson.fromJson(cache1.get(0).toString(), type1);
                            }

                            Instant end = Instant.now();
                            Duration between = Duration.between(start, end);
                            System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                            return cache.getCache(cacheName);
                        }

                        sql = MysqlBuilder.build().tbName(tbName).select().where("id", SqlCompareIdentity.EQ).sql();
                        System.out.println(uuid + ": 执行findById方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);
                        Object res = Operate.get(type1, sql, args);

                        // 使用缓存才存入map
                        if (isUseCache) {
                            System.out.println(uuid + ": 缓存查询结果");
//                            selectCounts.put(cacheName, 1);
//                            selectCaches.put(cacheName, res);
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, res);
                        }
                        cache.count(cacheName);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    case "update": {
                        ParseClazz parseClazz = parseObjectArgs(args);

                        String[] paramNames = new String[parseClazz.params.keySet().size()];
                        for (int i = 0; i < parseClazz.params.keySet().toArray().length; i++) {
                            paramNames[i] = parseClazz.params.keySet().toArray()[i].toString();
                        }
                        sql = MysqlBuilder.build().update(tbName, paramNames).where(parseClazz.idName, SqlCompareIdentity.EQ).sql();

                        System.out.println(uuid + ": 执行update方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);

                        String[] paramVals = new String[parseClazz.params.values().size() + 1];
                        for (int i = 0; i < parseClazz.params.values().toArray().length; i++) {
                            paramVals[i] = parseClazz.params.values().toArray()[i].toString();
//                                    System.out.println(paramVals[i]);
                        }
                        // 拼接上id
                        paramVals[paramVals.length - 1] = parseClazz.idVal.toString();
                        int res = Operate.update(sql, paramVals);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    case "insert": {
                        ParseClazz parseClazz = parseObjectArgs(args);
                        String[] paramNames = new String[parseClazz.params.keySet().size()];
                        for (int i = 0; i < parseClazz.params.keySet().toArray().length; i++) {
                            paramNames[i] = parseClazz.params.keySet().toArray()[i].toString();
                        }

                        sql = MysqlBuilder.build().tbName(tbName).insert(paramNames).sql();
                        System.out.println(uuid + ": 执行insert方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);

                        String[] paramVals = new String[parseClazz.params.values().size()];
                        for (int i = 0; i < parseClazz.params.values().toArray().length; i++) {
                            paramVals[i] = parseClazz.params.values().toArray()[i].toString();
//                                    System.out.println(paramVals[i]);
                        }
                        int res = update(sql, paramVals);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    case "delete": {
                        sql = MysqlBuilder.build().tbName(tbName).delete().where("id", SqlCompareIdentity.EQ).sql();
                        System.out.println(uuid + ": 执行delete方法");
                        System.out.println(uuid + ": 当前执行的sql语句: " + sql);
                        int res = update(sql, args[0]);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                }

                // 如果都不是上面的,就是用户自己定义的
                if (method.isAnnotationPresent(Select.class)) {
                    String cacheName = tbName + "." + method.getName();
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Select selectAnno = method.getAnnotation(Select.class);
                    sql = selectAnno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);

                    // 默认为全局的阈值
                    int countToReCache = globalCacheQueryMaxCount;
                    Cache cacheAnno = method.getAnnotation(Cache.class);
                    boolean annoUseCache = false;
                    // 多少次查询后重新缓存
                    if (method.isAnnotationPresent(Cache.class)) {
                        countToReCache = cacheAnno.count();
                        annoUseCache = cacheAnno.useCache();
                    }
                    // 可以注解方法单独开启cache
                    // 也可以是跟随全局一起
                    boolean useCache = isUseCache || annoUseCache;
                    // 从缓存读取
                    if (useCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % countToReCache) != 0) {
                        System.out.println(uuid + ": 从缓存中读取");
                        // 有没有在cache注解定义缓存的地方,如果没有或者和默认的一样,就用默认(全局type)
                        cache.count(cacheName);

                        if (globalCacheType == CacheType.redis) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            // 设置日期转换格式
                            gsonBuilder.setDateFormat("yyyy-MM--dd");
                            Gson gson = gsonBuilder.create();

                            List caches = (List) cache.getCache(cacheName);
                            ArrayList res = new ArrayList();

                            if (caches.size() != 1) {
                                for (Object s : caches) {
                                    //解析对象：第一个参数：待解析的字符串 第二个参数结果数据类型的Class对象
                                    res.add(gson.fromJson(s.toString(), type));
                                }
                            } else {
                                // 执行结束
                                Instant end = Instant.now();
                                Duration between = Duration.between(start, end);
                                System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                                return gson.fromJson(caches.get(0).toString(), type);
                            }

                            // 执行结束
                            Instant end = Instant.now();
                            Duration between = Duration.between(start, end);
                            System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                            return res;
                        }

                        Object res = cache.getCache(cacheName);

                        // 执行结束
                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }
                    // 判断是查询单个还是多个(返回值类型是List之类的吗)
                    // 这里只是简单判断一下
//                            Type genericReturnType = method.getGenericReturnType();
                    // 判断是否进行了泛型类型参数化(是否有泛型)
                    if (genericReturnType instanceof ParameterizedType) {
//                            if (x instanceof Collection< ? >){
//                            }
//                            if (x instanceof Map<?,?>){
//                            }
                        Object res = Operate.getList(type, sql, args);

                        // 存入缓存
                        if (useCache) {
                            System.out.println(uuid + ": 缓存查询结果");
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, res);
                        }

                        cache.count(cacheName);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return res;
                    }

                    Object res = Operate.get(type, sql, args);

                    // 存入缓存
                    if (useCache) {
                        System.out.println(uuid + ": 缓存查询结果");
                        cache.count(cacheName);
                        cache.cacheTo(cacheName, res);
                    }

                    cache.count(cacheName);

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }
                if (method.isAnnotationPresent(Update.class)) {
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Update anno = method.getAnnotation(Update.class);
                    sql = anno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);
                    int res = update(sql, args);

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }
                if (method.isAnnotationPresent(Delete.class)) {
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Delete anno = method.getAnnotation(Delete.class);
                    sql = anno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);
                    int res = update(sql, args);

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }
                if (method.isAnnotationPresent(Insert.class)) {
                    System.out.println(uuid + ": 执行用户注解定义的方法: " + method.getName());
                    Insert anno = method.getAnnotation(Insert.class);
                    sql = anno.value();
                    System.out.println(uuid + ": 当前sql: " + sql);
                    int res = update(sql, args);

                    Instant end = Instant.now();
                    Duration between = Duration.between(start, end);
                    System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                    return res;
                }

                // 如果不是上面的, 就走我们根据entity创建的方法
                Class<?> aClass = entitys.get(tbName);
                Map<String, String> methodList = methodMap.get(tbName);
                String[] params = paramsMap.get(tbName);

                System.out.println(uuid + ": 执行根据实体类字段自动生成的方法: " + method.getName());

                // 方法名和我们指定的被代理的gen方法名一致,就进入代理实现
                if (methodList.containsKey(method.getName())) {
//                    System.out.println(methodList.get(method.getName()));
                    if (method.getName().contains("find")) {
                        String cacheName = tbName + "." + method.getName();
//                System.out.println(cache.getCache(cacheName));
                        // 读取缓存
                        if (isUseCache && cache.getCache(cacheName) != null && (cache.getCount(cacheName) % globalCacheQueryMaxCount) != 0) {
                            System.out.println(uuid + ": 从缓存中读取");

                            cache.count(cacheName);

                            if (globalCacheType == CacheType.redis) {
                                GsonBuilder gsonBuilder = new GsonBuilder();
                                // 设置日期转换格式
                                gsonBuilder.setDateFormat("yyyy-MM--dd");
                                Gson gson = gsonBuilder.create();
                                List caches = (List) cache.getCache(cacheName);
                                ArrayList res = new ArrayList();
                                for (Object s : caches) {
                                    //解析对象：第一个参数：待解析的字符串 第二个参数结果数据类型的Class对象
                                    res.add(gson.fromJson(s.toString(), type));
                                }

                                // 执行结束
                                Instant end = Instant.now();
                                Duration between = Duration.between(start, end);
                                System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                                return res;
                            }

                            Instant end = Instant.now();
                            Duration between = Duration.between(start, end);
                            System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                            return cache.getCache(cacheName);
                        }

                        sql = MysqlBuilder.build().tbName(tbName).select().where(methodList.get(method.getName()), SqlCompareIdentity.EQ).sql();
                        System.out.println("当前sql: " + sql);

                        Object obj = getList(aClass, sql, args);

                        if (isUseCache) {
                            System.out.println(uuid + ": 缓存查询结果");
                            cache.count(cacheName);
                            cache.cacheTo(cacheName, obj);
                        }

                        cache.count(cacheName);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
                    if (method.getName().contains("update")) {
                        MysqlBuilder builder = MysqlBuilder.build().base("update tb_user set");
                        // update set xxx=?
                        List<String> setParams = new ArrayList<>();
                        for (int i = 0; i < params.length; i++) {
                            // 不等于作为条件的字段
                            if (!params[i].contains("id") && !methodList.get(method.getName()).equals(params[i])) {
                                setParams.add(params[i]);
                            }
                        }
                        for (int i = 0; i < setParams.size(); i++) {
                            if (i == setParams.size() - 1) {
                                // set x=?
                                builder.set(setParams.get(i));
                                break;
                            }
                            // set x=?,
                            builder.set(setParams.get(i)).comma();
                        }
                        sql = builder.where(methodList.get(method.getName()), SqlCompareIdentity.EQ).sql();
                        System.out.println("当前sql: " + sql);
                        int obj = update(sql, args);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
                    if (method.getName().contains("delete")) {
                        sql = MysqlBuilder.build().tbName(tbName).delete().where(methodList.get(method.getName()), SqlCompareIdentity.EQ).sql();
                        System.out.println("当前sql: " + sql);
                        int obj = update(sql, args);

                        Instant end = Instant.now();
                        Duration between = Duration.between(start, end);
                        System.out.println(uuid + ": 执行耗时(毫秒): " + between.toMillis());

                        return obj;
                    }
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

    /**
     * 查询之前调用,设置是否从缓存查询
     *
     * @param flag
     */
    public static void useCache(boolean flag) {
        isUseCache = flag;
        // 简单粗暴地清空缓存
        // 用户如果true存入缓存,再用false也没办法存入,再开true也是从之前的数据
        // 就永远拿不到新的值,这里清空,在重新为true的时候可以重新缓存
        // 也可以考虑在true的条件下,添加计数器,如果到一定数量,就重新缓存
        if (!isUseCache) {
//            selectCaches = new HashMap<>();
            // 重新计数
//            selectCounts = new HashMap<>();
            cache.reset();
        }
    }

//    public static Operate newInstance() {
//        if (instance == null) {
//            instance = new Operate();
//        }
//        return instance;
//    }

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

    static class genClass {
        Map<String, String> uptParams;
        String entityFullName;
        String entityName;

        public genClass(Map<String, String> uptParams, String entityFullName, String entityName) {
            this.uptParams = uptParams;
            this.entityFullName = entityFullName;
            this.entityName = entityName;
        }
    }
}
