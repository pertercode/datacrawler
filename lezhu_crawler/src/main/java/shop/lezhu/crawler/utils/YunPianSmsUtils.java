package shop.lezhu.crawler.utils;

import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;

import java.util.Map;

public class YunPianSmsUtils {

    static YunpianClient clnt;

    static {

    }


    public static Result<SmsSingleSend> sendSms(String mobile, String msg) {
        clnt = new YunpianClient(ConfigUtils.getApiKey()).init();
        //发送短信API
        Map<String, String> param = clnt.newParam(2);
        param.put(YunpianClient.MOBILE, mobile);
        param.put(YunpianClient.TEXT, msg);
        Result<SmsSingleSend> r = clnt.sms().single_send(param);
        return r;
    }


}
