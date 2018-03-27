import bean.CompanyInfo;
import com.google.gson.Gson;
import dao.BaseDao;
import http.HttpUtils;
import okhttp3.Request;
import org.junit.Test;
import services.ShiJieGCService;
import services.WJWService;

import java.awt.image.BufferedImage;

public class EJLTester {
    @Test
    public void runTester() {
//        WJWService service = new WJWService();
//        service.requestCategory() ;

        ShiJieGCService service = new ShiJieGCService();
        CompanyInfo companyInfo = service.getCompanyInfo("https://qiye.gongchang.com/zengjunwen/");
        System.out.println(new Gson().toJson(companyInfo));

    }
}
