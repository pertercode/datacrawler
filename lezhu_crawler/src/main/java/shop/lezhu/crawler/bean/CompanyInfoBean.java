package shop.lezhu.crawler.bean;

import com.google.gson.annotations.SerializedName;
import shop.lezhu.crawler.utils.StringUtils;

import java.util.List;

/**
 * Created by wushiling on 2018/1/8.
 */

public class CompanyInfoBean {

    @SerializedName(value = "id")
    private String id;

    @SerializedName(value = "isAuth")
    private String isAuth;

    private String key;

    private String location;

    private Integer page;

    private Integer no;

    // 公司名称
    @SerializedName(value = "name")
    private String companyName;

    // 联系人名称
    @SerializedName(value = "contactor")
    private String contactsName;

    // 公司所有人名称
    @SerializedName(value = "corowner")
    private String ownerName;

    // 先生OR女士
    @SerializedName(value = "male")
    private String male;

    // 职务
    @SerializedName("duty")
    private String duty;


    // QQ 比较特殊
    @SerializedName(value = "lstCorQQ")
    private List<LstCorQQBean> lstCorQQ;

    // 手机
    @SerializedName(value = "mp")
    private String phone;

    // 电话
    @SerializedName(value = "telephone")
    private String telephone;

    // 其他电话
    @SerializedName(value = "otherTelephone")
    private String otherTelephone;

    // 所在省
    @SerializedName(value = "provinceName")
    private String provinceName;

    // 所在市
    @SerializedName(value = "cityName")
    private String cityName;

    // 所在区
    @SerializedName(value = "address")
    private String address;

    // 注册地址
    @SerializedName(value = "regaddress")
    private String regaddress;

    // 经营范围
    @SerializedName(value = "areaName")
    private String areaName;


    /**
     * 获得手机号
     * @return
     */
    public String getNumber() {
        if (!StringUtils.isEmpty(phone) && phone.trim().length() >= 11) return phone.trim();
        if (!StringUtils.isEmpty(telephone) && telephone.trim().length() >= 11) return telephone.trim();
        if (!StringUtils.isEmpty(otherTelephone) && otherTelephone.trim().length() >= 11) return otherTelephone.trim();
        return "";
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsAuth() {
        return isAuth;
    }

    public void setIsAuth(String isAuth) {
        this.isAuth = isAuth;
    }

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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactsName() {
        return contactsName;
    }

    public void setContactsName(String contactsName) {
        this.contactsName = contactsName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getMale() {
        return male;
    }

    public void setMale(String male) {
        this.male = male;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getOtherTelephone() {
        return otherTelephone;
    }

    public void setOtherTelephone(String otherTelephone) {
        this.otherTelephone = otherTelephone;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegaddress() {
        return regaddress;
    }

    public void setRegaddress(String regaddress) {
        this.regaddress = regaddress;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<LstCorQQBean> getLstCorQQ() {
        return lstCorQQ;
    }

    public void setLstCorQQ(List<LstCorQQBean> lstCorQQ) {
        this.lstCorQQ = lstCorQQ;
    }


    public CompanyInfoBean(String companyName, String contactsName, String ownerName, String male, String duty, List<LstCorQQBean> lstCorQQ, String phone, String telephone, String otherTelephone, String provinceName, String cityName, String address, String regaddress, String areaName) {
        this.companyName = companyName;
        this.contactsName = contactsName;
        this.ownerName = ownerName;
        this.male = male;
        this.duty = duty;
        this.lstCorQQ = lstCorQQ;
        this.phone = phone;
        this.telephone = telephone;
        this.otherTelephone = otherTelephone;
        this.provinceName = provinceName;
        this.cityName = cityName;
        this.address = address;
        this.regaddress = regaddress;
        this.areaName = areaName;
    }

    public static class LstCorQQBean {
        private String qq;

        public String getQq() {
            return qq;
        }

        public void setQq(String qq) {
            this.qq = qq;
        }
    }
}
