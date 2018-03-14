package services;

import bean.*;
import com.google.gson.Gson;
import dao.BaseDao;
import dao.BaseMapper;
import dao.MyBatisUtils;
import http.HttpUtils;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.internal.http2.Header;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.IDUtils;
import utils.LogUtils;
import utils.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 筑牛网抓取服务
 */
public class ZhuNiuBaseService {

    private static final String BASE_URL = "http://www.zhuniu.com/";
    public static final String platform = "zhuniu";

    private LogUtils log = new LogUtils(platform, ZhuNiuBaseService.class);

    private BaseDao baseDao = new BaseDao();


    /**
     * @param isTax ： 用于筛选一般纳税人，值为1时只搜索一般纳税人. 值为2时搜索所有
     */
    public void requestCategory(String isTax) {
        if (StringUtils.isEmpty(isTax))
            isTax = "2";

        String url = "http://www.zhuniu.com/market/product_lists?region=&isTax=" + isTax + "&keywords=&orderPrice=&category=243&fieldValue=";

        final Request request = new Request.Builder()
                .url(url)
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            String body = responseWrap.body;
            Document doc = Jsoup.parse(body);

            // 获得所有的分类
            Elements tdElements = doc.select(".p-list.clearfix table tr td");

            if (tdElements.size() > 1) {
                Element td = tdElements.get(1);

                // 所有分类 a 标签
                Elements aElements = td.children();


                for (int i = 0; i < aElements.size(); i++) {
                    Element aElement = aElements.get(i);
                    // 分类ID
                    String cId = aElement.attr("data-info").trim();

                    // 分类名称
                    String cName = aElement.attr("title").trim();

                    String _id = IDUtils.genId(platform, cId);

                    Category category = new Category(_id, platform, cId, cName, 0, 0, "0");

                    log.i("cid = " + cId + " , cName = " + cName + " ,  parentTypeId = " + 0);

                    requestChildCategory(isTax, category);

                    baseDao.categoryReplace(category);
                }

            } else {
                // 记录错误日志
                String str = " css 选择 .p-list.clearfix table tr td , 找到的标签数量 < 2 , size = " + tdElements.size();
                log.e(str, responseWrap.e);
            }

        } else {
            // 记录失败日志
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }

    }

    private void requestChildCategory(String isTax, Category parent) {
        String url = "http://www.zhuniu.com/market/product_lists?region=&isTax=" + isTax + "&keywords=&orderPrice=&category=" + parent.getC_id() + "&fieldValue=";

        final Request request = new Request.Builder()
                .url(url)
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            String body = responseWrap.body;
            Document doc = Jsoup.parse(body);


            // 获得所有的分类
            Elements tableElements = doc.select(".p-list.clearfix table");

            if (tableElements.size() > 0) {

                // 获得所有的分类
                Elements tdElements = tableElements.select("tr td");

                if (tdElements.size() < 1) {
                    // 说明这个分类已经到底了，属于最底层分类，并且没有规格型号
                    parent.setC_islow(1);
                    return;
                }


                if (tdElements.size() > 1) {
                    Element td = tdElements.get(1);
                    String css_class = td.attr("class").trim();
                    if ("type".equals(css_class)) {
                        // 说明这个分类已经到底了，属于最底层分类，具有规格型号
                        parent.setC_islow(1);
                        return;
                    }

                    // 所有分类 a 标签
                    Elements aElements = td.children();

                    for (int i = 0; i < aElements.size(); i++) {

                        Element aElement = aElements.get(i);
                        // 分类ID
                        String cId = aElement.attr("data-info").trim();

                        // 分类名称
                        String cName = aElement.attr("title").trim();

                        String _id = IDUtils.genId(platform, cId);

                        Category category = new Category(_id, platform, cId, cName, parent.getC_level() + 1, 0, parent.get_id());

                        log.i("cid = " + cId + " , cName = " + cName + " ,  parentTypeId = " + parent.get_id() + "  , parentTypeName = " + parent.getC_name());

                        requestChildCategory(isTax, category);

                        baseDao.categoryReplace(category);

                        if (category.getC_islow() == 1) {
                            // 最底层抓取规格型号
                            requestType(category);
                        }

                    }

                } else {
                    // 记录失败日志
                    String str = " css 选择 .p-list.clearfix table tr td, 找到的标签数量 < 2 , size = " + tdElements.size();
                    log.e(str, responseWrap.e);
                }

            } else {
                // 记录失败日志
                String str = " css 选择 .p-list.clearfix table, 找到的标签数量 < 1 ";
                log.e(str, responseWrap.e);
            }

        } else {
            // 记录失败日志
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }
    }


    /**
     * 根据类型查询规格型号
     *
     * @param category
     */
    public void requestType(Category category) {
        String url = "http://www.zhuniu.com/market/product_lists?region=&isTax=2&keywords=&orderPrice=&category=" + category.getC_id() + "&fieldValue=";

        final Request request = new Request.Builder()
                .url(url)
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            try {
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);

                Elements elements = doc.select(".p-list table").get(0).select("tr");

                if (elements.size() < 1) {
                    log.i("该分类无规格型号 , ID = " + category.get_id() + " , " + url);
                    return;
                }

                for (int i = 0; i < elements.size(); i++) {
                    Element type = elements.get(i).select("td").get(0).select("b").get(0);

                    String typeName = type.text().trim().split("：")[0];

                    String _id = IDUtils.genId(platform, typeName + "_" + category.getC_id());

                    TypeName typeNameObj = new TypeName(_id, typeName, category.get_id());

                    baseDao.typeNameReplace(typeNameObj);

                    log.i("规格 = " + typeName);

                    Elements value = elements.get(i).select("td").get(1).select("a");
                    for (int j = 0; j < value.size(); j++) {
                        Element a = value.get(j);
                        TypeValue typeValue = new TypeValue();
                        typeValue.set_id(IDUtils.genId(platform, a.attr("data-info").trim()));
                        typeValue.settValue(a.text().trim());
                        typeValue.setTypeNameId(_id);
                        baseDao.typeValueReplace(typeValue);
                        log.i("值 = " + typeValue.gettValue());
                    }
                }

            } catch (Exception e) {
                // 记录失败日志
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(str, e);
            }

        } else {
            // 记录失败日志
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }

    }


    /**
     * 查询所有商品
     *
     * @return
     */
    public List<ProduceInfo> requestAllProduce(Integer page) {
        List<ProduceInfo> produceInfos = null;
        if (page == null)
            page = 1;

        String url = BASE_URL + "market/product_lists/p/" + page;

        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL + "market/product_lists/category/" + page)
                .url(url)
                .build();


        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            produceInfos = new ArrayList<ProduceInfo>();
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements producesHtml = doc.select(".info-box");
                int dataSize = producesHtml.size();

                if (dataSize < 1) {
                    log.i("page = " + page + " , url = " + url + " ,  页面没有数据! ");
                    return produceInfos;
                }

                for (int i = 0; i < dataSize; i++) {
                    Element element = producesHtml.get(i);

                    Element firstChild = element.child(0);  // class="product-pic-container"

                    Element dataElement = firstChild.child(0); // <a>

                    // 获取商品更多内容得链接
                    String contentUrl = dataElement.attr("href");
                    String baseUrl = BASE_URL.substring(0, BASE_URL.length() - 1);
                    contentUrl = baseUrl + contentUrl;

                    // 商品ID
                    String pid = contentUrl.substring(contentUrl.lastIndexOf('/') + 1);

                    // 商品名称
                    String pName = dataElement.child(0).attr("alt").trim();

                    // 商品图片
                    String pSrc = dataElement.child(0).attr("data-original").trim();

                    // 价格
                    String pPrice = element.select(".one-price,.zhuniu-fl").get(0).text().trim();

                    // 企业名称
                    String cName = element.select(".c-name,.overflow").get(0).ownText().trim();

                    String _id = IDUtils.genId(platform, pid);

                    ProduceInfo produceInfo = new ProduceInfo(_id, page, 0, pid, pName, contentUrl, pPrice, pSrc);
                    produceInfo.setcName(cName);
                    produceInfos.add(produceInfo);
                }
            } catch (Exception e) {
                // 记录失败日志
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(str, e);
                produceInfos = null;
            }
        } else {
            // 记录失败日志
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
            produceInfos = null;
        }

        return produceInfos;
    }


    /**
     * 查询企业信息
     */
    public CompanyInfo requestCompanyInfo(String url) {
        CompanyInfo info = null;
        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(url)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);
        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements categoryHtml = doc.select("div .template-bread");

                // 分类
                String category = categoryHtml.get(0).text().trim();

                // 企业信息
                Element companyElement = doc.select(".template-store").get(0);

                // 企业名称
                String companyName = companyElement.child(0).text();

                companyElement = companyElement.select(".template-store-way").get(0);

                // 企业联系人
                String companyContact = companyElement.child(0).ownText().trim();

                // 固定电话
                String companyMobile = companyElement.child(1).ownText().trim();

                // 手机
                String companyPhone = companyElement.child(2).ownText().trim();

                // qq
                String companyQq = companyElement.child(3).ownText().trim();

                // 企业地址
                String companyAddress = doc.select(".adapt-item").get(1).text();


                String href = doc.select(".template-store-bottom").get(0).child(0).attr("href");

                // 企业ID
                String cId = href.substring(href.lastIndexOf('/') + 1);

                String _id = IDUtils.genId(platform, cId);

                info = new CompanyInfo(_id, cId, companyName, companyContact, companyMobile, companyPhone, companyQq, companyAddress);

                if (category == null || category.trim().length() < 1) {
                    category = ">其他未知";
                }
                info.category = category;

            } catch (Exception e) {
                // 记录失败日志
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(str, e);
            }
        } else {
            // 记录失败日志
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }
        return info;
    }

    /**
     * 补全分类
     *
     * @param categoryStr
     * @return
     */
    public Category compCategory(String categoryStr) {
        Category result = null;
        try {
            if (categoryStr.startsWith(">"))
                categoryStr = categoryStr.substring(1);

            String[] arr = categoryStr.split(">");

            int sourceLength = arr.length;

            // 去掉重复
            List<String> categoryList = new ArrayList<String>();
            for (int i = arr.length - 1; i >= 0; i--) {
                if (i - 1 >= 0) {
                    if (arr[i].equals(arr[i - 1])) {
                        continue;
                    }
                }
                categoryList.add(0, arr[i]);
            }

            arr = new String[categoryList.size()];
            for (int i = 0; i < categoryList.size(); i++) {
                arr[i] = categoryList.get(i);
            }

            int endLength = arr.length;

            if (sourceLength != endLength) {
                log.i("Category 重复,但已经去重, 原来类型： " + categoryStr + " , 去重复后： " + new Gson().toJson(categoryList));
            }

            // 判断类别是否存在于数据库
            boolean[] fromDb = new boolean[arr.length];

            // 通过分类名，查找数据库数据或新建分类数据
            Category[] categories = new Category[arr.length];

            for (int i = 0; i < arr.length; i++) {
                String cname = arr[i].trim();
                boolean exists = baseDao.categoryExists(cname);
                fromDb[i] = exists;

                Category category = null;

                if (exists) {
                    // 存在
                    category = baseDao.categorySelect(cname);
                } else {
                    // 创建新的分类，并且入库
                    int clevel = i;

                    int is_low = i == arr.length - 1 ? 1 : 0;

                    String cid = IDUtils.uuid();

                    String _id = IDUtils.genId(platform, cid);

                    String c_parent = clevel == 0 ? "0" : "";

                    // 不存在...
                    category = new Category(_id, platform, cid, arr[i], clevel, is_low, c_parent);
                }
                categories[i] = category;
            }

            // 从最低类型向最高类型查询，遇到父 类别存在于数据库则退出循环
            for (int i = categories.length - 1; i >= 0; i--) {
                Category category = categories[i];

                if (i == categories.length - 1) {
                    result = category;
                }

                if (i == 0) {
                    category.setC_parent("0");
                } else {
                    category.setC_parent(categories[i - 1].get_id());
                }

                if (fromDb[i]) {
                    break;
                }
                baseDao.categoryInsert(category);
            }
        } catch (Exception e) {
            log.e(e.getMessage(), e);
        }
        return result;
    }

}
