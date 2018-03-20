package crawler;

import bean.Category;
import services.GCWService;
import utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2018/3/9.
 */
public class GCWThread implements Runnable {
    private LogUtils log = new LogUtils(GCWService.platform, GCWThread.class);
    private GCWService baseService = new GCWService();

    @Override
    public void run() {
        crawler();
        log.e("抓取完成.. ", null);
    }

    public void crawler() {
        List<Category> categories = baseService.requestCategory();
        if (categories != null) {
            for (Category c : categories) {
                baseService.getType(c);
                List<Category> lowest=baseService.getLowestCategory(c);
                if (lowest != null) {
                    for (Category lowestc : lowest) {
                        baseService.getType(lowestc);
                    }
                } else {
                    log.e("最底层分类为空", null);
                }
            }
        } else {
            log.e("分类为空", null);
        }


    }
}
