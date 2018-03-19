package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.YJLService;
import services.YunZhuService;
import utils.LogUtils;

import java.util.List;

/**
 * 云筑商城抓取数据
 */
public class YunZhuShangChengThread implements Runnable {

    private LogUtils log = new LogUtils(YunZhuService.platform, YunZhuService.class);
    private YunZhuService baseService = new YunZhuService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        crawler();
        log.e("抓取完成.. ", null);
    }

    public void crawler() {
        List<Category> categories = baseService.requestCategory();
        if (categories != null && categories.size() > 0) {

            for (int i = 0; i < categories.size(); i++) {
                Category category = categories.get(i);

                // 查询规格型号
                baseService.requestTypeName(category);

                Integer maxPage = 0;
                int currentPage = 1;

                int dbMaxPage = baseDao.produceMaxPage(YunZhuService.platform, category.get_id());
                currentPage = dbMaxPage - 5;
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
                            // 最大页数
                            if (maxPage == 0)
                                maxPage = produceInfo.getTotalPage();

                            // 企业信息页面
                            String pCurl = produceInfo.getpCUrl();

                            CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());

                            // 看看是否有缓存
                            if (companyInfo == null) {
                                companyInfo = baseService.requestCompany(pCurl);

                                String address = baseService.requestAddress(companyInfo);
                                companyInfo.setcAddress(companyInfo.getcAddress() + address);

                                // 加入缓存
                                CompanyCache.add(companyInfo);
                            }


                            if (companyInfo != null) {
                                baseDao.companyReplace(companyInfo);

                                produceInfo.setCategory(category);
                                produceInfo.setCompanyInfo(companyInfo);
                                baseDao.produceReplace(produceInfo);

                                log.i("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + category.getC_name());
                            } else {
                                log.e("ERROR： requestCompanyInfo 方法返回的 CompanyInfo对象是空 ， 查询URL = " + pCurl + " ， 相关参数 =  " + new Gson().toJson(produceInfo), null);
                            }

                        }

                        if (maxPage <= 0)
                            break;

                        currentPage++;
                    }
                } while (currentPage <= maxPage);
            }


        } else {
            log.e("requestCategory  返回的 categories 为空，或者 categories.size()  < 1", null);
        }
    }
}
