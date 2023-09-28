# MalORM

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
import org.malred.annotations.*;
import entity.TbUser;
import org.malred.repository.BaseCRUDRepository;

import java.util.List;

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
import org.malred.annotations.*;
import entity.TbUser;
import org.malred.repository.BaseCRUDRepository;

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