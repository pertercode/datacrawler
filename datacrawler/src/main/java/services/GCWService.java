package services;

import bean.Category;
import bean.TypeName;
import bean.TypeValue;
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GCWService {
    private LogUtils log = new LogUtils(platform, GCWService.class);
    public static final String BASE_URL = "http://www.gldjc.com/";
    public static final String platform = "guangcaiwang";
    private BaseDao baseDao = new BaseDao();


    public List<Category> requestCategory() {
        List<Category> categories = null;
        String url = "http://www.gldjc.com/getMaterailCatetoryList.ajax";

        Request request = new Request.Builder().url(url).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                categories = new ArrayList<Category>();
                String body = responseWrap.body;

                Gson gson = new Gson();
                M m = gson.fromJson(body, M.class);

                List<JsonList> categorylist = m.getMaterailCatetorylList();

                for (JsonList list : categorylist) {
                    String Level1name = list.getName();
                    String level1Cid = list.getMaterialCategoryId().toString();
                    String level1_id = IDUtils.genId(platform, level1Cid);

                    Category categoryLevel1 = new Category(level1_id, platform, level1Cid, Level1name, 0, 0, "0");
                    baseDao.categoryReplace(categoryLevel1);
                    log.i("1级分类入库"+categoryLevel1.getC_id()+"  "+categoryLevel1.getC_name());
                    for (JsonList list2 : list.getCatetoryList()) {
                        String Level2name = list2.getName();
                        String level2Cid = list2.getMaterialCategoryId().toString();
                        String level2_id = IDUtils.genId(platform, level2Cid);
                        Category categoryLevel2 = new Category(level2_id, platform, level2Cid, Level2name, 1, 0, categoryLevel1.get_id());
                        baseDao.categoryReplace(categoryLevel2);
                        log.i("2级分类入库"+categoryLevel2.getC_id()+"  "+categoryLevel2.getC_name()+"  "+categoryLevel2.getC_parent());
                        for (JsonList list3 : list2.getCatetoryList()) {
                            String Level3name = list3.getName();
                            String level3Cid = list3.getMapingId().toString();
                            String level3_id = IDUtils.genId(platform, level3Cid);
                            Category categoryLevel3 = new Category(level3_id, platform, level3Cid, Level3name, 2, 0, categoryLevel2.get_id());
                            categoryLevel3.setC_url("http://www.gldjc.com/scj/so.html?l=1&terms=%5B%7Bid%3A%22category2_id%22%2Cname%3A%22" + URLEncoder.encode(level3Cid, "UTF-8") + "%22%7D%5D");
                            categories.add(categoryLevel3);
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

    /**
     * 获取最低级分类
     *
     * @return 最低级分类
     */
    public List<Category> getLowestCategory(Category category) {
        List<Category> categories = null;

        Request request = new Request.Builder().url(category.getC_url()).headers(HttpUtils.getCommonHeaders()).build();
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);

        if (responseWrap.isSuccess()) {
            try {
                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);
                categories = new ArrayList<Category>();
                Elements keys = doc.select(".a-key");

                boolean flag = false;
                for (int i = 0; i < keys.size(); i++) {
                    if (keys.get(i).text().trim().startsWith("品种")) {
                        Element values = doc.select(".a-values").get(i);
                        Elements a = values.select(".fl-list").get(0).select("li a");

                        for (int j = 0; j < a.size(); j++) {

                            Element levellowest = a.get(j);
                            String name = levellowest.text().trim();
                            String cid = levellowest.attr("name");
                            String _id = IDUtils.genId(platform, cid);
                            Category categorylowest = new Category(_id, platform, cid, name, 3, 1, category.get_id());
                            categorylowest.setC_url("http://www.gldjc.com/scj/so.html?l=1&terms=[{id:%22category2_id%22,name:%22" + category.getC_id() + "%22},{id:%22attr_%E5%93%81%E7%A7%8D%22,name:%22" + URLEncoder.encode(name, "UTF-8") + "%22}]");
                            log.i("4级分类入库"+categorylowest.getC_id()+"  "+categorylowest.getC_name()+"  "+categorylowest.getC_parent());
                            baseDao.categoryReplace(categorylowest);
                            categories.add(categorylowest);

                            flag = true;
                        }

                    }
                }

                if (!flag) {
                    category.setC_islow(1);
                    log.i("此3级分类无下一层"+category.getC_id()+"  "+category.getC_name()+"  "+category.getC_url());
                }
                baseDao.categoryReplace(category);
                log.i("3级分类入库"+category.getC_id()+"  "+category.getC_name()+"  "+category.getC_parent());

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
     * 获取分类信息
     */
    public void getType(Category category) {
        // 把URL创建好了
        Request request = new Request.Builder().url(category.getC_url()).headers(HttpUtils.getCommonHeaders()).build();
        // 发送请求
        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttpNoProxy(request);
        if (responseWrap.isSuccess()) {
            try {
                baseDao.typeNameDelete(category.get_id());
                baseDao.typeValueDelete(category.get_id());

                String body = responseWrap.body;
                Document doc = Jsoup.parse(body);
                Elements elements = doc.select(".classify .classifysBox");
                for (int i = 0; i < elements.size(); i++) {
                    Element type = elements.get(i);

                    TypeName typeName = new TypeName();
                    typeName.settName(type.select(".a-key").get(0).text().trim().split(":")[0]);

                    if (typeName.gettName().equals("品种")) {
                        continue;
                    }
                    String nameId = IDUtils.uuid();
                    typeName.set_id(IDUtils.genId(platform, nameId));
                    typeName.setCategoryId(category.get_id());
                    baseDao.typeNameReplace(typeName);

                    log.i("分类名称入库了 : " + typeName.gettName());

                    Elements value = elements.get(i).select(".a-values .fl-list li");
                    for (int j = 0; j < value.size(); j++) {
                        Element li = value.get(j);
                        Element a = li.select("a").get(0);

                        TypeValue typeValue = new TypeValue();
                        typeValue.set_id(IDUtils.genId(typeName.get_id(), IDUtils.uuid()));
                        typeValue.settValue(a.text().trim());
                        typeValue.setTypeNameId(typeName.get_id());
                        typeValue.setCategoryId(category.get_id());

                        baseDao.typeValueReplace(typeValue);

                        log.i("值入库了 ： " + typeValue.gettValue());
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
     * 解析json用的实体类
     */
    static class M {
        private List<JsonList> materailCatetorylList = null;

        public List<JsonList> getMaterailCatetorylList() {
            return materailCatetorylList;
        }

        public void setMaterailCatetorylList(List<JsonList> materailCatetorylList) {
            this.materailCatetorylList = materailCatetorylList;
        }
    }

    static class JsonList {
        private Integer materialCategoryId;
        private String name;
        private Integer parentId;
        private Integer sort;
        private List<JsonList> catetoryList = null;
        private String ssName;
        private String isShow;
        private String createdAt;
        private String updatedAt;
        private String mapingId;

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public void setMapingId(String mapingId) {
            this.mapingId = mapingId;
        }

        public void setIsShow(String isShow) {
            this.isShow = isShow;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setSsName(String ssName) {
            this.ssName = ssName;
        }

        public String getSsName() {
            return ssName;
        }

        public String getMapingId() {
            return mapingId;
        }

        public String getIsShow() {
            return isShow;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public List<JsonList> getCatetoryList() {
            return catetoryList;
        }

        public void setCatetoryList(List<JsonList> catetoryList) {
            this.catetoryList = catetoryList;
        }

        public Integer getMaterialCategoryId() {
            return materialCategoryId;
        }

        public void setMaterialCategoryId(Integer materialCategoryId) {
            this.materialCategoryId = materialCategoryId;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}