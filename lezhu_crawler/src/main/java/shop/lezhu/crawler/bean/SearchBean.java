package shop.lezhu.crawler.bean;

import com.google.gson.annotations.SerializedName;
import shop.lezhu.crawler.utils.StringUtils;

public class SearchBean {

    @SerializedName(value = "keyword")
    private String key;

    @SerializedName(value = "region")
    private String location;

    @SerializedName(value = "company")
    private String company;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * 判断是否可以查询，可以查询得条件是key 和 location 都合法
     *
     * @return
     */
    public boolean canSearch() {
        boolean canSearch = false;

        if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(location)) {

            // 如果不是中文或者符号则直接退出
            if (!StringUtils.isChinese(location)) return false;

            if (!location.contains(":")) {
                return true;
            }

            String[] locations = location.split(":");

            if (locations.length > 0) {
                if (locations.length == 2) {
                    return location.contains("省");

                } else if (locations.length == 3) {
                    return location.contains("省") && location.contains("市");
                }
            }
        }
        return canSearch;
    }

    public SearchBean() {
    }

    public SearchBean(String key, String location) {
        this.key = key;
        this.location = location;
    }
}
