package services;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import com.google.gson.Gson;
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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 筑牛网抓取服务
 */
public class ZNBaseService {

    private static final Logger log = Logger.getLogger(ZNBaseService.class);

    private static final String BASE_URL = "http://www.zhuniu.com/";

    private static final String platform = "zhuniu";

    public static String phpSession = "";

//    /**
//     * 保存Cookie
//     *
//     * @return
//     */
//    public Header getCookie() {
//        Header header = null;
//        String val = (System.currentTimeMillis() / 1000) + "";
//
//        if (phpSession.trim().length() > 0) {
//            header = new Header("Cookie", "PHPSESSID=" + phpSession + "; Hm_lvt_28f83144ccb9b9784df395859259ef55=" + val + "; Hm_lpvt_28f83144ccb9b9784df395859259ef55=" + val);
//            return header;
//        }
//
//        String url = BASE_URL + "api/ajax_check_login_2016_type_2?random=" + System.currentTimeMillis();
//
//        try {
//            final Request request = new Request.Builder()
//                    .headers(HttpUtils.getCommonHeaders())
//                    .header("Referer", BASE_URL)
//                    .header("X-Requested-With", "XMLHttpRequest")
//                    .url(url)
//                    .build();
//
//            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);
//
//            if (responseWrap.isSuccess()) {
//                String setCookie = responseWrap.response.header("Set-Cookie");
//                int index = setCookie.indexOf('=') + 1;
//                int endIndex = setCookie.indexOf(';');
//                phpSession = setCookie.substring(index, endIndex);
//
//                header = new Header("Cookie", "PHPSESSID=" + phpSession + "; Hm_lvt_28f83144ccb9b9784df395859259ef55=" + val + "; Hm_lpvt_28f83144ccb9b9784df395859259ef55=" + val);
//
//                System.out.println("Cookie 已经重置 , Cookie = " + header.value.string(Charset.forName("UTF-8")));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return header;
//    }

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

//        Header cookie = getCookie();
//        final Request request = new Request.Builder()
//                .headers(HttpUtils.getCommonHeaders())
//                .header("Referer", "market/product_lists/category/" + page)
//                .header("Cookie", (cookie != null ? cookie.value.string(Charset.forName("UTF-8")) : ""))
//                .url(url)
//                .build();

        try {
            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", "market/product_lists/category/" + page)
                    .url(url)
                    .build();


            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
                produceInfos = new ArrayList<ProduceInfo>();
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements producesHtml = doc.select(".info-box");
                int dataSize = producesHtml.size();

                Elements pageSizeElements = doc.select(".end");

                // 获取总页数
                Integer totalPage = null;

                if (pageSizeElements.size() > 0) {
                    String totalPageStr = pageSizeElements.get(0).text().trim();
                    totalPage = Integer.parseInt(totalPageStr);
                }

                if (totalPage == null) {
                    throw new RuntimeException("totalPage is Null !");
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

                    ProduceInfo produceInfo = new ProduceInfo(_id, page, totalPage, pid, pName, contentUrl, pPrice, pSrc);
                    produceInfo.setcName(cName);
                    produceInfos.add(produceInfo);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return produceInfos;
    }


    /**
     * 查询企业信息
     */
    public CompanyInfo requestCompanyInfo(String url) {
        CompanyInfo info = null;

//        Header cookie = getCookie();
//
//        final Request request = new Request.Builder()
//                .headers(HttpUtils.getCommonHeaders())
//                .header("Referer", BASE_URL)
//                .header("Cookie", (cookie != null ? cookie.value.string(Charset.forName("UTF-8")) : ""))
//                .url(url)
//                .build();

        try {

            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", BASE_URL)
                    .url(url)
                    .build();

            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

            if (responseWrap.isSuccess()) {
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
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return info;

    }


    /**
     * 分类入库，并且返回商品的所属分类
     *
     * @param categoryStr
     * @return
     */
    public Category requestCategory(String categoryStr) {
        Category result = null;

        try {
            if (categoryStr.startsWith(">"))
                categoryStr = categoryStr.substring(1);

            String[] arr = categoryStr.split(">");

            int sourceLength = arr.length;

            if (sourceLength < 1)
                throw new RuntimeException("category size < 0 ! category = " + categoryStr);

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
                log.info("Category 重复,但已经去重, 原来类型： " + categoryStr + " , 去重复后： " + new Gson().toJson(categoryList));
            }

            // 判断类别是否存在于数据库
            boolean[] fromDb = new boolean[arr.length];

            // 通过分类名，查找数据库数据或新建分类数据
            Category[] categories = new Category[arr.length];

            for (int i = 0; i < arr.length; i++) {
                String cname = arr[i].trim();
                boolean exists = categoryExists(cname);
                fromDb[i] = exists;

                Category category = null;

                if (exists) {
                    // 存在
                    category = categorySelect(cname);
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
                categoryInsert(category);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        return result;
    }

    ////////////////  下面开始是 DAO  ///////////////////////////

    //    ///// 分类

    /**
     * 根据类型名称检测类型是否存在
     *
     * @param cname
     * @return
     */
    public boolean categoryExists(String cname) {
        boolean exists = false;
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            Integer count = mapper.categoryCount(cname);
            exists = count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return exists;
    }

    /**
     * 插入类型
     */
    public void categoryInsert(Category category) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.categoryInsert(category);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    /**
     * 根据名称查询类型
     *
     * @param cname
     * @return
     */
    public Category categorySelect(String cname) {
        SqlSession sqlSession = null;
        Category category = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            category = mapper.categorySelect(cname);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return category;
    }


    //
//
//    /// 产品
    public void produceReplace(ProduceInfo produceInfo) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.produceReplace(produceInfo);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    //// 企业
    public void companyExists() {
    }


    public void companyReplace(CompanyInfo companyInfo) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.companyReplace(companyInfo);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    // 查询最大页数 ( 根据平台 )
    public int produceMaxPage() {
        int result = 1;
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession();
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            result = mapper.produceMaxPage(platform);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return result;
    }


}
