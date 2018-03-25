package services;

import bean.*;
import crawler.EJianLianThread;
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
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018-03-13.
 *
 * @author chenyong
 */
public class HuaMuService {

    private LogUtils log = new LogUtils(HuaMuService.platform, HuaMuService.class);

    public static final String BASE_URL = "http://www.huamu.com/";

    public static final String platform = "huamu";

    private BaseDao baseDao = new BaseDao();

    /**
     * 抓取分类，返回最底层分类
     *
     * @return
     */
    public List<Category> requestCategory() {
        String url = "http://www.huamu.com/fenlei/";
        List<Category> categories = null;

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            try {
                categories = new ArrayList<Category>();
                Document doc = Jsoup.parse(responseWrap.body);

                Elements level1Elements = doc.select("dl[class=\"shop_assort\"] dt[class=\"bg_color1\"]");
                for (Element l1Element : level1Elements) {

                    //一级菜单名
                    String l1Name = l1Element.select("a").text().trim();
                    //一级Id
                    String l1Url = l1Element.select("a").attr("href").trim();
                    int startIndex = l1Url.lastIndexOf("/");
                    int endIndex = l1Url.lastIndexOf(".");
                    //Id
                    String l1Id = l1Url.substring(startIndex + 1, endIndex);
                    //_id
                    String l1_id = IDUtils.genId(platform, l1Id);

                    Category categoryLevel1 = new Category(l1_id, platform, l1Id, l1Name, 0, 0, "0");
                    categoryLevel1.setC_url(l1Url);
                    baseDao.categoryReplace(categoryLevel1);

                    log.i("1分类入库了 , 名称 =  " + categoryLevel1.getC_name());

                    //二级菜单
                    Elements level2Elements = l1Element.nextElementSibling().select("ul");
                    for (Element l2Element : level2Elements) {
                        //二级名
                        String l2Name = l2Element.select(".levltwo span a").text().trim();
                        //二级Id
                        String l2Url = l2Element.select(".levltwo span a").attr("href").trim();

                        String[] strs = l2Url.split("/");
                        String l2Id = strs[4].split("\\.")[0];

                        //_id
                        String l2_id = IDUtils.genId(platform, l2Id);
                        Category categoryLevel2 = new Category(l2_id, platform, l2Id, l2Name, 1, 0, l1_id);
                        categoryLevel2.setC_url(l2Url);

                        //三级分类
                        Elements level3Elements = l2Element.select(".levlthree a");
                        if (level3Elements.size() < 1) {
                            categoryLevel2.setC_islow(1);
                        }
                        baseDao.categoryReplace(categoryLevel2);

                        log.i("2分类入库了 , 名称 =  " + categoryLevel2.getC_name() + " ,  parent = " + categoryLevel1.getC_name());

                        if (categoryLevel2.getC_islow() == 1) {
                            categories.add(categoryLevel2);
                            continue;
                        }


                        for (Element l3Element : level3Elements) {
                            //分类名
                            String l3Name = l3Element.text().trim();
                            //Id
                            String l3Url = l3Element.attr("href").trim();
                            String[] l3Strs = l3Url.split("/");
                            String l3Id = l3Strs[4].split("\\.")[0];

                            //_id
                            String l3_id = IDUtils.genId(platform, l3Id);

                            Category categoryLevel3 = new Category(l3_id, platform, l3Id, l3Name, 2, 1, l2_id);
                            categoryLevel3.setC_url(l3Url);

                            baseDao.categoryReplace(categoryLevel3);
                            categories.add(categoryLevel3);
                            log.i("3分类入库了 , 名称 =  " + categoryLevel3.getC_name() + " ,  parent = " + categoryLevel2.getC_name());

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


    public void getType(Category category) {
        String url = "http://www.huamu.com/fenlei/" + category.getC_id() + ".html";
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body);

                // plants_age
                Elements typeElements = doc.select(".plants_age");

                if (typeElements.size() < 1) {
                    log.i("该分类没有规格型号... url = " + url);
                    return;
                }

                // 删除规格型号
                baseDao.typeNameDelete(category.get_id());
                baseDao.typeValueDelete(category.get_id());


                for (Element typeElement : typeElements) {

                    String typeId = IDUtils.uuid();
                    String typeName = typeElement.child(0).text().trim();
                    TypeName typeNameObj = new TypeName(IDUtils.genId(platform, typeId), typeName, category.get_id());
                    baseDao.typeNameReplace(typeNameObj);
                    log.i("类型 : " + typeName);


                    Elements typeValueElements = typeElement.select("ul li a");

                    for (int i = 0; i < typeValueElements.size(); i++) {
                        Element typeValueElement = typeValueElements.get(i);
                        String vValue = typeValueElement.text().trim();
                        String vId = IDUtils.uuid();
                        TypeValue typeValue = new TypeValue(IDUtils.genId(platform, vId), vValue, category.get_id(), typeNameObj.get_id());
                        baseDao.typeValueReplace(typeValue);
                        log.i("值 : " + vValue);
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
     * 抓取商品信息
     *
     * @return Category category,Integer page
     */
    public List<ProduceInfo> requestProduceInfo(Category category, int page) {
        List<ProduceInfo> produceInfos = null;

        String oldCId = category.getC_id();

        String[] ids = oldCId.split("_");

        //分页后的id
        String newCid = ids[0] + "_" + page + "_" + ids[1];

        String url = "http://www.huamu.com/fenlei/" + newCid + ".html";

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            try {
                produceInfos = new ArrayList<ProduceInfo>();
                Document doc = Jsoup.parse(responseWrap.body);

                Elements pElements = doc.select("ul[class=\"list_pic\"] li");

                String cAndT = doc.select(".page .nonce").text().trim();

                //总页数
                String[] strsSplit = cAndT.replace(" ", "").split("/");

                //总页数
                int totalPage = 0;

                if (strsSplit.length > 0 && cAndT.length() > 0) {
                    totalPage = Integer.parseInt(strsSplit[1]);
                }

                if (totalPage < 1) {
                    log.e("total page is 0  , url = " + url, null);
                    return null;
                }

                if (pElements.size() > 0)
                    for (Element e : pElements) {
                        //商品图片路径
                        String imgSrc = e.select("p a img").attr("src").trim();

                        //商品详情链接
                        String pUrl = e.select("p a").attr("href").trim();

                        //商品Id
                        String[] strs = pUrl.split("/");
                        String pId = strs[4].split("\\.")[0];

                        String pName = e.select("h3 .depict").text().trim();

                        //_id
                        String p_id = IDUtils.genId(platform, pId);

                        //价格
                        String price = e.select("h3 .price").text().replace("¥", "");

                        //企业名
                        String cName = e.select("h3 .info a").text().trim();

                        //企业Url
                        String pCurl = e.select("h3 .info a").attr("href").trim();

                        ProduceInfo produceInfo = new ProduceInfo(p_id, page, totalPage, pId, pName, pUrl, price, imgSrc);
                        produceInfo.setpCUrl(pCurl);
                        produceInfo.setcName(cName);
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
     * 抓取企业信息
     *
     * @return
     */
    public CompanyInfo requestCompanyInfo(String url) {
        CompanyInfo companyInfo = null;
        url += "special/contact.html";

        String cid = "";

        int cidIndex = url.indexOf('.');

        cid = url.substring(7, cidIndex);


        final Request request = new Request.Builder()
                //.headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.119 Mobile Safari/537.36")
                .url(url)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body);

                companyInfo = findFooter(doc);

                companyInfo.setcId(cid);
                companyInfo.set_id(IDUtils.genId(platform, cid));

                Elements elements = doc.select("ul li label");

                if (elements.size() > 0) {

                    Element ul = findParentUl(elements.get(0));

                    if (ul != null) {
                        elements = ul.select("li");
                        for (Element e : elements) {
                            String text = e.text().trim();
                            if (text.startsWith("地址")) {
                                int index = text.indexOf('：');
                                String address = text.substring(index + 1);
                                String floorAddress = companyInfo.getcAddress();
                                if (!StringUtils.isEmpty(floorAddress)) address = floorAddress + address;
                                companyInfo.setcAddress(address);
                            }

                            if (text.startsWith("电话")) {
                                int index = text.indexOf('：');
                                String mobile = text.substring(index + 1);
                                companyInfo.setcMobile(mobile);
                            }
                        }


                    }

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

    public Element findParentUl(Element element) {
        Element result = null;

        int maxSearchSize = 6;

        for (int i = 0; i < maxSearchSize; i++) {
            result = element.parent();
            if (result.is("ul"))
                break;
            element = result;
        }

        return result;
    }


    public CompanyInfo findFooter(Element doc) {

        Elements tds = doc.select("div.pp-footer tr td");

        Element td = null;

        if (tds.size() > 0) {
            td = tds.get(0);
        } else {
            Elements elements = doc.select(".footer_cnt p");
            if (elements.size() > 0) {
                td = elements.get(0);
            }
        }
        Element a = td.child(0);

        String cName = a.ownText().trim();

        String href = td.child(1).attr("href").trim();


        String cid = IDUtils.uuid();

        String regex = "store_(\\d+)";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(href);

        if (matcher.find()) {
            cid = matcher.group(1);
        }

//        System.out.println("cid : " + cid + " , " + cName);

        String text = td.ownText();

        text = text.replaceAll(Jsoup.parse("&nbsp;").text(), "");

        regex = "所在地区：?(.*)联系人";

        pattern = Pattern.compile(regex);

        matcher = pattern.matcher(text);


        String address = "";

        if (matcher.find()) {
            address = matcher.group(1);
            address = address.trim();
        }

        regex = "联系人：?(.*)电话";

        pattern = Pattern.compile(regex);

        matcher = pattern.matcher(text);

        String contactName = "";

        String contactPhone = "";

        if (matcher.find()) {
            contactName = matcher.group(1);
            contactName = contactName.trim();
        }

        Elements contactPhones = td.select(".store_mobile");

        if (contactPhones.size() > 0) {
            contactPhone = contactPhones.get(0).text().trim();
            if (contactPhone.contains("*")) {
                contactPhone = getPhone(cid);
            }

        }
        String c_id = IDUtils.genId(platform, cid);

        CompanyInfo companyInfo = new CompanyInfo(c_id, cid, cName, contactName, contactPhone, contactPhone, null, address);
        return companyInfo;
    }


    public String getPhone(String id) {
        String phone = "";

        String url = "http://www.huamu.com/index.php?app=stats&act=click_log&store_id=" + id + "&callback=receive&sn=&_=" + System.currentTimeMillis();
        Request request = new Request.Builder().headers(HttpUtils.getCommonHeaders())
                .header("Referer", "http://www.huamu.com/")
                .url(url).build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            String body = responseWrap.body;

            String regex = "store_mobile\":\"(\\d+)\",";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(body);


            if (matcher.find()) {
                phone = matcher.group(1);
                phone = phone.trim();
            }

        }

        return phone;
    }

}
