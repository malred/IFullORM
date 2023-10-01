import com.google.gson.Gson;
import entity.TbUser;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class testRedis {
    Jedis jedis;

    @Before
    public void before() {
        jedis = new Jedis("127.0.0.1", 6379);
    }

    @Test
    public void isList() {
        Object list = new ArrayList<TbUser>();
        // 是否是list
        if (list instanceof List) {
            System.out.println("is");
        }
    }

    @Test
    public void testList() {
        // 清空数据
        System.out.println(jedis.flushDB());
        ArrayList<TbUser> tbUsers = new ArrayList<>();
        tbUsers.add(new TbUser());
        tbUsers.add(new TbUser());
        tbUsers.add(new TbUser());

        String[] vals = new String[tbUsers.size()];
        Gson gson = new Gson();
        for (int i = 0; i < tbUsers.size(); i++) {
            vals[i] = gson.toJson(tbUsers.get(i));
        }
        jedis.lpush("list", vals);
        Long len = jedis.llen("list");
        System.out.println(len);
        List<String> list = jedis.lrange("list", 0, len);
        System.out.println(list);
    }
}

