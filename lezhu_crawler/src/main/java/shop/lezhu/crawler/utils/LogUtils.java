package shop.lezhu.crawler.utils;

import shop.lezhu.crawler.Main;

import java.io.*;
import java.net.UnknownHostException;

public class LogUtils {

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }


    public static void writeToFile(String key, String location, String msg) {
        try {
            File baseDir = new File(Main.BASE_PATH, "logs");
            baseDir.mkdirs();

            String fileName = key + "_" + location.replaceAll(":", "_") + ".log";
            File logFile = new File(baseDir, fileName);
            if (!logFile.exists())
                logFile.createNewFile();

            FileWriter fw = new FileWriter(new File(baseDir, fileName), true);
            fw.write(msg);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getLogDir() {
        File baseDir = new File(Main.BASE_PATH, "logs");
        baseDir.mkdirs();

        return baseDir;
    }

    // 书写发送统计log
    public static void writeSendLog(String key, String location, String msg) {
        try {
            File baseDir = new File(Main.BASE_PATH, "logs");
            baseDir.mkdirs();

            String fileName = key + "_" + location.replaceAll(":", "_") + "_count_.log";
            File logFile = new File(baseDir, fileName);
            if (!logFile.exists())
                logFile.createNewFile();

            FileWriter fw = new FileWriter(new File(baseDir, fileName), true);
            fw.write(msg);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
