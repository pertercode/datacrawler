package shop.lezhu.crawler.bean;

public class SearchInfoBean {
    private String cInfo;

    private String cDetail;

    private String cPhone;

    public String getcInfo() {
        return cInfo;
    }

    public void setcInfo(String cInfo) {
        this.cInfo = cInfo;
    }

    public String getcDetail() {
        return cDetail;
    }

    public void setcDetail(String cDetail) {
        this.cDetail = cDetail;
    }

    public String getcPhone() {
        return cPhone;
    }

    public void setcPhone(String cPhone) {
        this.cPhone = cPhone;
    }

    public SearchInfoBean(String cInfo, String cDetail, String cPhone) {
        this.cInfo = cInfo;
        this.cDetail = cDetail;
        this.cPhone = cPhone;
    }

    public SearchInfoBean() {
    }
}
