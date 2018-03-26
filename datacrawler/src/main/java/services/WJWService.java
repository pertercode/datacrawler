package services;

import bean.Category;
import bean.ProduceInfo;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.IDUtils;
import utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/9.
 */
public class WJWService {

    private LogUtils log = new LogUtils(WJWService.platform, WJWService.class);


    public static final String BASE_URL = "http://www.wjw.cn";

    public static final String platform = "wujinwang";

    private BaseDao baseDao = new BaseDao();

    //一级分类
    public List<Category> requestCategory() {
        List<Category> categories = null;
        Request request = new Request.Builder().url(BASE_URL).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request, "GBK");
        if (responseWrap.isSuccess()) {
            try {
                categories = new ArrayList<Category>();
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);
                Elements elements1 = doc.select(".ce-list");
                for (int i = 0; i < elements1.size(); i++) {
                    Element dl = elements1.get(i).select(".cel-main").get(0);
                    Elements title = dl.select(".celm-title");
                    String Level1name = title.select("a").text().trim();
                    String href2 = title.select("a").get(0).attr("href").trim();
                    int index2 = href2.lastIndexOf("/");
                    String level1Cid = href2.substring(index2 + 1);
                    String level1_id = IDUtils.genId(platform, level1Cid);
                    Category categoryLevel1 = new Category(level1_id, platform, level1Cid, Level1name, 0, 0,
                            "0");
                    categoryLevel1.setC_url(href2);
                    categories.add(categoryLevel1);
                }
            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(e.getMessage() + "\n" + str, e);
                categories = null;
            }
        } else {
            String str = HttpUtils.errorStringNoBody(responseWrap);
            log.e(str, responseWrap.e);
            categories = null;
        }
        return categories;
    }


    public List<ProduceInfo> requestProduce(Category c, Integer page) {
        List<ProduceInfo> produceInfos = null;
        if (page == null)
            page = 1;
        String url = c.getC_url() + "/" + page;

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request, "gbk");

        if (responseWrap.isSuccess()) {

            try {
                produceInfos = new ArrayList<ProduceInfo>();
                Document doc = Jsoup.parse(responseWrap.body);
                Elements produceElements = doc.select(".list-hen ul li");

                if (produceElements.size() < 1) {
                    return produceInfos;
                }
                for (int i = 0; i < produceElements.size(); i++) {
                    Elements produceElements1 = produceElements.get(i).select(".lhen-item");

                    for (int j = 0; j < produceElements1.size(); j++) {

                        String pName = produceElements1.get(j).select(".lhitem-cp a").text().trim();
                        System.out.println(pName);
                        String p_img = produceElements1.get(j).select(".lhitem-pic a img").attr("original").trim();
                        //System.out.println(p_img);
                        String href = produceElements1.get(j).select(".lhitem-cp a").attr("href").trim();
                        String p_id = href.split("/")[4];
                        String p_id1 = href.split("/")[5];
                        String p_id2 = p_id + "/" + p_id1;
                        //System.out.println(p_id2);
                        String p_url = produceElements1.get(j).select(".lhitem-pic a ").attr("href").trim();
                        System.out.println(p_url);
                        Request request1 = new Request.Builder().url("http://www.wjw.cn/product/" + p_id2).headers(HttpUtils.getCommonHeaders()).build();
                        HttpUtils.ResponseWrap responseWrap1 = HttpUtils.retryHttp(request1);
                        if (responseWrap1.isSuccess()) {
                            Document doc1 = Jsoup.parse(responseWrap1.body);
                            Elements produceElements11 = doc1.select(".left ul");
                            String pir = null;
                            if (produceElements11.get(0).select("li .Pro_det_7B .Pro_det_7B ").size() > 0) {
                                pir = produceElements11.get(0).select("li .Pro_det_7B .Pro_det_7B ").text();
                                String a1 = pir.split("/")[0];
                                if (a1.equals("0.00元")) {
                                    pir = "需要询价";
                                }
                            } else {
                                pir = produceElements11.get(0).select("li .Pro_det_7B  ").get(0).text();
                                pir = "需要询价";
                            }
                            System.out.println(pir);
                            ProduceInfo produceInfo = new ProduceInfo(IDUtils.genId(platform, p_id2), page, null, p_id2, pName, p_url, pir, p_img);

                            produceInfos.add(produceInfo);
                            baseDao.produceReplace(produceInfo);
                        }
                    }
                }
            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(e.getMessage() + "\n" + str, e);
                produceInfos = null;
            }
        } else {
            String str = HttpUtils.errorStringNoBody(responseWrap);
            log.e(str, responseWrap.e);
            produceInfos = null;
        }

        return produceInfos;

    }

}



