package bean;

public class CompanyInfo {

    private String _id;

    // 企业ID
    private String cId;

    // 企业名称
    private String cName;

    // 联系人
    private String cConcat;

    // 电话
    private String cMobile;

    // 手机
    private String cPhone;

    // QQ
    private String cQq;

    // 地址
    private String cAddress;

    // 分类
    public String category;


    private String cUrl;


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getcConcat() {
        return cConcat;
    }

    public void setcConcat(String cConcat) {
        this.cConcat = cConcat;
    }

    public String getcMobile() {
        return cMobile;
    }

    public void setcMobile(String cMobile) {
        this.cMobile = cMobile;
    }

    public String getcPhone() {
        return cPhone;
    }

    public void setcPhone(String cPhone) {
        this.cPhone = cPhone;
    }

    public String getcQq() {
        return cQq;
    }

    public void setcQq(String cQq) {
        this.cQq = cQq;
    }

    public String getcAddress() {
        return cAddress;
    }

    public void setcAddress(String cAddress) {
        this.cAddress = cAddress;
    }

    public String getcUrl() {
        return cUrl;
    }

    public void setcUrl(String cUrl) {
        this.cUrl = cUrl;
    }

    public CompanyInfo() {
    }

    public CompanyInfo(String _id, String cId, String cName, String cConcat, String cMobile, String cPhone, String cQq, String cAddress) {
        this._id = _id;
        this.cId = cId;
        this.cName = cName;
        this.cConcat = cConcat;
        this.cMobile = cMobile;
        this.cPhone = cPhone;
        this.cQq = cQq;
        this.cAddress = cAddress;
    }
}
