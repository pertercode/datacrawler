package services;

import bean.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * yijianlian 抓取服务
 */
public class YJLService {

    private LogUtils log = new LogUtils(platform, YJLService.class);

    private static final String BASE_URL = "http://www.ejianlian.com/";

    public static final String platform = "ejianlian";

    private BaseDao baseDao = new BaseDao();

    // 最低级的分类
    public List<Category> categories = new ArrayList<Category>();

    /**
     * 查询所有1J分类
     *
     * @return
     */
    public void requestCategory() {
        // 清空最低级分类
        categories.clear();
        String url = "http://ejianlian.com/Home/Goods/productlist/";
        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(url)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                Elements categoryElements = doc.select(".tabbg-subject a");
                for (int i = 0; i < categoryElements.size(); i++) {
                    Element categoryElement = categoryElements.get(i);
                    // 连接
                    String href = BASE_URL.substring(0, BASE_URL.length() - 1) + categoryElement.attr("href").trim();
                    String cName = categoryElement.text().trim();
                    String cid = IDUtils.uuid();

                    int index = href.indexOf("cat_id");

                    cid = href.substring(index);

                    String c_url = "http://ejianlian.com/Home/Goods/productlist/" + cid;

                    Category category = new Category(IDUtils.genId(platform, cid), platform, cid, cName, 0, 0, "0");
                    category.setC_url(c_url);
                    requestChildCategory(category);
                    baseDao.categoryReplace(category);
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
     * 查询所有子分类
     *
     * @return
     */
    public void requestChildCategory(Category parent) {
        String url = parent.getC_url();
        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(url)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                int index = parent.getC_level();
                Elements elements = doc.select(".option-default.clearfix.breed_1");

                if (elements.size() > index) {
                    Element element = elements.get(index);

                    Elements liElements = element.select(".listitem ul li");

                    if (liElements.size() < 1) {
                        // 没有子分类了
                        parent.setC_islow(1);
                        categories.add(parent);
                        return;
                    }

                    if (index >= 3) {
                        String str = HttpUtils.errorStringNoBody(responseWrap);
                        log.e("发现4J分类 , 请处理 !! \n" + str, null);
                    }

                    for (int i = 0; i < liElements.size(); i++) {
                        Element li = liElements.get(i);

                        Element a = li.select("a").get(0);

                        String cName = a.ownText().trim();

                        String cid = IDUtils.uuid();

                        if (index == 0) {
                            String href = a.attr("href").trim();
                            int cat_idIndex = href.indexOf("cat_id");
                            cid = href.substring(cat_idIndex);
                        } else {
                            String onclick = a.attr("onclick").trim();

                            String regexp = "goto\\('(.+)',(\\d+)\\)";

                            Pattern pattern = Pattern.compile(regexp);

                            Matcher matcher = pattern.matcher(onclick);

                            if (matcher.find()) {
                                String tag = matcher.group(1);
                                String id = matcher.group(2);
                                cid = parent.getC_id() + tag + "/" + id + "/";
                            }
                        }

                        String cUrl = BASE_URL + "Home/Goods/productlist/" + cid;
                        Category category = new Category(IDUtils.genId(platform, cid), platform, cid, cName, parent.getC_level() + 1, 0, parent.get_id());
                        category.setC_url(cUrl);
                        getType(category);
                        requestChildCategory(category);
                        log.i("cid = " + cid + " , cName = " + cName + " ,  parentTypeId = " + parent.get_id() + "  , parentTypeName = " + parent.getC_name() + " , url = " + cUrl);
                        baseDao.categoryReplace(category);

                    }
                } else {
                    // 没有子分类了
                    parent.setC_islow(1);
                    categories.add(parent);
                    return;
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
     * 查询型号
     */
    public void getType(Category category) {
        // 把URL创建好了
        Request request = new Request.Builder().url(category.getC_url()).headers(HttpUtils.getCommonHeaders()).build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);
                Elements elements = doc.select("#material");
                if (elements.size() > 0) {

                    // 根据类型 ID删除规格型号名称和值
                    baseDao.typeNameDelete(category.get_id());
                    baseDao.typeValueDelete(category.get_id());

                    for (int i = 0; i < elements.size(); i++) {
                        Element type = elements.get(i).select("dt").get(0).select("span").get(0);

                        String _id = IDUtils.uuid();

                        String tName = type.text().trim().split("：")[0];

                        TypeName typeName = new TypeName(_id, tName, category.get_id());
                        baseDao.typeNameReplace(typeName);
                        log.i("规格 = " + typeName.gettName());

                        Elements value = elements.get(i).select("dd .listitem").get(0).select("li");
                        for (int j = 0; j < value.size(); j++) {
                            Element a = value.get(j).select("a").get(0);

                            String id = a.attr("onclick");
                            String vid = id.split(",")[1].split("'")[1];

                            TypeValue typeValue = new TypeValue(IDUtils.genId(platform, vid), a.text().trim(), category.get_id(), typeName.get_id());

                            if (typeValue.gettValue().length() < 200) {
                                baseDao.typeValueReplace(typeValue);
                                log.i("值 = " + typeValue.gettValue());
                            }
                        }
                    }
                } else {
                    log.i("该分类无规格型号 , ID = " + category.get_id() + " , " + category.getC_url());
                }
            } catch (Exception e) {
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

        String url = category.getC_url() + "/p/" + page + "/sort/2/";
        Integer totalPage = null;

        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(url)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                produceInfos = new ArrayList<ProduceInfo>();
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

                    // 企业URL
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
     * 根据企业ID查询企业信息
     *
     * @param cid
     * @return
     */
    public CompanyInfo requestCompanyWithCId(String cid) {
        // 先查询联系人信息
        CompanyInfo companyInfo = requestCompanyContact(cid);

        if (companyInfo != null) {
            companyInfo.setcId(cid);
            companyInfo.set_id(IDUtils.genId(platform, cid));

            String url = "http://shop" + cid + ".ejianlian.com/ajaxGetHeadData/sid/" + cid + "?_=" + System.currentTimeMillis();

            final Request request = new Request.Builder()
                    .headers(HttpUtils.getCommonHeaders())
                    .header("Referer", url)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .url(url)
                    .build();

            HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

            companyInfo.setcName("");
            companyInfo.setcMobile("");

            if (responseWrap.isSuccess()) {
                try {
                    String json = responseWrap.body.trim();

                    GsonBeanShopInfo shopInfo = new Gson().fromJson(json, GsonBeanShopInfo.class);

                    String cName = shopInfo.getMsg().getShop_info().getShop_name();

                    String cContact = shopInfo.getMsg().getShop_info().getManager();

                    String mobile = shopInfo.getMsg().getShop_info().getTel();

                    if (StringUtils.isEmpty(cContact)) {
                        cContact = cName;
                    }

                    // 设置企业名称
                    companyInfo.setcName(cName);
                    // 设置固定电话
                    companyInfo.setcMobile(mobile);

                    // 如果没有获得到联系信息
                    if (StringUtils.isEmpty(companyInfo.getcPhone()) || companyInfo.getcPhone().startsWith("null")) {
                        String phone = shopInfo.getMsg().getShop_info().getService_tel();
                        companyInfo.setcPhone(phone);
                        companyInfo.setcConcat(cContact);
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

        return companyInfo;

    }

    private CompanyInfo requestCompanyContact(String id) {
        CompanyInfo companyInfo = null;

        String url = "http://shop" + id + ".ejianlian.com/ajaxGetContact/sid/" + id + "?_=" + System.currentTimeMillis();

        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", url)
                .header("X-Requested-With", "XMLHttpRequest")
                .url(url)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                String json = responseWrap.body.trim();
                GsonBeanContact gsonBeanContact = new Gson().fromJson(json, GsonBeanContact.class);

                if (gsonBeanContact == null)
                    throw new RuntimeException("Json Parse Error ! json = " + json);

                if (gsonBeanContact.getCode() == 1 && gsonBeanContact.getMsg() != null) {
                    companyInfo = new CompanyInfo();

                    String address = gsonBeanContact.getMsg().getRegion_id() + gsonBeanContact.getMsg().getCity_id() + gsonBeanContact.getMsg().getOffice_address();
                    companyInfo.setcAddress(address);

                    if (gsonBeanContact.getMsg().getContacts() == null || gsonBeanContact.getMsg().getContacts().size() < 1) {
                        companyInfo.setcConcat("");
                        companyInfo.setcPhone("");
                        companyInfo.setcQq("");
                    } else {
                        String qq = "";
                        String phone = "";
                        String contact = "";

                        List<GsonBeanContact.MsgBean.ContactsBean> contactsBeanList = gsonBeanContact.getMsg().getContacts();

                        String empty = "null";

                        for (int i = 0; i < contactsBeanList.size(); i++) {
                            GsonBeanContact.MsgBean.ContactsBean contactsBean = contactsBeanList.get(i);

                            String qqTemp = StringUtils.isEmpty(contactsBean.getQq()) ? empty : contactsBean.getQq();
                            String phoneTemp = StringUtils.isEmpty(contactsBean.getTel()) ? empty : contactsBean.getTel();
                            String contactTemp = StringUtils.isEmpty(contactsBean.getName()) ? empty : contactsBean.getName();

                            qq += qqTemp;
                            phone += phoneTemp;
                            contact += contactTemp;

                            if (i < contactsBeanList.size() - 1) {
                                qq += ",";
                                phone += ",";
                                contact += ",";
                            }

                        }

                        companyInfo.setcQq(qq);
                        companyInfo.setcPhone(phone);
                        companyInfo.setcConcat(contact);
                    }
                    return companyInfo;

                } else {
                    throw new RuntimeException("code <> 1 ! json = " + json);
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
     * 从商品页面查询企业信息
     *
     * @param url
     * @return
     */
    public CompanyInfo requestCompanyInfo(String url) {
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
                    String str = HttpUtils.errorStringNoBody(responseWrap);
                    log.e(str, e);
                }

                Elements companyInfoElements = doc.select(".contact li");

                String cContactName = companyInfoElements.get(0).child(1).ownText().trim();

                String cMobile = companyInfoElements.get(1).child(2).attr("data-tel").trim();

                String cPhone = cMobile;

                String cAddress = companyInfoElements.get(2).child(1).ownText().trim();

                companyInfo = new CompanyInfo(IDUtils.genId(platform, cid), cid, cName, cContactName, cMobile, cPhone, "", cAddress);

            } catch (Exception e) {
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(str, e);
                companyInfo = null;
            }
        } else {
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
            companyInfo = null;
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
        final Request request = new Request.Builder()
                .headers(HttpUtils.getCommonHeaders())
                .header("Referer", BASE_URL)
                .url(url)
                .build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                Document doc = Jsoup.parse(responseWrap.body, BASE_URL);
                String _id = doc.getElementById("sid").val().trim();
                Elements companyInfoElements = doc.select(".option-default.clearfix td");
                String cName = "";
                String mobile = "";
                String cAddress = "";
                String cContactName = "";
                String qq = "";
                String cPhone = "";
                if (companyInfoElements.size() < 1) {
                    companyInfoElements = doc.select(".contactus");
                    if (companyInfoElements.size() >= 1) {
                        Element td = companyInfoElements.select("tr").get(0).select("td").get(0);
                        cName = td.select("h1").get(0).text().trim();
                        mobile = td.select(".kefutel").get(0).text().trim();
                        cAddress = td.select("p").get(4).text().trim().split("：")[1];
                        String temp = doc.select(".lxrul").text().trim();
                        temp = temp.substring(1);
                        cContactName = temp.split("    ")[0];
                        cPhone = temp.split("    ")[1];
                        cPhone = cPhone.split("-")[0] + cPhone.split("-")[1] + cPhone.split("-")[2];
                        if (doc.select(".lxrul").get(0).select("a").size() > 0) {
                            String href = doc.select(".lxrul").get(0).select("a").attr("href");
                            qq = href.split("uin=")[1].split("&")[0];
                        }
                        companyInfo = new CompanyInfo(IDUtils.genId(platform, _id), _id, cName, cContactName, mobile, cPhone, qq, cAddress);
                        return companyInfo;
                    } else {
                        throw new RuntimeException("未找到 企业信息 , companyInfoElements.size()  < 1 " + url);
                    }
                }
                Elements companyInfoChildElements = companyInfoElements.get(0).children();

                if (companyInfoChildElements.size() < 1)
                    throw new RuntimeException("未找到 企业信息 ,  companyInfoChildElements.size()  < 1 ");

                // 企业名称
                cName = companyInfoChildElements.get(0).ownText().trim();
                // 企业电话
                mobile = companyInfoChildElements.get(1).text().trim();
                int index = mobile.indexOf('：');

                if (index >= 0) {
                    mobile = mobile.substring(index + 1).trim();
                } else {
                    mobile = "";
                }

                // 企业地址
                cAddress = companyInfoChildElements.get(5).ownText().trim();

                index = cAddress.indexOf('：');

                if (index >= 0) {
                    cAddress = cAddress.substring(index + 1).trim();
                } else {
                    cAddress = "";
                }

                Elements lianXiElements = doc.select(".lianxi");

                // 未找到联系人则从左侧窗口读取联系人信息
                if (lianXiElements.size() < 1) {
                    String phone = "";

                    Elements elements = doc.select(".telbtn");
                    if (elements.size() > 0) {
                        phone = elements.get(0).attr("data-tel").trim();
                    }
                    if (StringUtils.isEmpty(phone))
                        return null;
                    if (StringUtils.isEmpty(cContactName)) cContactName = cName;
                    companyInfo = new CompanyInfo(IDUtils.genId(platform, _id), _id, cName, cContactName, mobile, cPhone, qq, cAddress);
                    return companyInfo;
                }

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
                            String str = HttpUtils.errorStringNoBody(responseWrap);
                            log.e(str, e);
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
                String str = HttpUtils.errorStringNoBody(responseWrap);
                log.e(e.getMessage() + "\n" + str, e);
            }
        } else {
            String str = HttpUtils.errorString(responseWrap);
            log.e(str, responseWrap.e);
        }

        return companyInfo;
    }


    public static class GsonBeanContact {
        private int code;
        private MsgBean msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public MsgBean getMsg() {
            return msg;
        }

        public void setMsg(MsgBean msg) {
            this.msg = msg;
        }

        public static class MsgBean {
            /**
             * office_address : 郑州市南四环金马市场向东150米路南金马航达钢铁物流园11-12号
             * contacts : [{"name":"王红星","tel":"13949145308","qq":""},{"name":"郝鹏帅","tel":"13837197660","qq":""},{"name":"梁经理","tel":"13633841973","qq":""},{"name":"康经理","tel":"18603853678","qq":""}]
             * region_id : 河南省
             * city_id : 郑州市
             */

            private String office_address;
            private String region_id;
            private String city_id;
            private List<ContactsBean> contacts;

            public String getOffice_address() {
                return office_address;
            }

            public void setOffice_address(String office_address) {
                this.office_address = office_address;
            }

            public String getRegion_id() {
                return region_id;
            }

            public void setRegion_id(String region_id) {
                this.region_id = region_id;
            }

            public String getCity_id() {
                return city_id;
            }

            public void setCity_id(String city_id) {
                this.city_id = city_id;
            }

            public List<ContactsBean> getContacts() {
                return contacts;
            }

            public void setContacts(List<ContactsBean> contacts) {
                this.contacts = contacts;
            }

            public static class ContactsBean {
                private String name;
                private String tel;
                private String qq;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getTel() {
                    return tel;
                }

                public void setTel(String tel) {
                    this.tel = tel;
                }

                public String getQq() {
                    return qq;
                }

                public void setQq(String qq) {
                    this.qq = qq;
                }
            }
        }
    }


    public static class GsonBeanShopInfo {
        private int code;
        private MsgBean msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public MsgBean getMsg() {
            return msg;
        }

        public void setMsg(MsgBean msg) {
            this.msg = msg;
        }

        public static class MsgBean {
            private ShopInfoBean shop_info;
            private int deal;

            public ShopInfoBean getShop_info() {
                return shop_info;
            }

            public void setShop_info(ShopInfoBean shop_info) {
                this.shop_info = shop_info;
            }


            public int getDeal() {
                return deal;
            }

            public void setDeal(int deal) {
                this.deal = deal;
            }

            public static class ShopInfoBean {

                private String shop_name;
                private String manager;
                private String tel;
                private String service_tel;

                public String getShop_name() {
                    return shop_name;
                }

                public void setShop_name(String shop_name) {
                    this.shop_name = shop_name;
                }

                public String getManager() {
                    return manager;
                }

                public void setManager(String manager) {
                    this.manager = manager;
                }

                public String getTel() {
                    return tel;
                }

                public void setTel(String tel) {
                    this.tel = tel;
                }

                public String getService_tel() {
                    return service_tel;
                }

                public void setService_tel(String service_tel) {
                    this.service_tel = service_tel;
                }
            }


        }
    }
}
