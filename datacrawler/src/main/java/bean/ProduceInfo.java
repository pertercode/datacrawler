package bean;

/**
 * 商品信息
 */
public class ProduceInfo {

    // 平台
    private String _id;

    // 所属页
    private Integer page;

    // 总页数
    private Integer totalPage;

    // 商品ID
    private String pId;

    // 商品名称
    private String pName;

    // 商品详情连接
    private String pUrl;

    // 商品所属企业的URL
    private String pCUrl;

    // 商品价格
    private String price;

    // 图片地址
    private String imgSrc;

    private String imgLocal;

    /**
     * 企业名称
     */
    private String cName;

    // 所属分类信息
    private Category category;

    // 所属企业信息
    private CompanyInfo companyInfo;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getpUrl() {
        return pUrl;
    }

    public void setpUrl(String pUrl) {
        this.pUrl = pUrl;
    }

    public String getpCUrl() {
        return pCUrl;
    }

    public void setpCUrl(String pCUrl) {
        this.pCUrl = pCUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getImgLocal() {
        return imgLocal;
    }

    public void setImgLocal(String imgLocal) {
        this.imgLocal = imgLocal;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public CompanyInfo getCompanyInfo() {
        return companyInfo;
    }

    public void setCompanyInfo(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public ProduceInfo() {
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public ProduceInfo(String _id, Integer page, Integer totalPage, String pId, String pName, String pUrl, String price, String imgSrc) {
        this._id = _id;
        this.page = page;
        this.totalPage = totalPage;
        this.pId = pId;
        this.pName = pName;
        this.pUrl = pUrl;
        this.price = price;
        this.imgSrc = imgSrc;
    }
}
