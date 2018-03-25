package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.HuaMuService;
import services.YuanLinService;
import utils.LogUtils;

import java.util.List;

/**
 * Created by asus on 2018/3/9.
 */
public class YuanLinThread implements Runnable {

    private LogUtils log = new LogUtils(YuanLinService.platform, YuanLinThread.class);

    private static final String platform = YuanLinService.platform;

    private YuanLinService baseService = new YuanLinService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        crawler();
        log.e("抓取完成...", null);
    }

    public void crawler() {

        List<Category> categories = baseService.requestCategory1();

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
                    List<ProduceInfo> produceInfos = baseService.requestProduces(category, currentPage);
                    if (produceInfos != null) {
                        if (produceInfos.size() > 0) {

                            if (maxPage == 0)
                                maxPage = produceInfos.get(0).getTotalPage();

                            for (int j = 0; j < produceInfos.size(); j++) {

                                ProduceInfo produceInfo = produceInfos.get(j);

                                // 商品对应得企业信息连接
                                String pCurl = produceInfo.getpCUrl();

                                // 查询商品对应得企业信息，由于商品详情+ 企业信息+ 分类都在一个页面，所以 company不仅有企业信息还有分类信息
                                CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());
                                // 看看是否有缓存
                                if (companyInfo == null) {
                                    companyInfo = baseService.requestCompany(pCurl);
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
                                    String url = "http://www.yuanlin.com/b2b/" + category.getC_id();
                                    url = url.replaceAll("\\d+\\.html", currentPage + ".html");
                                    log.e("ERROR： requestCompanyInfo 方法返回的 CompanyInfo对象是空 ， 查询URL = " + pCurl + " ， 相关参数 =  " + new Gson().toJson(companyInfo) + " \n 列表 URL = " + url + " , produceName = " + produceInfo.getpName(), null);
                                }
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
}
