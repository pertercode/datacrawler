package shop.lezhu.crawler.bean;

import com.google.gson.annotations.SerializedName;
import shop.lezhu.crawler.utils.StringUtils;

public class SearchBean {

    private String id;
    private String contact_name;
    private String contact_phone;

    // 关键词
    private String keyword;

    // 商品名
    private String goods_name;
    // 地区
    private String region;
    // 数量
    private String count;
    // 单位
    private String demand_unit;
    private String address;
    // 企业名称
    private String company;

    // 连接
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public String getKeyword() {
        if (keyword == null || keyword.length() < 1) {
            return getGoods_name();
        }
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getDemand_unit() {
        return demand_unit;
    }

    public void setDemand_unit(String demand_unit) {
        this.demand_unit = demand_unit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SearchBean() {
    }

    public SearchBean(String id, String contact_name, String contact_phone, String goods_name, String keyword, String region, String count, String demand_unit, String address, String company) {
        this.id = id;
        this.contact_name = contact_name;
        this.contact_phone = contact_phone;
        this.goods_name = goods_name;
        this.keyword = keyword;
        this.region = region;
        this.count = count;
        this.demand_unit = demand_unit;
        this.address = address;
        this.company = company;
    }

    /**
     * 判断是否可以查询，可以查询得条件是key 和 location 都合法
     *
     * @return
     */
    public boolean canSearch() {
        boolean canSearch = false;

        if (!StringUtils.isEmpty(keyword) && !StringUtils.isEmpty(region)) {

            // 如果不是中文或者符号则直接退出
            if (!StringUtils.isChinese(region)) return false;

            if (!region.contains(":")) {
                return true;
            }

            String[] locations = region.split(":");

            if (locations.length > 0) {
                if (locations.length == 2) {
                    return region.contains("省");

                } else if (locations.length == 3) {
                    return region.contains("省") && region.contains("市");
                }
            }
        }
        return canSearch;
    }


    public String getBuyInfo() {
        String buyInfo = getGoods_name() + " " + getCount() + getDemand_unit();
        return buyInfo;
    }

}
