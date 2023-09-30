package org.malred;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.junit.Test;
import org.malred.utils.Common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
public class VelocityTest {
    @Test
    public void test7() throws IOException {
        // 1,设置velocity的资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        // 2,初始化velocity引擎
        Velocity.init(prop);
        // 3,创建velocity容器
        VelocityContext context = new VelocityContext();
        // 设置数据
        // map
        Map<String, String> map = new HashMap<String, String>();
        map.put( "username","String");
        map.put("addr", "String");
        context.put("paramMap", map);
        context.put("entityName","TbUser");
        context.put("entityFullName","entity.TbUser");
        // 4,加载velocity模板文件
        Template template = Velocity.getTemplate("GenRepository.vm", "utf-8");
        // 5,合并数据到模板
        FileWriter fw =
                new FileWriter("src/main/resources/TbUserGenRepository.java");
        // 合并+写入
        template.merge(context, fw);
        // 6,释放资源
        fw.close();
    }
}