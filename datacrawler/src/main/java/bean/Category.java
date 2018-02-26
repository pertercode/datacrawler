package bean;

/**
 * 分类表
 */
public class Category {

    private String _id;

    // 平台
    private String platform;

    // 分类ID
    private String c_id;

    // 分类名称
    private String c_name;

    // 分类级别
    private Integer c_level;

    // 是否为最低分类
    private Integer c_islow;

    // 父分类的名称
    private String c_parent;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getC_id() {
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public String getC_name() {
        return c_name;
    }

    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public Integer getC_level() {
        return c_level;
    }

    public void setC_level(Integer c_level) {
        this.c_level = c_level;
    }

    public Integer getC_islow() {
        return c_islow;
    }

    public void setC_islow(Integer c_islow) {
        this.c_islow = c_islow;
    }

    public String getC_parent() {
        return c_parent;
    }

    public void setC_parent(String c_parent) {
        this.c_parent = c_parent;
    }

    public Category() {
    }

    public Category(String _id, String platform, String c_id, String c_name, Integer c_level, Integer c_islow, String c_parent) {
        this._id = _id;
        this.platform = platform;
        this.c_id = c_id;
        this.c_name = c_name;
        this.c_level = c_level;
        this.c_islow = c_islow;
        this.c_parent = c_parent;
    }

}
