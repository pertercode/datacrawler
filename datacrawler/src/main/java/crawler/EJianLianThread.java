package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.YJLService;
import services.ZhuNiuBaseService;
import utils.LogUtils;

import java.util.List;

/**
 * E建联抓取程序
 */
public class EJianLianThread implements Runnable {
    private LogUtils log = new LogUtils(YJLService.platform, EJianLianThread.class);
    private YJLService baseService = new YJLService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        crawler();
    }

    public void crawler() {
        baseService.requestCategory();
        List<Category> categories = baseService.categories;

        for (int i = 0; i < categories.size(); i++) {

            Category category = categories.get(i);

            Integer maxPage = 0;

            int currentPage = 1;

            // 从数据库里查询最大页数 - 5 为当前页
            int dbMaxPage = baseDao.produceMaxPage(YJLService.platform, category.get_id());
            currentPage = dbMaxPage - 2;
            if (currentPage < 1) {
                currentPage = 1;
            }

            do {
                List<ProduceInfo> produceInfos = baseService.requestProduces(category, currentPage);

                if (produceInfos != null && produceInfos.size() > 0) {
                    if (maxPage == 0)
                        maxPage = produceInfos.get(0).getTotalPage();

                    for (int j = 0; j < produceInfos.size(); j++) {
                        ProduceInfo produceInfo = produceInfos.get(j);

                        String url = "";

                        // 商品详情连接
                        String purl = produceInfo.getpUrl();
                        // 商品对应得企业信息连接
                        String pCurl = produceInfo.getpCUrl();

                        // 查询商品对应得企业信息，由于商品详情+ 企业信息+ 分类都在一个页面，所以 company不仅有企业信息还有分类信息
                        CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());

                        // 看看是否有缓存
                        if (companyInfo == null) {
                            // 首先从联系我们中查询
                            companyInfo = baseService.requestCompany(pCurl);
                            url = pCurl;

                            if (companyInfo == null) {
                                // 如果没有查询到则从商品页面查询
                                companyInfo = baseService.requestCompanyInfo(purl);
                                url = purl;
                            }
                            // 加入缓存
                            CompanyCache.add(companyInfo);
                        }

                        if (companyInfo != null) {
                            produceInfo.setCategory(category);

                            // 设置企业信息
                            baseDao.companyReplace(companyInfo);
                            produceInfo.setCompanyInfo(companyInfo);

                            // 存储产品信息
                            baseDao.produceReplace(produceInfo);

                            log.i("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + category.getC_name());
                        } else {
                            log.e("ERROR： requestCompanyInfo 方法返回的 CompanyInfo对象是空 ， 查询URL = " + url + " ， 相关参数 =  " + new Gson().toJson(produceInfo), null);
                        }

                    }

                    if (maxPage <= 0)
                        break;

                    currentPage++;
                }
            } while (currentPage <= maxPage);
        }
    }
}
