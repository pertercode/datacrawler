package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.ZhuNiuBaseService;
import utils.LogUtils;

import java.util.List;

/**
 * 筑牛网抓取程序
 */
public class ZhuNiuThread implements Runnable {
    LogUtils log = new LogUtils(ZhuNiuBaseService.platform, ZhuNiuThread.class);

    private ZhuNiuBaseService baseService = new ZhuNiuBaseService();

    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        crawler(true);
    }


    public void crawler(boolean first) {
        // 查询分类，如果遇到 最低层，则查询规格 ，查询完规格后开始检索商品信息
        baseService.requestCategory("2");


    }

}
