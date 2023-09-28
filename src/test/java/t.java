import entity.ProductAndUser;
import org.junit.Before;
import org.junit.Test;
import dao.UserRepository;
import entity.TbUser;
import org.malred.utils.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class t {
    @Before
    public void before() {
        JDBCUtils.setDataSource("jdbc:mysql://localhost:3307/mybatis", "com.mysql.cj.jdbc.Driver", "root", "123456");
    }

    @Test
    public void testSelectBuild() {
        String sql = SqlBuilder.build().select("tb_user").where("id", SqlCompareIdentity.NE).sql();
        System.out.println(sql);

        String sql_cols = SqlBuilder.build().select("tb_user", "username", "gender", "addr").where("id", SqlCompareIdentity.NE).sql();
        System.out.println(sql_cols);

        String sql_cols_option2 = SqlBuilder.build().select("tb_user", "username", "gender", "addr").where("id", SqlCompareIdentity.NE).where("password", SqlCompareIdentity.NE).sql();
        System.out.println(sql_cols_option2);

        String sql_count = SqlBuilder.build().count("tb_user").where("id", SqlCompareIdentity.GT).sql();
        System.out.println(sql_count);
    }

    @Test
    public void testSelectBuildTbName() {
        // 直接设置build的tbName,然后使用不需要tbName的方法来构建
        String sql1 = SqlBuilder.build().tbName("tb_user").select().where("id", SqlCompareIdentity.NE).sql();
        System.out.println(sql1);

        String sql_cols1 = SqlBuilder.build().tbName("tb_user").select(new String[]{"username", "gender", "addr"}).where("id", SqlCompareIdentity.NE).sql();
        System.out.println(sql_cols1);

        String sql_cols_option21 = SqlBuilder.build().tbName("tb_user").select(new String[]{"username", "gender", "addr"}).where("id", SqlCompareIdentity.NE).where("password", SqlCompareIdentity.NE).sql();
        System.out.println(sql_cols_option21);

        String sql_count1 = SqlBuilder.build().tbName("tb_user").count().where("id", SqlCompareIdentity.GT).sql();
        System.out.println(sql_count1);

        String sql_join = SqlBuilder.build().tbName("tb_user").select().join(SqlJoinType.INNER, "tb_product").on("id", SqlCompareIdentity.EQ, "id").where("tb_user.id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql_join);
    }

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
    public void testUpdateBuild() {
        String sql = SqlBuilder.build().update("tb_user", "password", "username").where("id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql);

        String sql_no_tbname = SqlBuilder.build().tbName("tb_user").update(new String[]{"password", "username"}).where("id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql_no_tbname);
    }

    @Test
    public void testUpdate() throws SQLException {
        int cnt = 0;
        String sql = SqlBuilder.build().update("tb_user", "password", "username").where("id", SqlCompareIdentity.EQ).sql();
        cnt = Operate.update(sql, "O", "t50", "13");
        System.out.println("影响了" + cnt + "条数据");

        String sql_no_tbname = SqlBuilder.build().tbName("tb_user").update(new String[]{"password", "username"}).where("id", SqlCompareIdentity.EQ).sql();
        cnt = Operate.update(sql_no_tbname, "Obu", "t50123", "13");
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsertBuild() {
        String sql = SqlBuilder.build().insert("tb_user", "username", "password", "addr", "gender").sql();
        System.out.println(sql);
    }

    @Test
    public void testInsertBuildTbName() {
        String sql = SqlBuilder.build().tbName("tb_user").insert(new String[]{"username", "password", "addr", "gender"}).sql();
        System.out.println(sql);
    }

    @Test
    public void testInsert() throws SQLException {
        int count = 0;
        String sql = SqlBuilder.build().insert("tb_user", "username", "password", "addr", "gender").sql();
        count = Operate.update(sql, "name", "pass", "cn", "男");
        System.out.println("影响了" + count + "条数据");
    }

    @Test
    public void testDeleteBuild() {
        String sql_tb = SqlBuilder.build().tbName("tb_user").delete().where("id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql_tb);

        String sql = SqlBuilder.build().delete("tb_user").where("id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql);
    }

    @Test
    public void testDelete() throws SQLException {
        int cnt = 0;
        String sql_tb = SqlBuilder.build().tbName("tb_user").delete().where("id", SqlCompareIdentity.EQ).sql();
        cnt = Operate.update(sql_tb, 214);
        System.out.println("影响了" + cnt + "条数据");
    }

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
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);

        cnt = mapper.update(new TbUser(211, "eman", "ssap", null, null));
        System.out.println("影响了" + cnt + "条数据");

        cnt = mapper.update(new TbUser(211, "name", "pass", null, null));
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsertProxy() {
        int cnt = 0;
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);

        cnt = mapper.insert(new TbUser(0, "eman", "ssap", null, null));
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testDeleteProxy() {
        int cnt = 0;
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);

        cnt = mapper.delete(215);
        System.out.println("影响了" + cnt + "条数据");
    }

    // 用户定义的注解的代理实现测试
    @Test
    public void testSelectAnnotation() {
        UserRepository mapper = Operate.getMapper(UserRepository.class);
//                Operate.getMapper(UserRepository.class, TbUser.class);

        TbUser user = mapper.selectOneByUsername("张三");
        System.out.println(user);

        List<TbUser> tbUsers = mapper.selectOneByNEPassword("456");
        for (TbUser tbUser : tbUsers) {
            System.out.println(tbUser);
        }

        // 和因为在代理时写死传入的返回值类型,这里只能重新创建代理
        // 根据接口方法的返回值获取泛型类型,动态判断返回类型
//        UserRepository puMapper =
//                Operate.getMapper(UserRepository.class, ProductAndUser.class);
//        UserRepository puMapper = Operate.getMapper(UserRepository.class);
        List<ProductAndUser> userAndProductJoin = mapper.findUserAndProductJoin(7);
        System.out.println(userAndProductJoin);
    }

    @Test
    public void testUpdateAnnotation() {
//        UserRepository mapper =
//                Operate.getMapper(UserRepository.class, TbUser.class);
        UserRepository mapper = Operate.getMapper(UserRepository.class);
        int cnt = mapper.uptUser("哇哈哈1", 14);
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testDeleteAnnotation() {
//        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);
        UserRepository mapper = Operate.getMapper(UserRepository.class);
        int cnt = mapper.delUser(218, "tpass");
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsertAnnotation() {
//        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);
        UserRepository mapper = Operate.getMapper(UserRepository.class);
        int cnt = mapper.addUser("tttt", "tpass", "tttt2", "tpass2");
        System.out.println("影响了" + cnt + "条数据");
    }
}
