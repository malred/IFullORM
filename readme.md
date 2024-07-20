# MalORM

- 使用方法: 下载该库, 运行maven install, 在需要的项目中引用该库(org.malred.IFullORM)

> 封装了JDBC的简单的ORM框架, 有3种风格的接口

1. 类似jpa或mybaits-plus风格的接口, 定义接口继承BaseCRUDRepository, 就可以使用提供好的方法(通过动态代理实现)
2. 原生的jdbc封装 + SqlBuilder构建sql语句, 简化原生开发
3. 注解开发, 允许用户创建接口, 在接口方法的注解中编写复杂sql, 该框架会通过动态代理来注入参数和执行sql

## 设置数据库

> 可以通过JDBCUtils.setDataSource设置数据库连接的属性

```java
public class t {
    @Before
    public void before() {
        // 设置数据库属性
        JDBCUtils.setDataSource("test", "root", "root");
    }
}
```

## 1. jpa或mybaits-plus风格

### 继承BaseCRUDRepository

```java 
import entity.ProductAndUser;
import entity.TbUser;
import org.malred.annotations.table.Repository;
import org.malred.repository.BaseCRUDRepository;

@Repository("tb_user") // 指定数据库表名
public interface UserRepository extends BaseCRUDRepository<TbUser> {
}
```

### 使用代理方法

```java
public class t {
    // 基本CRUD接口的代理实现测试
    @Test
    public void testSelectProxy() {
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);
        List<TbUser> all = mapper.findAll();
        System.out.println(all);

        TbUser one = mapper.findById(1);
        System.out.println(one);
    }

    @Test
    public void testUpdateProxy() {
        int cnt = 0;
        UserRepository mapper = Operate.getMapper(UserRepository.class);

        cnt = mapper.update(new TbUser(212, "ema1n", "s1sap", "女", null));
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsertProxy() {
        int cnt = 0;
        UserRepository mapper = Operate.getMapper(UserRepository.class);

        cnt = mapper.insert(new TbUser(0, "eman", "ssap", null, null));
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testDeleteProxy() {
        int cnt = 0;
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);

        cnt = mapper.delete(223);
        System.out.println("影响了" + cnt + "条数据");
    }
}
```

## 2. 原生jdbc + SqlBuilder

```java
public class t {
    // sqlbuilder+jdbc封装 
    @Test
    public void testSelectMulti() throws Exception {
        ArrayList<TbUser> list;

        String sql = SqlBuilder.build().select("tb_user").where("id", SqlCompareIdentity.NE).sql();
        list = Operate.getList(TbUser.class, sql, 1);
        System.out.println(list);

        String sql_cols = SqlBuilder.build().select("tb_user", "username", "gender", "addr").where("id", SqlCompareIdentity.NE).sql();
        list = Operate.getList(TbUser.class, sql_cols, 1);
        System.out.println(list);

        String sql_cols_option2 = SqlBuilder.build().select("tb_user", "username", "gender", "addr").where("id", SqlCompareIdentity.NE).where("gender", SqlCompareIdentity.NE).sql();
        list = Operate.getList(TbUser.class, sql_cols_option2, 1, "男");
        System.out.println(list);

        String sql_count = SqlBuilder.build().count("tb_user").where("id", SqlCompareIdentity.GT).sql();
        Long cnt = (Long) Operate.getValue(sql_count, 3);
        System.out.println(cnt);
    }

    @Test
    public void testUpdate() throws SQLException {
        int cnt = 0;
        String sql = SqlBuilder.build()
                .update("tb_user", "password", "username")
                .where("id", SqlCompareIdentity.EQ)
                .sql();
        cnt = Operate.update(sql, "O", "t50", "13");
        System.out.println("影响了" + cnt + "条数据");

        String sql_no_tbname = SqlBuilder.build()
                .tbName("tb_user")
                .update(new String[]{"password", "username"})
                .where("id", SqlCompareIdentity.EQ)
                .sql();
        cnt = Operate.update(sql_no_tbname, "Obu", "t50123", "13");
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsert() throws SQLException {
        int count = 0;
        String sql = SqlBuilder.build()
                .insert("tb_user", "username", "password", "addr", "gender")
                .sql();
        count = Operate.update(sql, "name", "pass", "cn", "男");
        System.out.println("影响了" + count + "条数据");
    }

    @Test
    public void testDelete() throws SQLException {
        int cnt = 0;
        String sql_tb = SqlBuilder.build()
                .tbName("tb_user")
                .delete()
                .where("id", SqlCompareIdentity.EQ).sql();
        cnt = Operate.update(sql_tb, 219);
        System.out.println("影响了" + cnt + "条数据");
    }
}
```

## 3. 接口 + 注解

