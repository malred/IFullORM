package org.malred.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class LoadUtils {
    // 根据类名加载类
    public static List<Class<?>> loadClasses(List<File> classes, String scan) throws ClassNotFoundException {
        List<Class<?>> clazzes = new ArrayList<Class<?>>();
        for (File file : classes) {
            // 因为scan 就是/  ， 所有把 file的 / 转成  \   统一都是：  /
            String fPath = file.getAbsolutePath().replaceAll("\\\\", "/");
            // 把 包路径 前面的 盘符等 去掉 ， 这里必须是lastIndexOf ，防止名称有重复的
            String packageName = fPath.substring(fPath.lastIndexOf(scan));
            // 去掉后缀.class ，并且把 / 替换成 .    这样就是  com.hadluo.A 格式了 ， 就可以用Class.forName加载了
            packageName = packageName.replace(".class", "").replaceAll("/", ".");
            // 根据名称加载类
            clazzes.add(Class.forName(packageName));
        }
        return clazzes;

    }

    /**
     * 查找所有的文件
     *
     * @param dir      路径
     * @param fileList 文件集合
     */
    public static void listFiles(File dir, List<File> fileList) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                listFiles(f, fileList);
            }
        } else {
            if (dir.getName().endsWith(".class")) {
                fileList.add(dir);
            }
        }
    }

    // 传入包路径(.),扫描得到包下的类
    public static List<Class<?>> loadClass(String scan) throws ClassNotFoundException, IOException {
        // 结果 class
        List<Class<?>> result = null;
        // 把 . 转换成 \ 因为下面是文件操作
        scan = scan.replaceAll("\\.", "/");
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(scan);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            if (url.getProtocol().equals("file")) {
                List<File> classes = new ArrayList<File>();
                // 递归 变量路径下面所有的 class文件
                listFiles(new File(url.getFile()), classes);
                // 加载我们所有的 class文件 就行了
                result = loadClasses(classes, scan);
            } else if (url.getProtocol().equals("jar")) {
                // 等下再说
            }
        }
        return result;
    }

}
