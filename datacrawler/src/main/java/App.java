import crawler.ZhuNiuThread;
import org.apache.log4j.Logger;

public class App {

    // 此处初始化日志地址的代码一定要放在第一行
    static {
        String path = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.startsWith("/"))
            path = path.substring(1);
        if (path.indexOf("jar") > -1) {
            int index = path.lastIndexOf('/');
            path = path.substring(0, index);
        }
        System.out.println(path);
        System.setProperty("log_path", path);
    }

    private static final Logger log = Logger.getLogger(App.class);

    public static void main(String[] args) {

        // 从控制台传入参数，根据不同的平台启动不同的抓取程序
        if (args.length > 0) {
            String platform = args[0];

            if ("zhuniu".equals(platform.trim())) {
                zhuNiu();
            }

        } else {
            log.error("请加入运行参数，以告诉程序需要抓取的平台；例如 java -jar crawler.jar zhuniu");
        }

    }

    public static void zhuNiu() {
        ZhuNiuThread thread = new ZhuNiuThread();
        thread.run();
    }
}
