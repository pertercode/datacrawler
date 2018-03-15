import bean.Category;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import services.ZhuNiuBaseService;
import utils.IDUtils;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ZhuNiuTester {

    private String baseUrl = "http://www.zhuniu.com/";

    private String platform = "zhuniu";

    private BaseDao baseDao = new BaseDao();


    @Test
    public void runTester() {
    }


}