```java
package dao;

import entity.ProductAndUser;
import entity.TbUser;
import org.malred.annotations.sql.Delete;
import org.malred.annotations.sql.Insert;
import org.malred.annotations.sql.Select;
import org.malred.annotations.sql.Update;
import org.malred.annotations.table.Repository;

import java.util.List;

// 还有一种,注册带泛型的repo接口,反射获取泛型类,
// 泛型类@table注解指定表名,如果没有就通过simplename作为表名
// 然后sqlbuilder拼接sql
// 根据泛型类的字段分别创建crudbyxxx
@Repository("tb_user")
public interface UserRepository {
    @Select("select * from tb_user where username=?")
    public TbUser selectOneByUsername(String username);

    @Select("select * from tb_user where password!=?")
    public List<TbUser> selectOneByNEPassword(String password);


    // 提供复杂sql的执行
    @Select("select " +
            "u.id as uid,username,addr,password,gender," +
            "p.id as pid,product_name,product_time " +
            "from tb_user u " +
            "inner join tb_product p " +
            "on u.id=p.id " +
            "where u.id=?")
    public List<ProductAndUser> findUserAndProductJoin(int id);

    @Update("update tb_user set username=? where id=?")
    public int uptUser(String uname, int id);

    @Delete("delete from tb_user where id=? and password=?")
    public int delUser(int id, String password);

    @Insert("insert into tb_user(username,password) values (?,?),(?,?)")
    public int addUser(String uname1, String pass1, String uname2, String pass2);
}
```

```java
public class t {
    // 用户定义的注解的代理实现测试
    @Test
    public void testSelectAnnotation() {
        UserRepository mapper = Operate.getMapper(UserRepository.class);

        TbUser user = mapper.selectOneByUsername("张三");
        System.out.println(user);

        List<TbUser> tbUsers = mapper.selectOneByNEPassword("456");
        for (TbUser tbUser : tbUsers) {
            System.out.println(tbUser);
        }

        List<ProductAndUser> userAndProductJoin = mapper.findUserAndProductJoin(7);
        System.out.println(userAndProductJoin);
    }

    @Test
    public void testUpdateAnnotation() {
        UserRepository mapper = Operate.getMapper(UserRepository.class);
        int cnt = mapper.uptUser("哇哈哈1", 14);
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testDeleteAnnotation() {
        UserRepository mapper = Operate.getMapper(UserRepository.class);
        int cnt = mapper.delUser(218, "tpass");
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsertAnnotation() {
        UserRepository mapper = Operate.getMapper(UserRepository.class);
        int cnt = mapper.addUser("tttt", "tpass", "tttt2", "tpass2");
        System.out.println("影响了" + cnt + "条数据");
    }
}
```

# 更新日志

> 新增根据方法名提供代理方法, 方法名格式为find|update|delete_by_字段名_gen

> 首先需要在类上添加@ScanEntity注解, 指定实体类所在路径

```java

@ScanEntity("entity")
public class t {
}
```

> 然后在实体类上加上注解@Entity, 指定表名, 动态代理时会通过表名在接口和实体类间建立连接

```java

@Entity("tb_user")
public class TbUser {
}
```

> 在接口中定义方法

```java

@Repository("tb_user")
public interface UserRepository {
    public List<TbUser> find_by_username_gen(String username);

    public int update_by_username_gen(String password, String gender, String addr, String username);

    public int delete_by_username_gen(String username);
}
``` 

> 测试

```java

@ScanEntity("entity")
public class t {
    // 测试根据实体类字段自动生成的方法
    @Test
    public void testEntityParamGenFn() throws IOException, ClassNotFoundException {
        Operate.scan(t.class);
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);

        List<TbUser> users = mapper.find_by_username_gen("张三");
        System.out.println(users);

        int cnt = 0;
        cnt = mapper.update_by_username_gen("1314", "女", "zh_cn", "eman");
        System.out.println("影响了" + cnt + "条数据");

        cnt = mapper.delete_by_username_gen("ttt");
        System.out.println("影响了" + cnt + "条数据");
    }
}
```

# 更新日志 2023-9-30

之前更新了根据实体类字段名生成方法名, 然后实现其代理逻辑, repository只需要定义一个符合命名规则的方法就可以使用; 这次更新,
则可以自动生成带这些方法名的repository, 而且继承了baseRepository, 用户只需要定义一个repository继承就可以使用这些方法

## 生成代码

```java

@ScanEntity("entity")
public class t {
    @Test
    public void testGen() throws IOException, ClassNotFoundException {
        Operate.scan(t.class);
        Operate.gen();
    }
}
```

## 生成后的repository

