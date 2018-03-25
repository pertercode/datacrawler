package services;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import com.google.gson.Gson;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.IDUtils;
import utils.LogUtils;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2018/3/13.
 */
public class YuanLinService {

    private LogUtils log = new LogUtils(platform, YuanLinService.class);

    public static final String BASE_URL = "http://www.yuanlin.com/";

    public static final String platform = "yuanlin";

    private BaseDao baseDao = new BaseDao();


    /**
     * 查询所有1J分类
     *
     * @return
     */
    public List<Category> requestCategory1() {

        List<Category> categories = null;
        String url = "http://www.yuanlin.com/b2b/category1.html";
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {

                categories = new ArrayList<Category>();
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);
                Elements elements = doc.select("dt");
                int size = elements.size();
                for (int i = 0; i < size; i++) {
                    Element dt = elements.get(i);
                    String Level1name = dt.text().trim();
                    String level1Cid = IDUtils.uuid();
                    String level1_id = IDUtils.genId(platform, level1Cid);
                    Category categoryLevel1 = new Category(level1_id, platform, level1Cid, Level1name, 0, 0, "0");
                    baseDao.categoryReplace(categoryLevel1);
                    log.i("1分类入库了");

                    categories.addAll(requestCategory2(level1_id, i));
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

    public List<Category> requestCategory2(String id, int i) {
        List<Category> categories = null;
        String url = "http://www.yuanlin.com/b2b/category2.html";

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                categories = new ArrayList<Category>();
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);
                Elements elements = doc.select("div");
                Elements dls = elements.get(i).select("dl");

                for (int j = 0; j < dls.size(); j++) {
                    Element dl = dls.get(j);
                    String tagName = dl.select("dt").get(0).text().trim();
                    if (tagName.equals("常用")) {
                        continue;
                    }

                    Elements as = dl.select("dd a");

                    for (int k = 0; k < as.size(); k++) {
                        Element a = as.get(k);
                        String Level2name = a.text().trim();

                        if (StringUtils.isEmpty(Level2name))
                            continue;

                        String level2Cid = a.attr("href").trim();

                        String href2 = "http://www.yuanlin.com/b2b/" + level2Cid;

                        String level2_id = IDUtils.genId(platform, level2Cid);

                        Category categoryLevel2 = new Category(level2_id, platform, level2Cid, Level2name, 1, 1, id);
                        categoryLevel2.setC_url(href2);
                        baseDao.categoryReplace(categoryLevel2);

                        log.i("2分类入库了 , 名称 =  " + categoryLevel2.getC_name());
                        categories.add(categoryLevel2);
                    }
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


    /**
     * 查询分页商品数据
     *
     * @param category
     * @param page
     * @return
     */
    public List<ProduceInfo> requestProduces(Category category, Integer page) {
        List<ProduceInfo> produceInfos = null;
        if (page == null)
            page = 1;

        String cid = category.getC_id();

        String url = "http://www.yuanlin.com/b2b/" + cid;
        url = url.replaceAll("\\d+\\.html", page + ".html");

        Integer totalPage = null;
        final Request request = new Request.Builder().headers(HttpUtils.getCommonHeaders()).header("Referer", BASE_URL).url(url).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                produceInfos = new ArrayList<ProduceInfo>();
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);

                Elements produceElements = doc.select(".list_sell li");

                if (produceElements.size() < 1) {
                    return produceInfos;
                }

                for (int i = 0; i < produceElements.size(); i++) {
                    Element produceElement = produceElements.get(i);

                    Element aTag = produceElement.select(".item_title a").get(0);

                    String href = BASE_URL + aTag.attr("href").trim();

                    String p_id = href.split("_")[2];

                    p_id = p_id.split(".html")[0];

                    Element imgTag = produceElement.select("img").get(0);

                    String imgSrc = imgTag.attr("src").trim();

                    String price = "";

                    String pName = aTag.text().trim();

                    // 商品URL
                    String pUrl = href;

                    Element pCompanyTag = produceElement.select(".item_user .a_user a").get(0);

                    String pCompanyName = pCompanyTag.text().trim();

                    ProduceInfo produceInfo = new ProduceInfo(IDUtils.genId(platform, p_id), page, null, p_id, pName, pUrl, price, imgSrc);

                    produceInfo.setpCUrl(pCompanyTag.attr("href"));

                    if (totalPage == null) {
                        if (doc.select("#WxfPageControl1 .info").size() != 0) {
                            String totalPageStr = doc.select("#WxfPageControl1 .info").get(0).text().trim().split(":")[1].split("页")[0];
                            totalPage = Integer.parseInt(totalPageStr);
                        } else {
                            totalPage = 1;
                        }
                    }
                    produceInfo.setcName(pCompanyName);
                    produceInfo.setTotalPage(totalPage);
                    produceInfos.add(produceInfo);
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

    /**
     * 查询企业信息
     *
     * @param url
     */
    public CompanyInfo requestCompany(String url) {

        CompanyInfo companyInfo = null;

        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(url)
                .build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);

                Element box = findBox(doc);

                String text = box.text();

                text = text.replaceAll(Jsoup.parse("&nbsp;").text(), "").replaceAll("\\s+", "")
                        .replaceAll(Jsoup.parse("&#12288;").text(), "");

                companyInfo = new CompanyInfo();

                String cid = url.replace("http://", "").replace("https://", "");

                String _id = IDUtils.genId(platform, cid);

                companyInfo.setcId(cid);
                companyInfo.set_id(_id);


                String[] arr = text.split("：");

                String local = "";

                for (int i = 0; i < arr.length; i++) {

                    String a = arr[i].trim();

                    if (i == 0) {
                        String cName = a.replaceAll("联系方式|第\\d+年", "").replaceAll("联系人|邮编|电话|传真|微信|手机|其他号码|地址|所在地|认证", "").replace("E-mail", "");
                        ;
                        companyInfo.setcName(cName);
                    }

                    String n = a;


                    String v = "";

                    if ((i + 1) < arr.length) {
                        v = arr[i + 1];
                        v = v.replaceAll("联系人|邮编|电话|传真|微信|手机|其他号码|地址|所在地|认证", "").replace("E-mail", "");
                    }

                    if (StringUtils.isEmpty(v)) {
                        continue;
                    }

                    if (n.indexOf("手机") > -1) {
                        companyInfo.setcPhone(v);
                    }

                    if (n.indexOf("电话") > -1) {
                        companyInfo.setcMobile(v);
                    }

                    if (n.indexOf("联系人") > -1) {
                        companyInfo.setcConcat(v);
                    }

                    if (n.indexOf("所在地") > -1) {
                        local = v;
                    }

                    if (n.indexOf("地址") > -1) {
                        companyInfo.setcAddress(v);
                    }

                }

                if (!StringUtils.isEmpty(local)) {
                    companyInfo.setcAddress(local + (StringUtils.isEmpty(companyInfo.getcAddress()) ? "" : companyInfo.getcAddress()));
                }

            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(e.getMessage() + "\n" + str, e);
                companyInfo = null;
            }
        } else {
            String str = HttpUtils.errorStringNoBody(responseWrap);
            log.e(str, responseWrap.e);
            companyInfo = null;
        }

        return companyInfo;
    }


    /**
     * 获得联系人模块
     *
     * @return
     */
    public Element findBox(Document doc) {
        Element element = null;

        Elements boxs = doc.select(".box");
        if (boxs.size() > 0) {
            for (int i = 0; i < boxs.size(); i++) {
                Element box = boxs.get(i);
                String text = box.text().trim();

                if (text.contains("联系")) {
                    return box;
                }

            }
        }

        return element;


    }

}
