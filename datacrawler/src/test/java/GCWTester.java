import bean.Category;
import crawler.GCWThread;
import org.junit.Test;
import services.GCWService;

import java.util.List;

public class GCWTester {


    @Test
    public void runTester() {
//        GCWService gcwService = new GCWService();
//        List<Category> categories=gcwService.requestCategory();
////        Category category = new Category();
////        category.setC_url("http://www.gldjc.com/scj/so.html?l=1&terms=%5B%7Bid%3A%22category2_id%22%2Cname%3A%2228%22%7D%5D");
////        category.set_id("a");
//        gcwService.getLowestCategory(categories.get(0));
        GCWThread thread=new GCWThread();
        thread.run();
    }


}
