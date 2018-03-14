package main;

import crawler.EJianLianThread;
import crawler.YunZhuShangChengThread;
import crawler.ZhuNiuThread;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class App {
    public static String BASE_PATH;

    static {
        // 获得程序的工作目录（jar包所在位置）
        BASE_PATH = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (BASE_PATH.startsWith("/"))
            BASE_PATH = BASE_PATH.substring(1);
        if (BASE_PATH.indexOf("jar") > -1) {
            int index = BASE_PATH.lastIndexOf('/');
            BASE_PATH = BASE_PATH.substring(0, index);
        }
        System.setProperty("log_path", BASE_PATH);
        System.out.println(BASE_PATH);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String platform = args[0];
            if ("zhuniu".equals(platform.trim())) {
                zhuNiu();
            } else if ("ejianlian".equals(platform.trim())) {
                eJianLian();
            } else if ("yunzhushangcheng".equals(platform.trim())) {
                yunzhushangcheng();
            }

        } else {
            System.err.println("请加入运行参数，以告诉程序需要抓取的平台；例如 java -jar crawler.jar zhuniu");
        }

    }

    public static void zhuNiu() {
        ZhuNiuThread thread = new ZhuNiuThread();
        thread.run();
    }

    public static void eJianLian() {
        EJianLianThread thread = new EJianLianThread();
        thread.run();

    }

    public static void yunzhushangcheng() {
        YunZhuShangChengThread thread = new YunZhuShangChengThread();
        thread.run();
    }

    public static void alibaba() {
        YunZhuShangChengThread thread = new YunZhuShangChengThread();
        thread.run();
    }
}
