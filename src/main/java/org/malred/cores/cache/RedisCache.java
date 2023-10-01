package org.malred.cores.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.malred.cores.Operate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

public class RedisCache implements DefaultCache {
    // 缓存计数器,到?刷新
    public static Map<String, Integer> selectCounts = new HashMap<>();
    private Jedis jedis;

    public RedisCache(Jedis redisConfig) {
        this.jedis = redisConfig;
        // 连接
        String response = jedis.ping();
        System.out.println(response); // PONG

        System.out.println("删除当前选择数据库中的所有key：" + jedis.flushDB());
    }

    public RedisCache(String host, int port) {        //1.创建连接池配置对象
        JedisPoolConfig config = new JedisPoolConfig();
        //2.配置
        config.setMaxIdle(8);//忙时：最大连接数
        config.setMaxTotal(10);//闲时：最大连接数
        config.setMaxWaitMillis(1 * 1000); //创建连接超时
        config.setTestOnBorrow(true);//获取连接是测试连接是否畅通
        //3.创建连接池
        JedisPool pool = new JedisPool(config, host, port, 1000);
        //4.通过连接池获取连接
        this.jedis = pool.getResource();
        // 连接
        String response = jedis.ping();
        System.out.println(response); // PONG

        System.out.println("删除当前选择数据库中的所有key：" + jedis.flushDB());
    }

    @Override
    public int getCount(String key) {
        return selectCounts.get(key);
    }

    @Override
    public void cacheTo(String key, Object obj) {
//        System.out.println("存入redis: \n" + obj);
        // 已经有了,更新,先删了
        if (jedis.exists(key)) {
            jedis.del(key);
        }
        Gson gson = new Gson();
        if (obj instanceof List) {
            String[] vals = new String[((List<?>) obj).size()];
            for (int i = 0; i < ((List<?>) obj).size(); i++) {
                String json = gson.toJson(((List<?>) obj).get(i));
                vals[i] = json;
            }
            jedis.lpush(key, vals);
//            jedis.sadd(key, vals);
            return;
        }
        jedis.lpush(key, gson.toJson(obj));
    }

    @Override
    public Object getCache(String key) {
        // 返回的是json, 自己去转类型
        List<String> list = jedis.lrange(key, 0, -1);
//        Set<String> list = jedis.smembers(key);
//        if (list.size() == 1) {
//            return list.get(0);
//        }
        if (list.size() != 0) {
            return list;
        }
        return null;
    }

    @Override
    public void reset() {
        System.out.println("删除当前选择数据库中的所有key：" + jedis.flushDB());
        // 重新计数
        selectCounts = new HashMap<>();
    }

    @Override
    public void count(String key) {
        Integer integer = selectCounts.get(key);
        if (integer == null) {
            selectCounts.put(key, 0);
            return;
        }
        selectCounts.put(key, integer + 1);
    }
}
