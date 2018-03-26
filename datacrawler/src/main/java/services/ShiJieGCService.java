package services;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import com.google.gson.Gson;
import dao.BaseDao;
import http.HttpUtils;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import okhttp3.Request;
import org.apache.ibatis.io.Resources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.IDUtils;
import utils.LogUtils;
import utils.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 世界工厂 抓取服务
 */
public class ShiJieGCService {

    private LogUtils log = new LogUtils(platform, ShiJieGCService.class);

    public static final String BASE_URL = "https://www.gongchang.com";

    public static final String platform = "shijiegongchang";

    private BaseDao baseDao = new BaseDao();


    /**
     * 查询1J分类
     *
     * @return
     */
    public List<Category> getlv1() {
        List<Category> categoryList = null;
        Request request = new Request.Builder().url(BASE_URL).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        if (responseWrap.isSuccess()) {
            try {
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);

                Elements elements = doc.select(".cate-title-list>div");

                categoryList = new ArrayList<Category>();

                for (int i = 0; i < elements.size() - 1; i++) {

                    Element lv1 = elements.get(i).select(".cate-mt .title_cat a").get(0);

                    String level1Cid = lv1.attr("href").split("/")[4];

                    String level1_id = IDUtils.genId(platform, level1Cid);

                    String Level1name = lv1.text().trim();

                    Category Clv1 = new Category(level1_id, platform, level1Cid, Level1name, 0, 0, "0");

                    baseDao.categoryReplace(Clv1);

                    log.i("1分类入库 ： " + Clv1.getC_name());

                    Elements lv2s = elements.get(i).select(".goods-list a");

                    for (int j = 0; j < lv2s.size(); j++) {

                        Element lv2 = lv2s.get(j);

                        String level2Cid = lv2.attr("href").split("/")[4];

//                        level2Cid = IDUtils.genId(level1Cid, level2Cid);

                        String level2_id = IDUtils.genId(platform, level2Cid);

                        String level2Name = lv2.text().trim();

                        Category Clv2 = new Category(level2_id, platform, level2Cid, level2Name, 1, 0, Clv1.get_id());

                        Clv2.setC_url(lv2.attr("href"));


                        // 检查三级分类
                        List<Category> level3List = getlv3(Clv2);

                        if (level3List != null) {
                            if (level3List.size() < 1) {
                                Clv2.setC_islow(1);
                                categoryList.add(0, Clv2);
                            } else
                                categoryList.addAll(level3List);
                        }


                        baseDao.categoryReplace(Clv2);
                        log.i("2分类入库 ： " + Clv2.getC_name() + " , parent = " + Clv1.getC_name());


                    }

                }
            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(e.getMessage() + "\n" + str, e);
                categoryList = null;
            }
        } else {
            String str = HttpUtils.errorStringNoBody(responseWrap);
            log.e(str, responseWrap.e);
            categoryList = null;
        }

