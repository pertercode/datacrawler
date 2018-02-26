package shop.lezhu.crawler.services;

import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import shop.lezhu.crawler.RetryInterceptor;
import shop.lezhu.crawler.utils.GsonUtils;

public class BaseService {
    protected OkHttpClient mOkHttpClient = null;
    protected Gson gson;

    protected Headers headers = null;


    public BaseService() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder().addInterceptor(new RetryInterceptor()).build();
        }
        this.gson = GsonUtils.getGson();


//        Connection:Keep-Alive
//        Content-Encoding:gzip
//        Content-Language:zh-CN
//        Content-Type:text/html;charset=GBK
//        Date:Tue, 09 Jan 2018 12:27:59 GMT
//        Keep-Alive:timeout=15, max=9995
//        Server:nginx
//        Transfer-Encoding:chunked
//        Vary:Accept-Encoding
//        X-Application-Context:mssupplypage:80
//        Request Headers
//        view source
//        Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
//Accept-Encoding:gzip, deflate, br
//Accept-Language:zh-CN,zh;q=0.9
//Cache-Control:max-age=0
//Connection:keep-alive
//Cookie:visitid_time=2018-1-8%2014%3A50%3A30; hc360visitid=C7D3D368B500000175A470B027001A45; hc360first_time=2018-01-08; hcbrowserid=C7D3D368B5100001F9F54100291019A9; hckIndex=C7D3D368B5300001302410632D20A3B0; hc360firstvisittime=1515394231326; hc360firstvisittime=1515394231326; hccordet=00; hcpreurl=; hc360analyid=C7D4385055E00001342813701F368800; hc360analycopyid=C7D4385055E000012E6538F0BA3C185C; hc360sessionid=C7D4385057900001894E1980CAD0FC50; Hm_lvt_e1e386be074a459371b2832363c0d7e7=1515394234,1515500037; hcsearchurlport=1; hclastsearchkeyword=%u6C34%u6CE5; productHistory=646784231%23%26%23%u534E%u6DA6-325%20%20%20%20%20%u534E%u6DA6%u6C34%u6CE5%20%20%20%20%20%20%20%u5DE5%u5320%u88C5%u4FEE%u6C34%u6CE5%20%20%20%u6C34%u6CE5%u5382%u5BB6%20%20%u6C34%u6CE5%u6279%u53D1%23%26%23//img000.hc360.cn/k1/M08/D7/6B/wKhQw1k1-qCEXr55AAAAAMwaOxE636.jpg%23%26%23%A59%20-%20%A510.00%23%26%231@645312358%23%26%23%u6C5F%u82CF%u7701%20%u5357%u4EAC%u5E02%u94C1%u827A%u62A4%u680F%7C%u5E02%u653F%u62A4%u680F%7C%u9053%u8DEF%u62A4%u680F%7C%u950C%u94A2%u62A4%u680F%7C%u56F4%u680F%u7F51%23%26%23//img003.hc360.cn/k1/M03/B5/A8/wKhQw1krhxWEaq1DAAAAAMUmt48906.jpg%23%26%23%A530.00%23%26%231%3B%26%3B; hc5minbeat=1515500852513; Hm_lvt_c1bfff064e4a03c5b6f2b589e099da36=1515394234,1515500754,1515500853; Hm_lpvt_c1bfff064e4a03c5b6f2b589e099da36=1515500853; Hm_lpvt_e1e386be074a459371b2832363c0d7e7=1515500853
//Host:b2b.hc360.com
//Referer:https://s.hc360.com/?w=%CB%AE%C4%E0&mc=seller&ap=B&pab=B
//Upgrade-Insecure-Requests:1
//User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36


        Headers.Builder headsBuilder = new Headers.Builder();
        headsBuilder.add("Connection", "Keep-Alive");
        headsBuilder.add("Content-Language", "zh-CN");
        headsBuilder.add("Content-Type", "text/html;charset=GBK");
        headsBuilder.add("Date", "Tue, 09 Jan 2018 12:27:59 GMT");
        headsBuilder.add("Keep-Alive", "timeout=15, max=9995");
        headsBuilder.add("Server", "nginx");
        headsBuilder.add("Referer", "https://s.hc360.com/?w=%CB%AE%C4%E0&mc=seller&ap=B&pab=B");
        headsBuilder.add("Cookie", "visitid_time=2018-1-8%2014%3A50%3A30; hc360visitid=C7D3D368B500000175A470B027001A45; hc360first_time=2018-01-08; hcbrowserid=C7D3D368B5100001F9F54100291019A9; hckIndex=C7D3D368B5300001302410632D20A3B0; hc360firstvisittime=1515394231326; hc360firstvisittime=1515394231326; hccordet=00; hcpreurl=; hc360analyid=C7D4385055E00001342813701F368800; hc360analycopyid=C7D4385055E000012E6538F0BA3C185C; hc360sessionid=C7D4385057900001894E1980CAD0FC50; Hm_lvt_e1e386be074a459371b2832363c0d7e7=1515394234,1515500037; hcsearchurlport=1; hclastsearchkeyword=%u6C34%u6CE5; productHistory=646784231%23%26%23%u534E%u6DA6-325%20%20%20%20%20%u534E%u6DA6%u6C34%u6CE5%20%20%20%20%20%20%20%u5DE5%u5320%u88C5%u4FEE%u6C34%u6CE5%20%20%20%u6C34%u6CE5%u5382%u5BB6%20%20%u6C34%u6CE5%u6279%u53D1%23%26%23//img000.hc360.cn/k1/M08/D7/6B/wKhQw1k1-qCEXr55AAAAAMwaOxE636.jpg%23%26%23%A59%20-%20%A510.00%23%26%231@645312358%23%26%23%u6C5F%u82CF%u7701%20%u5357%u4EAC%u5E02%u94C1%u827A%u62A4%u680F%7C%u5E02%u653F%u62A4%u680F%7C%u9053%u8DEF%u62A4%u680F%7C%u950C%u94A2%u62A4%u680F%7C%u56F4%u680F%u7F51%23%26%23//img003.hc360.cn/k1/M03/B5/A8/wKhQw1krhxWEaq1DAAAAAMUmt48906.jpg%23%26%23%A530.00%23%26%231%3B%26%3B; hc5minbeat=1515500852513; Hm_lvt_c1bfff064e4a03c5b6f2b589e099da36=1515394234,1515500754,1515500853; Hm_lpvt_c1bfff064e4a03c5b6f2b589e099da36=1515500853; Hm_lpvt_e1e386be074a459371b2832363c0d7e7=1515500853");
        headsBuilder.add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.221 Safari/537.36 SE 2.X MetaSr 1.0");
        headers = headsBuilder.build();
    }
}
