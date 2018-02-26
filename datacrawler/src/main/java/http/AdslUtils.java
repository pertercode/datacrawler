package http;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 自动拨号以切换IP
 */
public class AdslUtils {
    public static final String ADSL_TITLE = "051610767288";
    public static final String ADSL_NAME = "051610767288";
    public static final String ADSL_PASS = "111222";


    private static String executeCmd(String strCmd) throws Exception {
        Process p = Runtime.getRuntime().exec("cmd /c " + strCmd);
        StringBuilder sbCmd = new StringBuilder();

        // 设置GB2312解决乱码
        // 如果程序默认编码就是GB2312，可以不写
        BufferedReader br = new BufferedReader(new InputStreamReader(p
                .getInputStream(), "GB2312"));
        String line;
        while ((line = br.readLine()) != null) {
            sbCmd.append(line + "\n");
        }
        return sbCmd.toString();

    }

    /**
     * 连接ADSL
     */
    public static boolean conn() throws Exception {
        System.out.println("正在建立连接...");

        String adslCmd = "rasdial " + ADSL_TITLE + " " + ADSL_NAME + " "
                + ADSL_PASS;

        String tempCmd = executeCmd(adslCmd);

        // 判断是否连接成功
        if (tempCmd.indexOf("已连接") > 0) {

            System.out.println("已成功连接.");

            return true;
        } else {
            System.err.println(tempCmd);
            return false;
        }
    }

    /**
     * 断开ADSL
     */
    public static boolean stop() throws Exception {
        String cutAdsl = "rasdial " + ADSL_TITLE + " /disconnect";
        String result = executeCmd(cutAdsl);

        if (result.indexOf("没有连接") != -1) {
            System.err.println(ADSL_TITLE + "连接不存在!");
            return false;
        } else {
            System.out.println("宽带连接已断开...");
            return true;
        }
    }
}