        return categoryList;
    }

    /**
     * 查询第三级分类  返回连接
     */
    public List<Category> getlv3(Category category) {
        String url = category.getC_url();

        List<Category> categories = null;
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                categories = new ArrayList<Category>();
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);

                boolean isLow = check(doc);

                if (isLow) {
                    return categories;
                }

                Elements li = doc.select(".product_option>div").get(2).select("li");

                for (int i = 1; i < li.size(); i++) {
                    Element lv3 = li.get(i).select("a").get(0);
                    String text = lv3.text().trim();
                    String leve13name = text.split("\\(")[0];
                    String href3 = lv3.attr("href").trim();
                    String level3Cid = href3.split("/")[4];

//                    level3Cid = IDUtils.genId(category.getC_id(), level3Cid);

                    String level3_id = IDUtils.genId(platform, level3Cid);
                    Category categoryLevel3 = new Category(level3_id, platform, level3Cid, leve13name, 2, 0, category.get_id());
                    categoryLevel3.setC_url(href3);

                    List<Category> categoryList = getlv4(categoryLevel3);

                    if (categoryList != null) {
                        if (categoryList.size() < 1) {
                            categoryLevel3.setC_islow(1);
                            categories.add(0, categoryLevel3);
                        }

                        baseDao.categoryReplace(categoryLevel3);
                        log.i("3分类入库 ： " + categoryLevel3.getC_name() + " , parent =  " + category.getC_name());

                        if (categoryList.size() > 0)
                            categories.addAll(categoryList);


                    } else {
                        throw new RuntimeException("getlv4 return null , paran category = " + new Gson().toJson(categoryLevel3));
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
     * 查询第四级分类  返回连接
     */
    public List<Category> getlv4(Category category) {
        List<Category> categories = null;
        Request request = new Request.Builder().url(category.getC_url()).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        if (responseWrap.isSuccess()) {
            try {
                categories = new ArrayList<Category>();
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);

                boolean isLow = check(doc);

                if (isLow)
                    return categories;


                Elements li = doc.select(".product_option>div").get(2).select("li");

                for (int i = 1; i < li.size(); i++) {
                    Element lv4 = li.get(i).select("a").get(0);
                    String text = lv4.text().trim();
                    String leve14name = text.split("\\(")[0];
                    String href4 = lv4.attr("href").trim();
                    String level4Cid = href4.split("/")[4];

//                    level4Cid = IDUtils.genId(category.getC_id(), level4Cid);

                    String level4_id = IDUtils.genId(platform, level4Cid);
                    Category categoryLevel4 = new Category(level4_id, platform, level4Cid, leve14name, 3, 1, category.get_id());
                    categoryLevel4.setC_url(href4);
                    baseDao.categoryReplace(categoryLevel4);
                    categories.add(categoryLevel4);
                    log.i("4分类入库 ： " + categoryLevel4.getC_name() + " , parent =  " + category.getC_name());
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
     * 检测是否为最低等级，判断当前文档的选中状态，如果是有2已选中，说明已是最底层
     */
    public boolean check(Document doc) {
        boolean res = false;
        try {
            if (doc.select(".product_option>div").get(2).select(".product_option1C3").size() >= 2) {
                res = true;
            }
        } catch (Exception e) {
            log.e(e.getMessage(), e);
        }
        return res;
    }


    /**
     * 查询产品信息
     */
    public List<ProduceInfo> getProduceInfo(Category category, Integer page) {
        List<ProduceInfo> produceInfos = null;
        if (page == null)
            page = 1;

        String url = category.getC_url() + "/" + page;
        Integer totalPage = null;
        final Request request = new Request.Builder().headers(HttpUtils.getCommonHeaders()).header("Referer", BASE_URL).url(url).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                produceInfos = new ArrayList<ProduceInfo>();
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements produceElements = doc.select(".product_content .extension_ul li");

                if (produceElements.size() < 1) {
                    // 没有数据
                    return produceInfos;
                }

                for (int i = 0; i < produceElements.size(); i++) {

                    Element produceElement = produceElements.get(i);
                    Element aTag = produceElement.select(".title_list a").get(0);
                    String href = aTag.attr("href").trim();
                    String p_id = href.split("/")[4];

                    Element imgTag = produceElement.select(".img img").get(0);
                    String imgSrc = imgTag.attr("src").trim();

                    Element priceTag = produceElement.select(".su-price").get(0);
                    String price = priceTag.text().trim();
                    String pName = aTag.text().trim();
                    // 商品URL
                    String pUrl = href;

                    Element pCompanyTag = produceElement.select(".extension_right p").get(0).select("a").get(0);
                    String pCompanyName = pCompanyTag.text().trim();
                    ProduceInfo produceInfo = new ProduceInfo(IDUtils.genId(platform, p_id), page, null, p_id, pName, pUrl, price, imgSrc);
                    produceInfo.setpCUrl(pCompanyTag.attr("href"));

                    if (totalPage == null) {
                        totalPage = 1;
                        if (doc.select(".pages cite").size() > 0) {
                            String totalPageStr = doc.select(".pages cite").get(0).text().trim().split("/")[1].split("页")[0];
                            totalPage = Integer.parseInt(totalPageStr);
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
     * 查询公司信息
     */
    public CompanyInfo getCompanyInfo(String url) {
        CompanyInfo companyInfo = null;
        url = url + "contact/";
        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(url)
                .build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);

                String title = doc.select("title").text().trim();

                if (title.indexOf("公司不存在") > -1) {
                    return null;
                }

                String _id = doc.select("#menu a").get(0).attr("href").split("https://")[1];
                if (_id.startsWith("qiye.gongchang.com")) {
                    _id = _id.split("/")[1];
                } else {
                    _id = _id.split(".gongchang")[0];
                }
                Elements c = doc.select("#main").get(0).select(" .main_body").get(0).select("table").get(0).select("tr");
                String cName = "";
                String cAddress = "";
                String mobile = "";
                String cContactName = "";
                String cPhone = "";
                String qq = "";
                String local = "";
                for (int i = 0; i < c.size(); i++) {
                    Element co = c.get(i);


                    if (co.select("td").get(0).text().trim().startsWith("所在地区"))
                        local = co.select("td").get(1).text().trim();


                    if (co.select("td").get(0).text().trim().startsWith("公司名称"))
                        cName = co.select("td").get(1).text().trim();
                    if (co.select("td").get(0).text().trim().startsWith("公司地址"))
                        cAddress = co.select("td").get(1).text().trim();
                    if (co.select("td").get(0).text().trim().startsWith("公司电话")) {
                        String murl = co.select("td").get(1).select("img").attr("src").trim();
                        mobile = searchPhone(murl, url);
                    }
                    if (co.select("td").get(0).text().trim().startsWith("联 系 人"))
                        cContactName = co.select("td").get(1).text().trim();
                    if (co.select("td").get(0).text().startsWith("手机")) {
                        String purl = co.select("td").get(1).select("img").attr("src").trim();
                        cPhone = searchPhone(purl, url);
                    }
                    if (co.select("td").get(0).text().trim().startsWith("即时通讯") && co.select("td").get(1).select("a").size() == 2)
                        qq = co.select("td").get(1).select("a").get(1).attr("href").split("&")[1].split("=")[1];

                }

                if (!StringUtils.isEmpty(local)) {
                    cAddress = local + cAddress;
                }

                companyInfo = new CompanyInfo(IDUtils.genId(platform, _id), _id, cName, cContactName, mobile, cPhone, qq, cAddress);

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
     * 查询电话
     */
    public String searchPhone(String url, String hurl) {
        String phone = "";

        if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(hurl)) {
            try {
                //创建url
                Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders())
                        .addHeader("referer", hurl)
                        .addHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.221 Safari/537.36 SE 2.X MetaSr 1.0")
                        .build();

                //发送请求
                HttpUtils.ResponseWrap responseWrap = HttpUtils.download(request);

                InputStream is = responseWrap.inputStream;

                BufferedImage bd = ImageIO.read(is);

                ITesseract instance = new Tesseract();  // JNA Interface Mapping

                File tessDataFolder = Resources.getResourceAsFile("tessdata").getAbsoluteFile();
                instance.setDatapath(tessDataFolder.getAbsolutePath());
                instance.setLanguage("new");
                phone = instance.doOCR(bd);

                is.close();
                responseWrap.response.close();
            } catch (Exception e) {
                log.e(e.getMessage() + " , url  =  " + url + ", hurl ： " + hurl, e);
                phone = "";
            }
        }

        return phone.trim().replaceAll("\\s*|\\t|\\r|\\n", "");
    }

}
