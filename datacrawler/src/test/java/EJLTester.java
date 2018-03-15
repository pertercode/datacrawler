import bean.Category;
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
        YJLService service = new YJLService() ;
        service.requestCompany("http://shop3364.ejianlian.com/contact/");
    }



}
