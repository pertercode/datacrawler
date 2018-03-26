import http.HttpUtils;
import okhttp3.Request;
import org.junit.Test;
import services.ShiJieGCService;

public class EJLTester {


    @Test
    public void runTester() {
        ShiJieGCService service = new ShiJieGCService();
        service.getCompanyInfo("https://qiye.gongchang.com/a634753784/") ;


        System.out.println("最后 : " + service.getlv1().size());
    }


}
