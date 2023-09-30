package org.malred.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GenUtils {
    public static void genMethodRepository(
            String entityName, String entityFullName,  Map<String, String> paramMap
    ) throws IOException {
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
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("username", "String");
//        map.put("addr", "String");
        context.put("paramMap", paramMap);
//        context.put("uptParamMap", uptParamMap);
        context.put("entityName", entityName);
        context.put("entityFullName", entityFullName);
        // 4,加载velocity模板文件
        Template template = Velocity.getTemplate("GenRepository.vm", "utf-8");
        // 5,合并数据到模板
        FileWriter fw = new FileWriter("src/main/resources/" + entityName + "GenRepository.java");
        // 合并+写入
        template.merge(context, fw);
        // 6,释放资源
        fw.close();
    }
}
