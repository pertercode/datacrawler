package shop.lezhu.crawler.utils;

import shop.lezhu.crawler.Main;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;

/**
 * Created by wushiling on 2018/1/8.
 */

public class StringUtils {


    public static String URLEncoder(String str, String charset) {
        try {
            return URLEncoder.encode(str, charset);
        } catch (UnsupportedEncodingException e) {
            Main.mainForm.printLog(LogUtils.getStackTraceString(e));
        }
        return "";
    }

    public static String URLDecoder(String str, String charset) {
        try {
            return URLDecoder.decode(str, charset);
        } catch (UnsupportedEncodingException e) {
            Main.mainForm.printLog(LogUtils.getStackTraceString(e));
        }
        return "";
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        int chineseCount = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c) || c == ':') {
                chineseCount++;
            }
        }
        return chineseCount == ch.length;
    }


    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() < 1;
    }


    public static String calcUseTime(long start, long end) {
        DecimalFormat df = new DecimalFormat("#.00");

        long use = end - start;

        double seconds = use / 1000;

        if (seconds > 60) {
            return df.format(seconds / 60) + " 分钟";
        } else {
            return df.format(seconds) + " 秒";
        }
    }
}
