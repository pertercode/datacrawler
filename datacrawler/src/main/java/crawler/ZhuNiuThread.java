package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import http.HttpUtils;
import services.ZhuNiuBaseService;
import utils.LogUtils;

import java.util.List;
import java.util.logging.Logger;

/**
 * 筑牛网抓取程序
 */
public class ZhuNiuThread implements Runnable {


    LogUtils log = new LogUtils(ZhuNiuBaseService.platform, ZhuNiuThread.class);

    private ZhuNiuBaseService baseService = new ZhuNiuBaseService();

    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        baseService.requestCategory("2");
        while (true)
            crawler();
    }


    public void crawler() {
        Integer maxPage = 9999999;

        int currentPage = 1;

        // 从数据库里查询最大页数 - 5 为当前页
        int dbMaxPage = baseDao.produceMaxPage(ZhuNiuBaseService.platform);

        currentPage = dbMaxPage - 2;

        if (currentPage < 1) {
            currentPage = 1;
        }

        int emptyPage = 0;

        do {
            List<ProduceInfo> produceInfos = baseService.requestAllProduce(currentPage);

            if (produceInfos != null) {

                if (produceInfos.size() > 0) {

                    for (int i = 0; i < produceInfos.size(); i++) {

                        ProduceInfo produceInfo = produceInfos.get(i);

                        String url = produceInfo.getpUrl();

                        // 查询商品对应得企业信息，由于商品详情+ 企业信息+ 分类都在一个页面，所以 company不仅有企业信息还有分类信息
                        CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());

                        // 看看是否有缓存
                        if (companyInfo == null) {
                            companyInfo = baseService.requestCompanyInfo(url);
                            // 加入缓存
                            CompanyCache.add(companyInfo);
                        }

                        if (companyInfo != null) {
                            // 设置类别信息
                            Category category = baseService.compCategory(companyInfo.category);

                            if (category != null) {
                                produceInfo.setCategory(category);

                                // 设置企业信息
                                baseDao.companyReplace(companyInfo);
                                produceInfo.setCompanyInfo(companyInfo);

                                // 存储产品信息
                                baseDao.produceReplace(produceInfo);

                                log.i("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + produceInfo.getCompanyInfo().category);
                            } else {
                                log.e("ERROR： compCategory 方法返回的 Category ，  companyInfo.category = " + companyInfo.category, null);
                            }

                        } else {
                            log.e("ERROR： requestCompanyInfo 方法返回的 CompanyInfo对象是空 ， 查询URL = " + url + " ， 相关参数 =  " + new Gson().toJson(produceInfo), null);
                        }
                    }
                    currentPage++;
                } else {
                    // 如果3个空页面则退出
                    if (emptyPage > 3) {
                        break;
                    }
                    emptyPage++;
                }
            }

        } while (currentPage <= maxPage);
    }

}
