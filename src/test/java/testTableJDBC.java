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
//                .setDataSource("jdbc:mysql://localhost:3307/mybatis?tinyInt1isBit=false",
                .setDataSource("jdbc:mysql://localhost:3307/mybatis",
                        "com.mysql.cj.jdbc.Driver", "root", "123456");

//        JDBCUtils.setSchema("mybatis");
    }

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
        cnt = mapper.update_by_email_gen("sss", new Date(),false , "1@1.com");
        System.out.println("影响了" + cnt + "条数据");

        cnt = mapper.delete_by_username_gen("sss");
        System.out.println("影响了" + cnt + "条数据");
    }
}
