package crawler;

import bean.Category;
import dao.BaseDao;
import services.GCWService;
import utils.LogUtils;

import java.util.List;

/**
 * Created by asus on 2018/3/9.
 */
public class GCWThread implements Runnable  {
    private LogUtils log = new LogUtils(GCWService.platform, GCWThread.class);
    private GCWService baseService = new GCWService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        while(true){
            crawler();
        }
    }

    public void crawler (){
        List<Category> categories =  baseService.requestCategory() ;

        if(categories != null){
            for (Category c: categories) {
                baseService.getType(c);
            }
        }


    }
}
