package services;

import bean.Category;
import bean.CompanyInfo;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018-03-09.
 *
 * @author chenyong
 */
public class HuiCongService {
    private LogUtils log = new LogUtils(platform, HuiCongService.class);

    public static final String BASE_URL = "https://www.hc360.com/";

    public static final String platform = "huicongwang";

    private BaseDao baseDao = new BaseDao();

    /**
     * 抓取菜单
     *
     * @return 最底层菜单
     */
    public List<Category> requestCateogry() {
        String url = "https://www.hc360.com/";
        List<Category> categories = null;
        //创建url
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        //发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request, "gb2312");
        //如果发送成功
        if (responseWrap.isSuccess()) {
            try {
                categories = new ArrayList<Category>();
                Document doc = Jsoup.parse(responseWrap.body);
                Elements level1Elements = doc.select(".item_Vlist .H_sideBar_list .sideBarLeft ul li");
                int level1Size = level1Elements.size();
                for (int i = 0; i < level1Size; i++) {
                    Element level1Element = level1Elements.get(i);
                    //分类名
                    String level1Name = level1Element.select(".sideBarLeftTit").html();
                    //Cid
                    String level1Cid = IDUtils.uuid();
                    //_id
                    String level1_id = IDUtils.genId(platform, level1Cid);

                    //一级分类入库
                    Category categoryLevel1 = new Category(level1_id, platform, level1Cid, level1Name, 0, 0, "0");
                    baseDao.categoryReplace(categoryLevel1);

                    log.i("1分类入库了 , 名称 =  " + categoryLevel1.getC_name());

                    //二级分类
                    Elements level2Elements = level1Element.select(".sideBarLinkBox a");
                    int level2Size = level2Elements.size();
                    for (int j = 0; j < level2Size; j++) {
                        Element level2Element = level2Elements.get(j);
                        //分类名
                        String level2Name = level2Element.text().trim();
                        String level2Href = level2Element.attr("href");
                        int leftIndex = level2Href.lastIndexOf("/");
                        int rigthIndex = level2Href.lastIndexOf(".");

                        String level2Cid = level2Href.substring(leftIndex + 1, rigthIndex);

                        String level2_id = IDUtils.genId(platform, level2Cid);

                        //二级分类入库
                        Category categoryLevel2 = new Category(level2_id, platform, level2Cid, level2Name, 1, 1, level1_id);
                        categoryLevel2.setC_url(level2Href);
                        //将最底层分类加入集合
                        categories.add(categoryLevel2);
                        baseDao.categoryReplace(categoryLevel2);
                        log.i("2分类入库了 , 名称 =  " + categoryLevel2.getC_name() + " ,  parent = " + categoryLevel1.getC_name());
                    }
                }
            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(str, e);
                categories = null;
            }
        } else {
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
            categories = null;
        }

        return categories;
    }


