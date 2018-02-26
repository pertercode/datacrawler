package shop.lezhu.crawler.test;

import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;
import shop.lezhu.crawler.utils.YunPianSmsUtils;

import java.util.Map;

public class Test {

    @org.junit.Test
    public void t (){
        YunPianSmsUtils.sendSms("18796213142","测试,发给我自己");
    }

}
