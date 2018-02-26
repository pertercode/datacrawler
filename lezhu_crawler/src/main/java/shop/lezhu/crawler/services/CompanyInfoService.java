package shop.lezhu.crawler.services;

import com.google.gson.JsonParseException;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import shop.lezhu.crawler.Main;
import shop.lezhu.crawler.bean.CompanyInfoBean;
import shop.lezhu.crawler.bean.SearchBean;
import shop.lezhu.crawler.bean.SearchJSONBean;
import shop.lezhu.crawler.utils.ConfigUtils;
import shop.lezhu.crawler.utils.GsonUtils;
import shop.lezhu.crawler.utils.LogUtils;
import shop.lezhu.crawler.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompanyInfoService extends BaseService {

    private List<String> ids = new ArrayList<String>();

    private void addId(String id) {
        if (!ids.contains(id)) {
            ids.add(id);
        }
    }


    /**
     * 从慧聪网加载企业详情
     *
     * @param id : 企业详情
     * @return
     */
    public CompanyInfoBean requestComponyInfoWithId(String id) {
        // 休息一会防止封杀
        randomSleep();

        String url = "https://b2b.hc360.com/supplyself/" + id + ".html";
        CompanyInfoBean infoBean = null;
        String responseBody = null;

        try {
            final Request request = new Request.Builder().headers(headers)
                    .url(url)
                    .build();
            Call call = mOkHttpClient.newCall(request);

            Response response = call.execute();

            if (response.isSuccessful()) {
                String html = response.body().string();

                String huiYuan = html.contains("hclogo-small") + "";

                Pattern pattern = Pattern.compile("var\\s*companyJson\\s*=\\s*(\\{.*\\})");

                Matcher matcher = pattern.matcher(html);

                if (matcher.find()) {
                    responseBody = matcher.group(1);
                    infoBean = gson.fromJson(responseBody, CompanyInfoBean.class);
                    infoBean.setId(id);
                    infoBean.setIsAuth(huiYuan);
                }


            }
        } catch (JsonParseException e) {
            String[] msg = new String[]{"URL = " + url, "Response = " + responseBody};
            Main.mainForm.printLog(msg, e);
        } catch (IOException e) {
            String msg = "URL = " + url.toString();
            Main.mainForm.printLog(msg, e);
        } catch (Exception e) {
            Main.mainForm.printLog(LogUtils.getStackTraceString(e));
        }
        return infoBean;
    }


    /**
     * 根据关键词和地址请求企业得ID列表
     *
     * @param page     : 页数，每页都有多个片段
     * @param location : 位置
     * @param key      : 搜索关键词
     */
    public String[] requestComponyIdsWithPage(String key, String location, String page) {
        ids.clear();
        requestComponyIdsWithPage(key, location, page, null);
        int maxFragmentId = 4;
        for (int i = 3; i <= maxFragmentId; i++) {
            requestComponyIdsWithPage(key, location, page, i + "");
        }
        String[] result = new String[ids.size()];
        ids.toArray(result);
        ids.clear();
        return result;
    }


    /**
     * 根据关键词请求企业列表, 每一个 page 有2个片段
     *
     * @param page       ： 页数
     * @param fragmentId ： 片段ID
     * @param key        ： 搜索关键词
     * @param location   ： 位置
     */
    private void requestComponyIdsWithPage(String key, String location, String page, String fragmentId) {

        final String charset = "gb2312";

        String keyCode = StringUtils.URLEncoder(key, charset);

        String locationCode = StringUtils.URLEncoder(location, charset);

        String url = "https://s.hc360.com/?1=1" + (keyCode.length() > 0 ? "&w=" + keyCode : "")
                + "&mc=seller&P=1&ee=" + page + "&ap=B&pab=B&q=1&t=1" + (location.length() > 0 ? "&z=" + locationCode : "") + ((fragmentId == null || fragmentId.length() < 1) ? "" : "&af=" + fragmentId);

        try {
            //创建一个Request
            final Request request = new Request.Builder().headers(headers)
                    .url(url)
                    .build();
            //new call
            Call call = mOkHttpClient.newCall(request);

            Response response = call.execute();

            if (response.isSuccessful()) {
                String html = response.body().string();

                Pattern pattern = Pattern.compile(".*data-detailbcid=\"(\\d+)\".*data-useractivelogs=\".*UserBehavior_s_title.*\"");

                Matcher matcher = pattern.matcher(html);

                while (matcher.find()) {
                    String componyId = matcher.group(1);
                    addId(componyId);
                }
            }
        } catch (IOException e) {
            String msg = "URL = " + url.toString();
            Main.mainForm.printLog(msg, e);
        } catch (Exception e) {
            Main.mainForm.printLog(LogUtils.getStackTraceString(e));
        }
    }


    /**
     * 随机睡眠
     */
    private Random random = new Random(3);

    public void randomSleep() {
        double sleep = random.nextDouble() + 1;
        try {
            Thread.sleep(Math.round(sleep * 1000));
        } catch (InterruptedException e) {
            Main.mainForm.printLog(LogUtils.getStackTraceString(e));
        }
    }


    /**
     * 请求服务器上待爬虫搜索得关键字
     *
     * @return : 关键字集合,如果集合为0则说明没有待搜索得关键字
     */
    public List<SearchBean> requestSearchBeans() {
        List<SearchBean> searchBeans = new ArrayList<SearchBean>();
        String url = ConfigUtils.getApi() + "glist";
        String responseBody = null;
        try {

            final Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = mOkHttpClient.newCall(request);

            Response response = call.execute();

            if (response.isSuccessful()) {
                responseBody = response.body().string();
                SearchJSONBean bean = GsonUtils.getGson().fromJson(responseBody, SearchJSONBean.class);
                searchBeans = bean.getData();
            }
        } catch (IOException ex) {
            String msg = "URL = " + url;
            Main.mainForm.printLog(msg, ex);
        } catch (JsonParseException ex) {
            String[] msg = new String[]{"URL = " + url, "Response = " + responseBody};
            Main.mainForm.printLog(msg, ex);
        }
        return searchBeans;
    }


    /**
     * 将已抓取得KEY设置为已抓取状态
     *
     * @param bean : 搜索信息Bean
     * @return ： 是否已设置为抓取状态
     */
    public boolean setExecute(SearchBean bean) {
        boolean result = false;
        String urlStr = ConfigUtils.getApi() + "setexecute";
        HttpUrl url = HttpUrl.parse(urlStr).newBuilder()
                .addQueryParameter("keyword", bean.getKey())
                .addQueryParameter("region", bean.getLocation())
                .addQueryParameter("company", bean.getCompany()).build();
        try {
            //创建一个Request
            final Request request = new Request.Builder()
                    .url(url)
                    .build();

            //new call
            Call call = mOkHttpClient.newCall(request);

            Response response = call.execute();

            if (response.isSuccessful()) {
                result = true;
            }
        } catch (IOException ex) {
            String msg = "URL = " + url.toString();
            Main.mainForm.printLog(msg, ex);
        }
        return result;
    }

}
