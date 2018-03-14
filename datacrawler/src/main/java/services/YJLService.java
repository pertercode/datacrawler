package services;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.IDUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * yijianlian 抓取服务
 */
public class YJLService {

    private static final Logger log = Logger.getLogger(YJLService.class);

    private static final String BASE_URL = "http://www.ejianlian.com/";

    private static final String platform = "ejianlian";

    private BaseDao baseDao = new BaseDao();

    /**
     * 查询所有分类，返回所有最低级分类
     *
     * @return 返回所有最底级分类
     */
    public List<Category> requestCategory() {
        List<Category> categories = null;
        String url = "http://www.ejianlian.com/home/market/material";

        try {
            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", BASE_URL)
                    .url(url)
                    .build();
            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
                categories = new ArrayList<Category>();
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements categoryElements = doc.select(".menu_ul .item");

                for (int i = 0; i < categoryElements.size(); i++) {
                    Element categoryMenu = categoryElements.get(i);

                    // 1级分类的名称
                    String level1CategoryName = categoryMenu.select(".item-nav .my").get(0).ownText().trim();

                    // 1J分类ID
                    String cid = null;

                    String cid_level1_uuid = IDUtils.uuid();

                    bean.Category level1 = null;

                    Elements level2Categorys = categoryMenu.select(".item-nav .top-factory a");

                    // 循环获取1J分类下的二级分类
                    for (int j = 0; j < level2Categorys.size(); j++) {
                        Element level2Element = level2Categorys.get(j);

                        // 级别2 ID
                        String level2CategoryId = level2Element.attr("data-id").trim();
                        String level2CategoryName = level2Element.attr("title").trim();

                        String href = BASE_URL.substring(0, BASE_URL.length() - 1) + level2Element.attr("href").trim();

                        // 匹配顶级分类级别1的ID
                        try {
                            // http://www.ejianlian.com/Home/Goods/productlist/cat_id/41/
                            String regex = "cat_id/(\\d+)/";

                            Pattern pattern = Pattern.compile(regex);

                            Matcher matcher = pattern.matcher(href);

                            if (matcher.find()) {
                                cid = matcher.group(1);
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        if (level1 == null) {
                            if (cid == null || cid.length() < 1)
                                cid = cid_level1_uuid;

                            String _id = IDUtils.genId(platform, cid);

                            // 初始化 1J 分类
                            level1 = new bean.Category(_id, platform, cid, level1CategoryName, 0, 0, "0");

                            log.info("1级分类 " + level1CategoryName + " 已入库... ");
                        }

                        // 二级分类
                        bean.Category level2 = new bean.Category(IDUtils.genId(platform, level2CategoryId), platform, level2CategoryId, level2CategoryName, 1, null, level1.get_id());
                        level2.setC_url(href);

                        log.info("2级分类 " + level2CategoryName + " 已入库... parent = " + level1.get_id() + " ,  parent_name = " + level1.getC_name());

                        level1.getCategories().add(level2);

                        // 查询所有三级分类
                        List<Category> level3_categorys = null;

                        try {
                            level3_categorys = requestChildCategory(href, level2);

                            if (level3_categorys == null)
                                throw new RuntimeException("level3_categorys from requestChildCategory is null ,  href  =  " + href);

                            if (level3_categorys.size() > 0) {
                                level2.setC_islow(0);
                                level2.getCategories().addAll(level3_categorys);

                                // 插入三级分类
                                baseDao.categoryReplaceList(level2.getCategories());

                                // 存储三级分类，便于抓取商品
                                categories.addAll(level3_categorys);
                            } else {
                                level2.setC_islow(1);
                                categories.add(level2);
                            }

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }


                    // 插入 Level 1
                    if (level1 != null) {
                        baseDao.categoryReplace(level1);
                        // 插入二级分类
                        baseDao.categoryReplaceList(level1.getCategories());
                    } else {
                        log.error("level1 is null!");
                    }
                }
            } else {
                log.error("responseWrap not success , url  =  " + url);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return categories;
    }


    /**
     * 查询子分类
     *
     * @param url
     * @return
     */
    private List<Category> requestChildCategory(String url, Category parent) {
        List<Category> categories = null;
        try {
            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", BASE_URL)
                    .url(url)
                    .build();

            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements categoryElements = doc.select(".option-default.clearfix.breed_1");

                categories = new ArrayList<Category>();

                // 说明存在三级分类
                if (categoryElements.size() > 1) {

                    Element categoryElement = categoryElements.get(1);

                    categoryElements = categoryElement.select(".clearfix li a");

                    for (int i = 0; i < categoryElements.size(); i++) {

                        categoryElement = categoryElements.get(i);

                        String onclick = categoryElement.attr("onclick");

                        String categoryName = categoryElement.ownText().trim();

                        String regex = ",(\\d+)\\)";

                        String cid = null;

                        // 匹配顶级分类级别1的ID
                        try {
                            // goto('subbreed_2',609)
                            Pattern pattern = Pattern.compile(regex);

                            Matcher matcher = pattern.matcher(onclick);

                            if (matcher.find()) {
                                cid = matcher.group(1);
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        if (cid == null)
                            throw new RuntimeException("未匹配到类别ID , source =  " + onclick + " , regex = " + regex);


                        String href = url + "/subbreed_2/" + cid;

                        bean.Category category = new bean.Category(IDUtils.genId(platform, cid), platform, cid, categoryName, 2, 1, parent.get_id());
                        category.setC_url(href);

                        log.info("3级分类 " + categoryName + " 已入库... parent = " + parent.get_id() + " ,  parent_name = " + parent.getC_name());

                        categories.add(category);
                    }
                }
            } else {
                log.error("responseWrap not success , url  =  " + url);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        String url = category.getC_url() + "/p/" + page + "/sort/2";

        try {
            Integer totalPage = null;

            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", BASE_URL)
                    .url(url)
                    .build();

            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
                produceInfos = new ArrayList<ProduceInfo>();

                try {
                    Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                    Elements produceElements = doc.select("#tab_img.tabitem.tabitem2 .imgitem");

                    for (int i = 0; i < produceElements.size(); i++) {
                        Element produceElement = produceElements.get(i);
                        Element aTag = produceElement.select(".picbox a").get(0);

                        String href = aTag.attr("href").trim();

                        String p_id = href.substring(href.lastIndexOf('/') + 1);

                        Element imgTag = produceElement.select(".picbox img").get(0);

                        String imgSrc = imgTag.attr("src").trim();

                        imgSrc = BASE_URL.substring(0, BASE_URL.length() - 1) + imgSrc;

                        Element priceTag = produceElement.select(".ctxbox .price.f-fl strong").get(0);

                        String price = priceTag.ownText().trim();

                        Element pNameTag = produceElement.select(".ctxbox .row2 a").get(0);

                        String pName = pNameTag.ownText().trim();


                        // 商品URL
                        String pUrl = pNameTag.attr("href").trim();
                        pUrl = BASE_URL.substring(0, BASE_URL.length() - 1) + pUrl;

                        Element pCompanyUrlTag = produceElement.select(".ctxbox .row.row4 a").get(0);


                        // 商品所属企业的信息URL
                        String pCUrl = pCompanyUrlTag.attr("href").trim();
                        pCUrl += "/contact/";


                        Element pCompanyTag = produceElement.select(".ctxbox .row4 a").get(0);

                        String pCompanyName = pCompanyTag.ownText().trim();


                        ProduceInfo produceInfo = new ProduceInfo(IDUtils.genId(platform, p_id), page, null, p_id, pName, pUrl, price, imgSrc);
                        produceInfo.setpCUrl(pCUrl);


                        if (totalPage == null) {
                            String totalPageStr = doc.select(".marrow").get(0).ownText().trim();
                            totalPageStr = totalPageStr.substring(1);
                            totalPage = Integer.parseInt(totalPageStr);
                        }

                        produceInfo.setcName(pCompanyName);

                        produceInfo.setTotalPage(totalPage);

                        produceInfos.add(produceInfo);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);

                    produceInfos = null;
                }
            } else {
                log.error("responseWrap not success , url  =  " + url);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return produceInfos;
    }


    public CompanyInfo requestCompanyInfo(String url) {
        CompanyInfo companyInfo = null;

        try {
            Integer totalPage = null;

            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", BASE_URL)
                    .url(url)
                    .build();

            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
                try {
                    Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                    Element cNameElement = doc.select(".brand h3").get(0);

                    String cName = cNameElement.ownText().trim();

                    Element cidElement = doc.select(".wtbtn").get(0);

                    String companyUrl = cidElement.attr("href");

                    String cid = "";

                    // 匹配顶级分类级别1的ID
                    try {
                        String regex = "shop(\\d+)\\.";

                        Pattern pattern = Pattern.compile(regex);

                        Matcher matcher = pattern.matcher(companyUrl);

                        if (matcher.find()) {
                            cid = matcher.group(1);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    Elements companyInfoElements = doc.select(".contact li");

                    String cContactName = companyInfoElements.get(0).child(1).ownText().trim();

                    String cMobile = companyInfoElements.get(1).child(2).attr("data-tel").trim();

                    String cPhone = cMobile;

                    String cAddress = companyInfoElements.get(2).child(1).ownText().trim();

                    companyInfo = new CompanyInfo(IDUtils.genId(platform, cid), cid, cName, cContactName, cMobile, cPhone, "", cAddress);

                } catch (Exception e) {
                    log.error(e.getMessage() + " url  =  " + url, e);
                    companyInfo = null;


                }
            } else {
                log.error("responseWrap not success , url  =  " + url);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return companyInfo;
    }


    /**
     * 查询企业信息
     *
     * @param url
     */
    public CompanyInfo requestCompany(String url) {
        CompanyInfo companyInfo = null;
        try {
            Integer totalPage = null;

            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", BASE_URL)
                    .url(url)
                    .build();

            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
                try {
                    Document doc = Jsoup.parse(responseWrap.body, BASE_URL);

                    String _id = doc.getElementById("sid").val().trim();

                    Elements companyInfoElements = doc.select(".option-default.clearfix td");

                    if (companyInfoElements.size() < 1)
                        throw new RuntimeException("未找到 企业信息 , companyInfoElements.size()  < 1 , 选择器 = .option-default.clearfix td  url = " + url);

                    Elements companyInfoChildElements = companyInfoElements.get(0).children();

                    if (companyInfoChildElements.size() < 1)
                        throw new RuntimeException("未找到 企业信息 ,  companyInfoChildElements.size()  < 1 , 选择器 = .option-default.clearfix td  url = " + url);

                    // 企业名称
                    String cName = companyInfoChildElements.get(0).ownText().trim();

                    // 企业电话
                    String mobile = companyInfoChildElements.get(1).text().trim();
                    int index = mobile.indexOf('：');

                    if (index >= 0) {
                        mobile = mobile.substring(index + 1).trim();
                    } else {
                        mobile = "";
                    }

                    // 企业地址
                    String cAddress = companyInfoChildElements.get(5).ownText().trim();

                    index = cAddress.indexOf('：');

                    if (index >= 0) {
                        cAddress = cAddress.substring(index + 1).trim();
                    } else {
                        cAddress = "";
                    }

                    Elements lianXiElements = doc.select(".lianxi");

                    if (lianXiElements.size() < 1) {
                        log.warn("未找到 联系人信息 ,  lianXiElements.size()  < 1 , 选择器 = .lianxi");

                        return null;
                    }

                    String cContactName = "";
                    String qq = "";

                    String cPhone = "";

                    for (int i = 0; i < lianXiElements.size(); i++) {
                        Element element = lianXiElements.get(i);

                        Element sourceElement = element;

                        // QQ 相关元素
                        Elements qqElements = element.select("p a");

                        String html = element.html();

                        Pattern p = Pattern.compile("\\<!--([\\s\\S.]+)--\\>");

                        Matcher m = p.matcher(html);
                        if (m.find()) {
                            html = m.group(1);
                            element = Jsoup.parse(html);
                        }

                        Elements lianXiInfoElements = element.select(".telbtn");

                        String tempContactName = "无";
                        String tempPhone = "无";

                        if (lianXiInfoElements.size() < 1) {

                            tempContactName = sourceElement.child(0).ownText().trim();
                            index = tempContactName.indexOf('：');

                            if (index >= 0) {
                                tempContactName = tempContactName.substring(index + 1).trim();
                                tempContactName = tempContactName.trim();
                            } else {
                                tempContactName = "无";
                            }

                            tempPhone = sourceElement.child(1).text().trim();
                            index = tempPhone.indexOf('：');

                            if (index >= 0) {
                                tempPhone = tempPhone.substring(index + 1).trim();
                            } else {
                                tempPhone = "无";
                            }

                            tempPhone = tempPhone.replaceAll("-", "");

                        } else {
                            Element lianXiElement = lianXiInfoElements.get(0);
                            tempContactName = lianXiElement.attr("data-contact").trim();
                            if (tempContactName.length() < 1) tempContactName = "无";
                            tempPhone = lianXiElement.attr("data-tel").trim();
                            if (tempPhone.length() < 1) tempPhone = "无";
                        }


                        String tempQq = "无";

                        if (qqElements.size() > 0) {
                            String qqHref = qqElements.get(0).attr("href");

                            // 匹配顶级分类级别1的ID
                            try {
                                String regex = "uin=(\\d+)&";

                                Pattern pattern = Pattern.compile(regex);

                                Matcher matcher = pattern.matcher(qqHref);

                                if (matcher.find()) {
                                    tempQq = matcher.group(1);
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }

                        cContactName += tempContactName;
                        cPhone += tempPhone;
                        qq += tempQq;

                        if (i < lianXiElements.size() - 1) {
                            cContactName += ",";
                            cPhone += ",";
                            qq += ",";
                        }

                    }

                    companyInfo = new CompanyInfo(IDUtils.genId(platform, _id), _id, cName, cContactName, mobile, cPhone, qq, cAddress);

                } catch (Exception e) {
                    log.error(e.getMessage() + " , url  =  " + url, e);
                    companyInfo = null;
                }
            } else {
                log.error("responseWrap not success , url  =  " + url);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return companyInfo;
    }

}