    /**
     * 抓取单页商品信息
     *
     * @return
     */
    public List<ProduceInfo> requestProduce(Category category, Integer page) {
        List<ProduceInfo> produceInfos = null;

        String url = "https://s.hc360.com/?w=" + category.getC_name() + "&mc=seller&ee=" + page + "&ap=B&pab=B&t=1";

        //创建url
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        //发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request, "gb2312");
        if (responseWrap.isSuccess()) {
            try {
                produceInfos = new ArrayList<ProduceInfo>();
                Document doc = Jsoup.parse(responseWrap.body);
                Elements producesElements = doc.select(".wrap-grid ul").get(0).select("li");
                //如果没有数据
                if (producesElements.size() < 1) {
//                    log.e("页面数据为 0 ,  url = " + url, null);
                    return produceInfos;
                }

                //总页数
                String totalPageStr = doc.select(".s-mod-page .total").text().trim();
                int left = totalPageStr.indexOf("共");
                int right = totalPageStr.indexOf("页");
                int totalPage = Integer.parseInt(totalPageStr.substring(left + 1, right));

                //当前页
                int currPage = Integer.parseInt(doc.select(".s-mod-page .page-cur").text().trim());

                for (int i = 0; i < producesElements.size() - 1; i++) {
                    Element pElement = producesElements.get(i);
                    //商品名称
                    String pName = pElement.select(".NewItem .pRel a").attr("title").trim();
                    //商品详情链接
                    String pUrl = pElement.select(".NewItem .pRel a").attr("href").trim();
                    if (!pUrl.startsWith("https:")) {
                        pUrl = "https:" + pUrl;
                    }
                    //商品图片
                    String imgSrc = pElement.select(".NewItem .pRel a img").attr("src").trim();

                    if (!imgSrc.startsWith("http:")) {
                        imgSrc = "http:" + imgSrc;
                    }

                    String pPriceAll = pElement.select(".NewItem .seaNewList .seaNewPrice ").text().trim();
                    //商品价格
                    String pPrice = pPriceAll.substring(pPriceAll.indexOf("¥") + 1);

                    //CID
                    int index = pUrl.lastIndexOf("/");
                    String pCid = pUrl.substring(index + 1).substring(0, pUrl.substring(index + 1).indexOf("."));

                    //_id
                    String p_id = IDUtils.genId(platform, pCid);

                    //企业名
                    String cName = pElement.select(".NewItem .newCname p a").attr("title").trim();
                    //企业链接
                    String cUrl = pElement.select(".NewItem .newCname p a").attr("href").trim();
                    if (!cUrl.contains("https:")) {
                        cUrl = "https:" + cUrl;
                    }
                    ProduceInfo produceInfo = new ProduceInfo(p_id, currPage, totalPage, pCid, pName, pUrl, pPrice, imgSrc);

                    produceInfo.setcName(cName);
                    produceInfo.setpCUrl(cUrl);

                    //加入集合
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
     * @param
     * @return
     */
    public CompanyInfo requestCompanyInfo(String url) {
        CompanyInfo companyInfo = null;

        String suffix = "/shop/company.html";
        String allUrl = url + suffix;
        if (!allUrl.contains("https:")) {
            allUrl = "https:" + allUrl;
        }
        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(allUrl)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request, "gb2312");

        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements companyElements = doc.select(".qyArchives");
                Element companyElement = null;
                //企业名
                String cName = "";
                if (companyElements.size() > 0) {
                    companyElement = companyElements.get(0);
                    //企业名
                    cName = companyElement.select("h2 a").attr("title").trim();
                    //cCid
                    int leftIndex = url.indexOf("//");
                    int rightIndex = url.indexOf(".");
                    String cCid = url.substring(leftIndex + 2, rightIndex);

                    //c_id
                    String c_id = IDUtils.genId(platform, cCid);

                    //联系人
                    Element rigElement = doc.select(".ContacCon3").get(0);
                    Elements liElements = doc.select(".ContacCon3 ul li");
                    int licount = liElements.size();

                    String concat = rigElement.select(".con3Rig span a").get(0).text().trim();

                    //qq
                    String qqUrl = rigElement.select(".con3Rig .aQQIco").attr("href").trim();
                    String[] strs = qqUrl.split("&");
                    String param = "";
                    String qq = "";
                    if (strs.length > 2) {
                        param = strs[1];
                        qq = param.substring(param.indexOf("=") + 1);
                    }
                    //电话
                    String tel = rigElement.select(".con3Rig[node-name=telephone]").text().trim();
                    if (tel.contains("*")) {
                        tel = rigElement.select("#telephone_id").val();
                    }
                    //手机号
                    String phone = rigElement.select(".con3Rig[node-name=mp]").text().trim();
                    if (phone.contains("*")) {
                        phone = rigElement.select("#mp_id").val();
                    }

                    //地址
                    String cAddr = liElements.select(":eq(" + (licount - 2) + ") .con3Rig").text().trim();

                    //判断地址是否是数字开头
                    Pattern pattern = Pattern.compile("^(\\d+)(.*)");
                    Matcher matcher = pattern.matcher(cAddr);
                    if (matcher.matches()) {//数字开头
                        cAddr = companyElement.select(".ArchivesList ul li").get(2).select(".w105").text().trim();
                    }

                    companyInfo = new CompanyInfo(c_id, cCid, cName, concat, tel, phone, qq, cAddr);
                } else {
                    //另一个网站模板
                    companyInfo = requestSecondCompany(allUrl);
                }

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


    public CompanyInfo requestCompanyInfo2() {
        CompanyInfo companyInfo = null;
        String url = "https://bjyanfu55555.b2b.hc360.com/";
        String allUrl = url + "/shop/company.html";
        if (!allUrl.contains("https:")) {
            allUrl = "https:" + allUrl;
        }
        try {
            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", BASE_URL)
                    .url(allUrl)
                    .build();

            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
                try {
                    Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                    Element companyElement = doc.select(".qyArchives").get(0);
                    //企业名
                    String cName = companyElement.select("h2 a").text().trim();

                    //cCid
                    int leftIndex = url.indexOf("//");
                    int rightIndex = url.indexOf(".");
                    String cCid = url.substring(leftIndex + 2, rightIndex);

                    //c_id
                    String c_id = IDUtils.genId(platform, cCid);

                    //联系人
                    Element rigElement = doc.select(".ContacCon3").get(0);
                    Elements liElements = doc.select(".ContacCon3 ul li");
                    int licount = liElements.size();

                    String concat = rigElement.select(".con3Rig span a").get(0).text().trim();

                    //qq
                    String qqUrl = rigElement.select(".con3Rig .aQQIco").attr("href").trim();
                    String[] strs = qqUrl.split("&");
                    String param = "";
                    String qq = "";
                    if (strs.length > 2) {
                        param = strs[1];
                        qq = param.substring(param.indexOf("=") + 1);
                    }
                    //电话
                    String tel = rigElement.select(".con3Rig[node-name=telephone]").text().trim();
                    if (tel.contains("*")) {
                        tel = rigElement.select("#telephone_id").val();
                    }
                    //手机号
                    String phone = rigElement.select(".con3Rig[node-name=mp]").text().trim();
                    if (phone.contains("*")) {
                        phone = rigElement.select("#mp_id").val();
                    }
                    //地址
                    String cAddr = liElements.select(":eq(" + (licount - 2) + ") .con3Rig").text().trim();

                    //判断地址是否是数字开头
                    Pattern pattern = Pattern.compile("^(\\d+)(.*)");
                    Matcher matcher = pattern.matcher(cAddr);
                    if (matcher.matches()) {//数字开头
                        cAddr = companyElement.select(".ArchivesList ul li").get(2).select(".w105").text().trim();
                    }

                    companyInfo = new CompanyInfo(c_id, cCid, cName, concat, tel, phone, qq, cAddr);

                } catch (Exception e) {
                    String str = HttpUtils.errorStringNoBody(responseWrap);
                    log.e(str, e);
                    companyInfo = null;
                }
            } else {
                String str = HttpUtils.errorString(responseWrap);
                log.e(str, responseWrap.e);
            }
        } catch (Exception e) {
            log.e(e.getMessage(), e);
        }
        return companyInfo;
    }

    /**
     * 公司黄页模板
     *
     * @return
     */
    public CompanyInfo requestSecondCompany(String url) {
        CompanyInfo companyInfo = null;
        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        //发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);
        if (responseWrap.isSuccess()) {
            Document doc = Jsoup.parse(responseWrap.body);
            Elements elements = doc.select("div.leftBox ul li");
            if (elements.size() > 0) {
                String cName = "";
                String contact = "";
                String cAddr = "";
                String tel = "";
                String phone = "";
                for (Element e : elements) {
                    if (e.text().trim().startsWith("公司")) {
                        cName = e.text().trim().split("：")[1].trim();
                    }
                    if (e.text().trim().startsWith("联系")) {
                        contact = e.text().trim().split("：")[1].trim();
                    }
                    if (e.text().trim().startsWith("地址")) {
                        cAddr = e.text().trim().split("：")[1].trim();
                    }
                    if (e.text().trim().startsWith("电话")) {
                        tel = e.text().trim().split("：")[1].trim();
                    }
                }
                //cCid
                int leftIndex = url.indexOf("//");
                int rightIndex = url.indexOf(".");
                String cCid = url.substring(leftIndex + 2, rightIndex);

                //c_id
                String c_id = IDUtils.genId(platform, cCid);

                if (cName.equals("") || cName.length() < 1) {
                    cName = doc.select("div.sub-info h1").text().trim();
                }

                Elements detailElements = doc.select("div.detailsinfo ul li");
                if (detailElements.size() > 0) {
                    for (Element de : detailElements) {
                        //如果联系人为空
                        if (contact.equals("") || contact.length() < 1) {
                            if (de.text().trim().startsWith("联系")) {
                                contact = de.text().trim().split("：")[1];
                            }
                        }
                        //如果手机为空
                        if (phone.equals("") || phone.length() < 1) {
                            if (de.text().trim().startsWith("手机")) {
                                phone = de.text().trim().split("：")[1];
                            }
                        }
                        //如果地址为空
                        if (cAddr.equals("") || cAddr.length() < 1) {
                            if (de.text().trim().startsWith("地址")) {
                                cAddr = de.text().trim().split("：")[1];
                            }
                        }
                    }
                } else {

                    String str = HttpUtils.errorStringNoBody(responseWrap);
                    log.e(str, null);
                }
                companyInfo = new CompanyInfo(c_id, cCid, cName, contact, phone, tel, null, cAddr);
            }
        } else {

            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }
        return companyInfo;
    }
}
