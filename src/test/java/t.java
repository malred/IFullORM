import entity.ProductAndUser;
import org.junit.Before;
import org.junit.Test;
import dao.UserRepository;
import entity.TbUser;
import org.malred.annotations.table.ScanEntity;
import org.malred.cores.Operate;
import org.malred.cores.builder.mysql.MysqlBuilder;
import org.malred.utils.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ScanEntity("entity")
public class t {
    @Before
    public void before() {
        // 设置数据库属性
        JDBCUtils
                .setDataSource("jdbc:mysql://localhost:3307/mybatis",
                        "com.mysql.cj.jdbc.Driver", "root", "123456");
    }

    // sqlbuilder+jdbc封装
    @Test
    public void testSelectBuild() {
        String sql = MysqlBuilder.build()
                .select("tb_user")
                .where("id", SqlCompareIdentity.NE)
                .sql();
        System.out.println(sql);

        String sql_cols = MysqlBuilder.build()
                .select("tb_user", "username", "gender", "addr")
                .where("id", SqlCompareIdentity.NE)
                .sql();
        System.out.println(sql_cols);

        String sql_cols_option2 = MysqlBuilder.build().select("tb_user", "username", "gender", "addr").where("id", SqlCompareIdentity.NE).where("password", SqlCompareIdentity.NE).sql();
        System.out.println(sql_cols_option2);

        String sql_count = MysqlBuilder.build().count("tb_user").where("id", SqlCompareIdentity.GT).sql();
        System.out.println(sql_count);
    }

    @Test
    public void testSelectBuildTbName() {
        // 直接设置build的tbName,然后使用不需要tbName的方法来构建
        String sql1 = MysqlBuilder.build().tbName("tb_user").select().where("id", SqlCompareIdentity.NE).sql();
        System.out.println(sql1);

        String sql_cols1 = MysqlBuilder.build().tbName("tb_user").select(new String[]{"username", "gender", "addr"}).where("id", SqlCompareIdentity.NE).sql();
        System.out.println(sql_cols1);

        String sql_cols_option21 = MysqlBuilder.build().tbName("tb_user").select(new String[]{"username", "gender", "addr"}).where("id", SqlCompareIdentity.NE).where("password", SqlCompareIdentity.NE).sql();
        System.out.println(sql_cols_option21);

        String sql_count1 = MysqlBuilder.build().tbName("tb_user").count().where("id", SqlCompareIdentity.GT).sql();
        System.out.println(sql_count1);

        String sql_join = MysqlBuilder.build().tbName("tb_user").select().join(SqlJoinType.INNER, "tb_product").on("id", SqlCompareIdentity.EQ, "id").where("tb_user.id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql_join);
    }

    @Test
    public void testSelectMulti() throws Exception {
        ArrayList<TbUser> list;

        String sql = MysqlBuilder.build().select("tb_user").where("id", SqlCompareIdentity.NE).sql();
        list = Operate.getList(TbUser.class, sql, 1);
        System.out.println(list);

        String sql_cols = MysqlBuilder.build().select("tb_user", "username", "gender", "addr").where("id", SqlCompareIdentity.NE).sql();
        list = Operate.getList(TbUser.class, sql_cols, 1);
        System.out.println(list);

        String sql_cols_option2 = MysqlBuilder.build().select("tb_user", "username", "gender", "addr").where("id", SqlCompareIdentity.NE).where("gender", SqlCompareIdentity.NE).sql();
        list = Operate.getList(TbUser.class, sql_cols_option2, 1, "男");
        System.out.println(list);

        String sql_count = MysqlBuilder.build().count("tb_user").where("id", SqlCompareIdentity.GT).sql();
        Long cnt = (Long) Operate.getValue(sql_count, 3);
        System.out.println(cnt);
    }

    @Test
    public void testUpdateBuild() {
        String sql = MysqlBuilder.build()
                .update("tb_user", "password", "username")
                .where("id", SqlCompareIdentity.EQ)
                .sql();
        System.out.println(sql);

        String sql_no_tbname = MysqlBuilder.build().tbName("tb_user").update(new String[]{"password", "username"}).where("id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql_no_tbname);
    }

    @Test
    public void testUpdate() throws SQLException {
        int cnt = 0;
        String sql = MysqlBuilder.build()
                .update("tb_user", "password", "username")
                .where("id", SqlCompareIdentity.EQ)
                .sql();
        cnt = Operate.update(sql, "O", "t50", "13");
        System.out.println("影响了" + cnt + "条数据");

        String sql_no_tbname = MysqlBuilder.build()
                .tbName("tb_user")
                .update(new String[]{"password", "username"})
                .where("id", SqlCompareIdentity.EQ)
                .sql();
        cnt = Operate.update(sql_no_tbname, "Obu", "t50123", "13");
        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsertBuild() {
        String sql = MysqlBuilder.build()
                .insert("tb_user", "username", "password", "addr", "gender")
                .sql();
        System.out.println(sql);
    }

    @Test
    public void testInsertBuildTbName() {
        String sql = MysqlBuilder.build()
                .tbName("tb_user")
                .insert(new String[]{"username", "password", "addr", "gender"}).sql();
        System.out.println(sql);
    }

    @Test
    public void testInsert() throws SQLException {
        int count = 0;
        String sql = MysqlBuilder.build()
                .insert("tb_user", "username", "password", "addr", "gender")
                .sql();
        count = Operate.update(sql, "name", "pass", "cn", "男");
        System.out.println("影响了" + count + "条数据");
    }

    @Test
    public void testDeleteBuild() {
        String sql_tb = MysqlBuilder.build()
                .tbName("tb_user").delete()
                .where("id", SqlCompareIdentity.EQ)
                .sql();
        System.out.println(sql_tb);

        String sql = MysqlBuilder.build().delete("tb_user")
                .where("id", SqlCompareIdentity.EQ).sql();
        System.out.println(sql);
    }

    @Test
    public void testDelete() throws SQLException {
        int cnt = 0;
        String sql_tb = MysqlBuilder.build()
                .tbName("tb_user")
                .delete()
                .where("id", SqlCompareIdentity.EQ).sql();
        cnt = Operate.update(sql_tb, 219);
        System.out.println("影响了" + cnt + "条数据");
    }

    // 基本CRUD接口的代理实现测试
    @Test
    public void testSelectProxy() {
        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);
//        UserRepository mapper = Operate.getMapper(UserRepository.class);
        List<TbUser> all = mapper.findAll();
        System.out.println(all);

        TbUser one = mapper.findById(1);
        System.out.println(one);
    }

    @Test
    public void testUpdateProxy() {
        int cnt = 0;
//        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);
        UserRepository mapper = Operate.getMapper(UserRepository.class);

        cnt = mapper.update(new TbUser(212, "ema1n", "s1sap", "女", null));
        System.out.println("影响了" + cnt + "条数据");

//        cnt = mapper.update(new TbUser(211, "name", "pass", null, null));
//        System.out.println("影响了" + cnt + "条数据");
    }

    @Test
    public void testInsertProxy() {
        int cnt = 0;
//        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);
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

    // 用户定义的注解的代理实现测试
    @Test
    public void testSelectAnnotation() {
        UserRepository mapper = Operate.getMapper(UserRepository.class);
//        UserRepository mapper = Operate.getMapper(UserRepository.class, TbUser.class);

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

    @Test
    public void testLoadEntity() {
        try {
            Operate.scan(t.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 测试根据实体类字段自动生成的方法
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

    @Test
    public void testGen() throws IOException, ClassNotFoundException {
        Operate.scan(t.class);
    }
}
