package utils;

import main.App;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class LogUtils {

    private Logger log = null;

    public LogUtils(String platform, Class clazz) {
        log = Logger.getLogger(clazz);
        String logPath = App.BASE_PATH + "log/" + platform + "/error.log";
        FileAppender appender = (FileAppender) log.getRootLogger().getAppender("E");
        appender.setFile(logPath);
        appender.activateOptions();
    }

    public void i(String str) {
        log.info(str);
    }

    public void e(String str, Exception e) {
        log.error(str, e);
    }
}
