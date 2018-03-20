import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import com.google.gson.Gson;
import org.junit.Test;
import services.WuAGeService;
import services.YJLService;

import java.util.List;

public class WuAgeTester {


    @Test
    public void runTester() {
        WuAGeService service = new WuAGeService();
        List<Category> categoryList = service.requestCateogry() ;

        ProduceInfo produceInfo = service.requestProduceInfo(categoryList.get(0) , 1).get(0);

        service.requestCompanyInfo(produceInfo.getpCUrl()) ;
    }


}
