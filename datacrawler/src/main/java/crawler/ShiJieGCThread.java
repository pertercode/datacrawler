package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.ShiJieGCService;
import services.ZhuNiuBaseService;
import utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 世界工厂
 */
public class ShiJieGCThread implements Runnable {

    LogUtils log = new LogUtils(ShiJieGCService.platform, ShiJieGCThread.class);

    private static final String platform = ShiJieGCService.platform;

    private ShiJieGCService baseService = new ShiJieGCService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        crawler();
        log.e("抓取完毕", null);
    }

    public void crawler() {
        List<Category> categoryList = baseService.getlv1();
//        List<Category> categoryList = baseDao.categorySelectList(ShiJieGCService.platform,1) ;
        dosearch(categoryList);
    }

    public void dosearch(List<Category> categories) {
        if (categories != null && categories.size() > 0) {
            for (int i = 0; i < categories.size(); i++) {

                Category category = categories.get(i);
                Integer maxPage = 0;
                int currentPage = 1;

                // 从数据库里查询最大页数 - 5 为当前页
                int dbMaxPage = baseDao.produceMaxPage(platform, category.get_id());

                currentPage = dbMaxPage - 2;
                if (currentPage < 1) {
                    currentPage = 1;
                }

                do {
                    List<ProduceInfo> produceInfos = baseService.getProduceInfo(category, currentPage);

                    if (produceInfos != null) {
                        if (produceInfos.size() > 0) {
                            if (maxPage == 0)
                                maxPage = produceInfos.get(0).getTotalPage();
                            for (int j = 0; j < produceInfos.size(); j++) {
                                ProduceInfo produceInfo = produceInfos.get(j);
                                // 最大页数
                                if (maxPage == 0)
                                    maxPage = produceInfo.getTotalPage();


                                // 商品对应得企业信息连接
                                String pCurl = produceInfo.getpCUrl();

                                // 查询商品对应得企业信息，由于商品详情+ 企业信息+ 分类都在一个页面，所以 company不仅有企业信息还有分类信息
                                CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());
                                // 看看是否有缓存
                                if (companyInfo == null) {
                                    companyInfo = baseService.getCompanyInfo(pCurl);
                                    // 加入缓存
                                    CompanyCache.add(companyInfo);
                                }
                                if (companyInfo != null) {
                                    // 设置企业信息
                                    baseDao.companyReplace(companyInfo);

                                    produceInfo.setCategory(category);
                                    produceInfo.setCompanyInfo(companyInfo);
                                    // 存储产品信息
                                    baseDao.produceReplace(produceInfo);

                                    log.i("产品入库" + "  " + produceInfo.getpName() + " ,  " + "企业入库" + "  " + companyInfo.getcName());
                                }
                            }
                        }
                        if (maxPage <= 0)
                            break;
                        currentPage++;
                    } else {
                        log.e("requestProduces 返回的值是 null ,  category  =  " + new Gson().toJson(category) + " , currentPage =  " + currentPage, null);
                    }
                } while (currentPage <= maxPage);
            }
        }
    }
}
