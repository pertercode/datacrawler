package services;

import bean.*;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.IDUtils;
import utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 云筑商城 抓取服务
 */
public class YunZhuService {

    private LogUtils log = new LogUtils(platform, YunZhuService.class);


    public static final String BASE_URL = "https://mall.yzw.cn";

    public static final String platform = "yunzhushangcheng";

    private BaseDao baseDao = new BaseDao();

    /**
     * 查询所有分类，返回所有最低级分类
     *
     * @return 返回所有最底级分类
     */
    public List<Category> requestCategory() {
        List<Category> categories = null;
        String url = "https://mall.yzw.cn/";
        // 把URL创建好了
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        // 发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        if (responseWrap.isSuccess()) {
            categories = new ArrayList<Category>();
            String body = responseWrap.body;
            Document doc = Jsoup.parse(body);
            Elements elements = doc.select(".nav-details");
            int size = elements.size();
            for (int i = 0; i < size; i++) {
                Element e = elements.get(i).select("dl").get(0);
                Element li = e.select("dt").get(0);
                String Level1name = li.text().trim();
                String level1Cid = IDUtils.uuid();
                String level1_id = IDUtils.genId(platform, level1Cid);
                Category categoryLevel1 = new Category(level1_id, platform, level1Cid, Level1name, 0, 0, "0");
                baseDao.categoryReplace(categoryLevel1);
                log.i("1分类入库了 , 名称 =  " + categoryLevel1.getC_name() + " ,  parent = " + categoryLevel1.getC_parent());
                Elements elements2 = e.select("dd");
                int size2 = elements2.size();
                for (int j = 0; j < size2; j++) {
                    Element dd = elements2.get(j);
                    Element level2Element = dd.select(".nav-sub-title").get(0);
                    String Level2name = level2Element.text().trim();
                    String href = level2Element.select("a").get(0).attr("href").trim();

                    href = BASE_URL + href;

                    int index = href.lastIndexOf('/');
                    String level2Cid = href.substring(index + 1);
                    String level2_id = IDUtils.genId(platform, level2Cid);
                    Category categoryLevel2 = new Category(level2_id, platform, level2Cid, Level2name, 1, 0, categoryLevel1.get_id());

                    log.i("2分类入库了 , 名称 =  " + categoryLevel2.getC_name() + " ,  parent = " + categoryLevel2.getC_parent());

                    Elements elements3 = dd.select(".hints");
                    int size3 = elements3.size();

                    if (size3 < 1) {
                        categoryLevel2.setC_islow(1);
                        categories.add(categoryLevel2);
                    }

                    baseDao.categoryReplace(categoryLevel2);


                    if (size3 > 0) {
                        for (int k = 0; k < size3; k++) {
                            Elements elements4 = elements3.select("a");
                            int size4 = elements4.size();
                            for (int l = 0; l < size4; l++) {
                                Element level3Element = elements4.get(l);
                                String Level3name = level3Element.text().trim();
                                String href3 = level3Element.attr("href").trim();

                                href3 = BASE_URL + href3;

                                int index3 = href3.lastIndexOf('-');
                                String level3Cid = href3.substring(index3 + 1);
                                String level3_id = IDUtils.genId(platform, level3Cid);
                                Category categoryLevel3 = new Category(level3_id, platform, level3Cid, Level3name, 2, 1, categoryLevel2.get_id());
                                categoryLevel3.setC_url(href3);
                                baseDao.categoryReplace(categoryLevel3);
                                log.i("3分类入库了 , 名称 =  " + categoryLevel3.getC_name() + " ,  parent = " + categoryLevel3.getC_parent());
                                categories.add(categoryLevel3);
                            }
                        }
                    }
                }
            }
        } else {
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
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
        String url = category.getC_url() + "?page=" + (page - 1);
        Integer totalPage = null;
        final Request request = new Request.Builder().headers(HttpUtils.getCommonHeaders()).header("Referer", BASE_URL).url(url).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        if (responseWrap.isSuccess()) {
            produceInfos = new ArrayList<ProduceInfo>();
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements produceElements = doc.select(".product-list li");
                if (produceElements.size() < 1) {
                    // 没有数据
                    return produceInfos;
                }
                for (int i = 0; i < produceElements.size(); i++) {
                    Element produceElement = produceElements.get(i);
                    Element aTag = produceElement.select("a").get(0);
                    String href = aTag.attr("href").trim();
                    String p_id = href.substring(href.lastIndexOf('/') + 1);
                    Element imgTag = produceElement.select("img").get(0);
                    String imgSrc = imgTag.attr("data-original").trim();

                    Element priceTag = produceElement.select(".blue-font").get(0);
                    String price = priceTag.text().trim();
                    Element pNameTag = produceElement.select(".item-info a").get(0);
                    String pName = pNameTag.text().trim();
                    // 商品URL
                    String pUrl = pNameTag.attr("href").trim();
                    pUrl = BASE_URL + pUrl;

                    Element pCompanyTag = produceElement.select(".company").get(0);
                    String pCompanyName = pCompanyTag.text().trim();
                    ProduceInfo produceInfo = new ProduceInfo(IDUtils.genId(platform, p_id), page, null, p_id, pName, pUrl, price, imgSrc);
                    produceInfo.setpCUrl(pUrl);
                    if (totalPage == null) {
                        String totalPageStr = doc.select(".pagination span span").get(0).text().trim();
                        totalPage = Integer.parseInt(totalPageStr);
                    }
                    produceInfo.setcName(pCompanyName);
                    produceInfo.setTotalPage(totalPage);
                    produceInfos.add(produceInfo);
                }
            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(str, e);
                produceInfos = null;
            }
        } else {
            String str = HttpUtils.errorString(responseWrap);
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
                String _id = doc.select(".site-menu li").get(1).select("a").get(0).attr("href");
                _id = _id.substring(_id.lastIndexOf("/") + 1);
                Elements companyInfoElements = doc.select(".overblock .sidebar");
                Element companyInfoChildElements = companyInfoElements.get(0);
                Elements p = companyInfoChildElements.select("p");
                // 企业名称
                String cName = p.get(0).text().trim();
                // 企业电话
                String mobile = "";
                // 企业地址
                String cAddress = "";
                for (int i = 1; i < p.size(); i++) {
                    if (p.get(i).select("label").size() > 0) {
                        String text = p.get(i).select("label").get(0).text().trim();
                        if (text.equals("电话：")) {
                            mobile = p.get(i).select("span").get(0).text().trim();
                        } else if (text.equals("地区：")) {
                            cAddress = p.get(i).select("span").get(0).text().trim();
                        }
                    }
                }
                p = companyInfoChildElements.select(".connect-way p");
                String cContactName = "";
                for (int i = 0; i < p.size(); i++) {
                    String text = p.get(i).text();
                    if (text.split("：")[0].equals("联系人")) {
                        cContactName = p.get(i).select("span").get(0).text().trim();
                    }
                }
                String qq = "";
                String cPhone = companyInfoChildElements.select(".connect-way .tel").get(0).text().trim();
                companyInfo = new CompanyInfo(IDUtils.genId(platform, _id), _id, cName, cContactName, mobile, cPhone, qq, cAddress);
            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(str, e);
                companyInfo = null;
            }
        } else {
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }
        return companyInfo;
    }

    /**
     * 获取规格
     *
     * @param
     */
    public void requestTypeName(Category category) {
        String url = category.getC_url();
        //创建url
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        //发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        //如果请求成功
        if (responseWrap.isSuccess()) {
            baseDao.typeNameDelete(category.get_id());
            baseDao.typeValueDelete(category.get_id());
            Document doc = Jsoup.parse(responseWrap.body);
            Elements elements = doc.select(".filterbox .filter-conditions");
            for (Element e : elements) {
                //类型名
                String tName = e.select(".conditions-name").text().trim().replace("：", "");
                //_id
                String t_id = IDUtils.genId(platform, tName + "_" + category.getC_id());
                TypeName typeName = new TypeName();
                typeName.set_id(t_id);
                typeName.setCategoryId(category.get_id());
                typeName.settName(tName);
                baseDao.typeNameReplace(typeName);
                log.i("规格类型入库了 , 名称 =  " + typeName.gettName());
                Elements valueElements = e.select("ul[class=\"conditions-list product-property\"] li");
                for (int i = 1; i < valueElements.size(); i++) {
                    //值的名称
                    String vName = valueElements.get(i).select("a").text().trim();
                    String vHref = valueElements.get(i).select("a").attr("href").trim();
                    //值的Id
                    String vId = vHref.split("\\?")[1].split("=")[1].trim();
                    //值的_id
                    String v_id = IDUtils.genId(platform, vId);
                    TypeValue typeValue = new TypeValue();
                    typeValue.set_id(v_id);
                    typeValue.settValue(vName);
                    typeValue.setCategoryId(category.get_id());
                    typeValue.setTypeNameId(typeName.get_id());
                    baseDao.typeValueReplace(typeValue);
                    log.i("规格的值入库了 , 名称 =  " + typeValue.gettValue() + "\t所属分类是：" + typeValue.getTypeNameId());
                }
            }
        } else {
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }
    }
}