```java 
import org.malred.repository.BaseCRUDRepository;
import entity.TbUser;

import java.util.List;

public interface TbUserGenRepository extends BaseCRUDRepository<TbUser> {

    public List<TbUser> find_by_password_gen(String password);

    public int update_by_password_gen(
            String gender,
            String addr,
            String username,
            String password
    );

    public int delete_by_password_gen(String password);

    public List<TbUser> find_by_gender_gen(String gender);

    public int update_by_gender_gen(
            String password,
            String addr,
            String username,
            String gender
    );

    public int delete_by_gender_gen(String gender);

    public List<TbUser> find_by_addr_gen(String addr);

    public int update_by_addr_gen(
            String password,
            String gender,
            String username,
            String addr
    );

    public int delete_by_addr_gen(String addr);

    public List<TbUser> find_by_username_gen(String username);

    public int update_by_username_gen(
            String password,
            String gender,
            String addr,
            String username
    );

    public int delete_by_username_gen(String username);
}
```

## 用户定义repository继承

```java
package dao;

import entity.ProductAndUser;
import entity.TbUser;

import java.util.List;

@Repository("tb_user")
public interface UserRepository extends TbUserGenRepository {
}
```

## 使用

```java

@ScanEntity("entity")
public class t {
    @Test
    public void testEntityParamGenFn() throws IOException, ClassNotFoundException {
        // 扫描实体类 
        Operate.scan(t.class);
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);

        List<TbUser> users = mapper.find_by_username_gen("张三");
        System.out.println(users);

        int cnt = 0;
        cnt = mapper.update_by_username_gen("1314", "女", "zh_cn", "eman");
        System.out.println("影响了" + cnt + "条数据");

        cnt = mapper.delete_by_username_gen("yyy");
        System.out.println("影响了" + cnt + "条数据");
    }
}
```

# 更新: 自动建表, 以及提供建表字段注解

> 调用框架scan方法, 会扫描传入方法的类的ScanEntity注解, 根据该注解value找到对应包, 扫描该包下所有类, 如果带有Entity注解,
> 就会根据该注解value创建数据库表, 建表时根据提供的字段注解进行定制化建表

目前可用建表字段注解(org.malred.annotations.table)

- ID 表明是ID字段
- AutoIncrement 主键自增
- NotNull 不能为空
- SQLCharLen varchar字段长度
- SQLDateNow 默认date为当前时间
- SQLDefault 字段默认值

```java
package entity;

import org.malred.annotations.table.*;

import java.util.Date;

@Entity("tb_test")
public class TbTest {
    @ID()
    @AutoIncrement()
    int id;
    @SQLCharLen(50)
    String username;
    @SQLDefault("1@1.com")
    String email;
    @SQLDateNow()
    Date birthdate;
    //    String birthdate;
    @NotNull()
    boolean is_active;

    @Override
    public String toString() {
        return "TbTest{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", birthdate=" + birthdate +
                ", is_active=" + is_active +
                '}';
    }
}
```

> 测试

记得在生成repository后, 创建一个interface来extend该repository, 然后就可以操作了

```java
import dao.TestRepository;
import dao.UserRepository;
import entity.TbTest;
import org.junit.Before;
import org.junit.Test;
import org.malred.annotations.table.*;
import org.malred.cores.Operate;
import org.malred.utils.Common;
import org.malred.utils.JDBCUtils;

import java.sql.Timestamp;
import java.util.*;

@ScanEntity("entity")
public class testTableJDBC {
    @Before
    public void before() {
        // 设置数据库属性
        JDBCUtils
                .setDataSource("jdbc:mysql://localhost:3306/mybatis",
                        "com.mysql.cj.jdbc.Driver", "root", "123456");

//        JDBCUtils.setSchema("mybatis");
    }

    // 创建表和repository, repository包含操作数据库的方法
    @Test
    public void tableRaw() throws Exception {
        Operate.scan(testTableJDBC.class);
        Operate.gen();
    }

    // 测试根据实体类字段自动生成的方法
    @Test
    public void testEntityParamGenFn() throws Exception {
        Operate.scan(testTableJDBC.class);
        // 扫描实体类
        TestRepository mapper = Operate.getMapper(TestRepository.class, TbTest.class);

        TbTest t = new TbTest();
        t.setUsername("张三");
        t.setIs_active(true);
        mapper.insert(t);

        List<TbTest> users = mapper.find_by_username_gen("张三");
        System.out.println(users);

        int cnt = 0;
        cnt = mapper.update_by_email_gen("sss", new Date(), false, "1@1.com");
        System.out.println("影响了" + cnt + "条数据");

        cnt = mapper.delete_by_username_gen("sss");
        System.out.println("影响了" + cnt + "条数据");
    }
}
```
