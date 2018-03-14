import bean.Category;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import services.ZhuNiuBaseService;
import utils.IDUtils;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ZhuNiuTester {

    private String baseUrl = "http://www.zhuniu.com/";

    private String platform = "zhuniu";

    private BaseDao baseDao = new BaseDao();


    @Test
    public void runTester() {
        ZhuNiuBaseService zhuNiuBaseService = new ZhuNiuBaseService() ;
//        Category category = new Category();
//        category.set_id("zhuniu_275");
//        category.setC_id("275");
        zhuNiuBaseService.requestAllProduce(1) ;
    }


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
                .header("Referer", baseUrl)
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
                    requestChildCategory(isTax, category);
                    baseDao.categoryReplace(category);
                }

            } else {
            }

        } else {
            // 记录失败日志

        }

    }

    private void requestChildCategory(String isTax, Category parent) {
        String url = "http://www.zhuniu.com/market/product_lists?region=&isTax=" + isTax + "&keywords=&orderPrice=&category=" + parent.getC_id() + "&fieldValue=";

        final Request request = new Request.Builder()
                .url(url)
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", baseUrl)
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

                        System.out.println("cid = " + cId + " , cName = " + cName + " ,  parentTypeId = " + parent.get_id() + "  , parentTypeName = " + parent.getC_name());

                        requestChildCategory(isTax, category);

                        baseDao.categoryReplace(category);

                    }

                } else {
                    // 记录失败日志
                }

            } else {
                // 记录失败日志
            }

        } else

        {
            // 记录失败日志

        }
    }

}
