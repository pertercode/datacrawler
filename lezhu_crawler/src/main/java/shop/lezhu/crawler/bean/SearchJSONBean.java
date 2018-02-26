package shop.lezhu.crawler.bean;

import java.util.List;

public class SearchJSONBean {


    private int code;
    private List<SearchBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<SearchBean> getData() {
        return data;
    }

    public void setData(List<SearchBean> data) {
        this.data = data;
    }


}
