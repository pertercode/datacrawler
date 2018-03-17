package shop.lezhu.crawler.utils;

import com.yunpian.sdk.util.StringUtil;
import shop.lezhu.crawler.Main;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

public class ConfigUtils {

    private static final String DEFAULT_TPL = "#company# 欲采购 #buyInfo#，联系电话 #phone#，更多采购信息，请见： #url# ";

    private static final String DEFAULT_API_KEY = "96c419beb9afd074b9ccec8e623ed3b2";

    public static final String DEFAULT_DEBUG_BASEURL = "http://test.lezhu.shop/";

    public static final String DEFAULT_RELEASE_BASEURL = "http://api.lezhu.shop/";

    private static final Boolean DEFAULT_DEBUG = true;


    private static String readConfig(String key) {
        String configName = "crawler.config";
        String result = "";
        try {
            File baseDir = new File(Main.BASE_PATH);
            baseDir.mkdirs();

            File configFile = new File(baseDir, configName);
            if (!configFile.exists())
                configFile.createNewFile();

            FileInputStream fis = new FileInputStream(configFile) ;
            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
            Properties properties = new Properties();
            properties.load(isr);

            result = properties.getProperty(key,"");
            System.out.println(result);
            isr.close();
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String writeConfig(String key, String value) {
        String configName = "crawler.config";
        String result = "";
        try {
            File baseDir = new File(Main.BASE_PATH);
            baseDir.mkdirs();

            File configFile = new File(baseDir, configName);
            if (!configFile.exists())
                configFile.createNewFile();

            FileInputStream fis = new FileInputStream(configFile) ;
            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
            Properties properties = new Properties();
            properties.load(isr);
            isr.close();
            fis.close();

            properties.setProperty(key,value);

            FileOutputStream fos = new FileOutputStream(configFile);
            OutputStreamWriter osw= new OutputStreamWriter(fos,Charset.forName("UTF-8"));
            properties.store(osw, "settings");
            osw.flush();
            osw.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isDebug() {
        boolean isDebug = DEFAULT_DEBUG;
        String debugString = readConfig("debug");

        if (!StringUtils.isEmpty(debugString)) {
            isDebug = debugString.contains("true") || debugString.contains("1") || debugString.contains("yes") || debugString.contains("y");
        }
        return isDebug;
    }

    public static void setDebug(Boolean isDebug) {
        writeConfig("debug", isDebug.toString());
    }


    /**
     * 获得短信模板
     *
     * @return
     */
    public static String getMsgTpl() {
//        String tpl = readConfig("msg_tpl");
//        if (StringUtils.isEmpty(tpl)) {
//            tpl = DEFAULT_TPL;
//        }
        return DEFAULT_TPL;
    }

//    public static void setMsgTpl(String tpl) {
//        writeConfig("msg_tpl", tpl);
//    }


    /**
     * 获得Api Url 链接
     *
     * @return
     */
    public static String getApi() {
        String tpl = readConfig("base_api");
        if (StringUtils.isEmpty(tpl)) {
            tpl = DEFAULT_RELEASE_BASEURL;
        }
        return tpl;
    }

    public static void setApi(String apiUrl) {
        writeConfig("base_api", apiUrl);
    }


    /**
     * 获得YP_API_KEY
     *
     * @return
     */
    public static String getApiKey() {
        String key = readConfig("yp_apikey");
        if (StringUtils.isEmpty(key)) {
            key = DEFAULT_API_KEY;
        }
        return key;
    }

    public static void setApiKey(String api_key) {
        writeConfig("yp_apikey", api_key);
    }





}
