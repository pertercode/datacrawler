package services;

import bean.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WuAGeService {
    private LogUtils log = new LogUtils(platform, WuAGeService.class);

    public static final String BASE_URL = "https://www.wuage.com/";

    public static final String platform = "wuage";

    private BaseDao baseDao = new BaseDao();


    /**
     * 查询1J分类
     *
     * @return
     */
    public List<Category> requestCateogry() {
        String url = "https://s.wuage.com/product/search";
        List<Category> categories = null;

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {

            try {
                Document doc = Jsoup.parse(responseWrap.body);
                Elements categoryElements = doc.select("[sn-type=\"cate\"]");

                if (categoryElements.size() > 0) {
                    categories = new ArrayList<Category>();
                    String categoryStr = categoryElements.get(0).attr("sn-tag").trim();
                    String arr[] = categoryStr.split("\\^_\\^");

                    for (String str : arr) {
                        String info[] = str.split("@@");

                        String cid = info[1];

                        String cName = info[0];

                        Category category = new Category(IDUtils.genId(platform, cid), platform, cid, cName, 0, 0, "0");
                        category.setC_url("https://s.wuage.com/product/search?postCategoryIds=" + cid);
                        log.i("1J 分类入库 , cid = " + category.get_id() + " , cName = " + category.getC_name());
                        getType(category);

                        baseDao.categoryReplace(category);

                        List<Category> categories2 = requestCateogry2(category);

                        if (categories2 != null) {
                            categories.addAll(categories2);
                        }
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


    // 查询二级分类
    private List<Category> requestCateogry2(Category category) {
        String url = category.getC_url();
        List<Category> categories = null;

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {

            try {
                Document doc = Jsoup.parse(responseWrap.body);
                Elements categoryElements = doc.select(".il.tag");

                if (categoryElements.size() > 0) {
                    categories = new ArrayList<Category>();
                    String categoryStr = categoryElements.get(0).attr("sn-tag").trim();
                    String arr[] = categoryStr.split("\\^_\\^");

                    for (String str : arr) {
                        String info[] = str.split("@@");

                        String cid = info[1];

                        cid = category.getC_id() + "_" + cid;

                        String cName = info[0];

                        Category category2 = new Category(IDUtils.genId(platform, cid), platform, cid, cName, 1, 1, category.get_id());
                        category2.setC_url("https://s.wuage.com/product/search?postCategoryIds=" + category.getC_id() + "&propertyString=" + info[1]);

                        log.i("2J 分类入库 , cid = " + category2.get_id() + " , cName = " + category2.getC_name() + " , parentName = " + category.getC_name());

                        getType(category2);

                        baseDao.categoryReplace(category2);
                        categories.add(category2);
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


    // 查询规格型号
    public void getType(Category category) {
        String url = category.getC_url();

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {

            try {
                Document doc = Jsoup.parse(responseWrap.body);
                Elements elements = doc.select(".mod-sn-list li");

                if (elements.size() > 0) {
                    // 不是最底层则有一个品名的问题，去掉这个问题
                    if (category.getC_islow() < 1) {
                        elements.remove(0);
                    }

                    // 删除已经存在得规格型号
                    baseDao.typeNameDelete(category.get_id());
                    baseDao.typeValueDelete(category.get_id());

                    for (int i = 0; i < elements.size(); i++) {
                        Element element = elements.get(i);

                        String tName = element.child(0).text().trim();
                        TypeName typeName = new TypeName(IDUtils.uuid(), tName, category.get_id());

                        log.i("规格型号名称 : " + typeName.gettName());

                        String valueStr = element.child(1).child(0).attr("sn-tag").trim();

                        String arr[] = valueStr.split("\\^_\\^");

                        for (String str : arr) {
                            String info[] = str.split("@@");

                            String vId = info[1];

                            vId = typeName.get_id() + "_" + vId;

                            String vName = info[0];

                            TypeValue typeValue = new TypeValue(vId, vName, category.get_id(), typeName.get_id());
                            log.i("值 : " + typeValue.gettValue());
                            baseDao.typeValueReplace(typeValue);
                        }

                        baseDao.typeNameReplace(typeName);
                    }

                }
            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(e.getMessage() + "\n" + str, e);
            }
        } else {
            String str = HttpUtils.errorStringNoBody(responseWrap);
            log.e(str, responseWrap.e);
        }

    }


    /**
     * 获取商品信息
     *
     * @return
     */
    public List<ProduceInfo> requestProduceInfo(Category category, Integer page) {

        List<ProduceInfo> produceInfos = null;

        String url = category.getC_url() + "&browseSchema=2&pageSize=60&page=" + page;

        //创建url
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        //发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        //如果请求成功
        if (responseWrap.isSuccess()) {
            try {
                produceInfos = new ArrayList<ProduceInfo>();
                Document doc = Jsoup.parse(responseWrap.body);
                Elements producesElements = doc.select(".mod-img .fe-col");

                //当前页数
                Element pageElements = doc.select("#page").get(0);

                //总页数
                int totalPage = Integer.parseInt(pageElements.attr("total-size").trim());

                if (producesElements.size() > 0) {

                    for (Element e : producesElements) {

                        String data_userid = e.attr("data-userid").trim();

                        Element produceA = e.select("a.img").first();

                        //商品url
                        String pUrl = produceA.attr("href").trim();

                        String pId = e.attr("data-offerid").trim();

                        Element imgElement = produceA.select("img").get(0);

                        //图片scr
                        String imgSrc = imgElement.attr("o-src").trim();

                        if (StringUtils.isEmpty(imgSrc)) {
                            imgSrc = imgElement.attr("src").trim();
                        }

                        if (!StringUtils.isEmpty(imgSrc)) {
                            imgSrc = "https:" + imgSrc;
                        }

                        //商品名
                        String pName = e.select("p a").text().trim();


                        //商品价格
                        String price = e.select(".price-box").text().trim().replace(",", "");

                        //企业名
                        String cName = e.select(".company .company-name").text().trim();

                        String cUrl = "";

                        String _id = IDUtils.genId(platform, pId);

                        url = "https://s.wuage.com/product/shop/url?memberId=" + data_userid;

                        request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
                        responseWrap = HttpUtils.retryHttpNoProxy(request);
                        //如果请求成功
                        if (responseWrap.isSuccess()) {
                            String body = responseWrap.body;

                            String regexp = "\\{\"url\":\"(.+)\"\\}";

                            Pattern pattern = Pattern.compile(regexp);
                            Matcher matcher = pattern.matcher(body);
                            if (matcher.find()) {
                                cUrl = matcher.group(1);
                                if (!StringUtils.isEmpty(cUrl)) {
                                    cUrl = "https://" + cUrl;
                                }
                            }

                        }

                        ProduceInfo produceInfo = new ProduceInfo(_id, page, totalPage, pId, pName, pUrl, price, imgSrc);
                        produceInfo.setCategory(category);
                        produceInfo.setcName(cName);
                        produceInfo.setpCUrl(cUrl);

                        produceInfos.add(produceInfo);
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


    /**
     * 获取企业信息
     *
     * @param url 企业url
     * @return
     */
    public CompanyInfo requestCompanyInfo(String url) {
        url = url + "/page/contactinfo.htm";

        CompanyInfo companyInfo = null;

        //创建url
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        //发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);
        //如果请求成功
        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body);
                Element element = doc.select(".contact-info").get(0);

                //企业手机号
                String tel = element.select(".mobile .mobile-phone.mobile-hide").text();

                String mpTel = doc.select("#contact-m-p").val().substring(2);
                tel = tel.replace("****", mpTel);

                //企业名
                String cName = element.select(".title").text().trim();
                //地址
                String cAddr = element.select(".address .content").text().trim();
                //联系人
                String concat = element.select(".contact span").get(1).text().trim();
                //ID
                String cId = url.split("/")[3];
                //_id
                String c_id = IDUtils.genId(platform, cId);

                companyInfo = new CompanyInfo(c_id, cId, cName, concat, tel, tel, null, cAddr);

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


}