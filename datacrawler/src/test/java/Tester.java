import http.HttpUtils;
import http.IpUtils;
import okhttp3.Request;
import org.junit.Test;

import java.util.logging.Logger;

public class Tester {



    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }


    @Test
    public void testLog() {
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Tester.class);
        logger.error("1" ,new RuntimeException("1"));
    }

}
