import bean.Category;
import bean.CompanyInfo;
import com.google.gson.Gson;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import services.YJLService;
import services.ZhuNiuBaseService;
import utils.IDUtils;
import utils.StringUtils;

public class EJLTester {


    @Test
    public void runTester() {
        YJLService service = new YJLService();
        CompanyInfo companyInfo =  service.requestCompanyWithCId("3357");

    }


}
