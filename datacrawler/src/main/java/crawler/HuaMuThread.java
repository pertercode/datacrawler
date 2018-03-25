package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.HuaMuService;
import services.YJLService;
import utils.LogUtils;

import java.util.List;

public class HuaMuThread implements Runnable {

    private LogUtils log = new LogUtils(HuaMuService.platform, HuaMuThread.class);

    private HuaMuService baseService = new HuaMuService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        //最底层分类
        List<Category> categoryList = baseService.requestCategory();

        if (categoryList != null && categoryList.size() > 0) {
            for (Category category : categoryList) {
                baseService.getType(category);

                Integer currPage = 1;
                int maxPage = 0;

                do {
                    //抓取商品信息
                    List<ProduceInfo> produceInfos = baseService.requestProduceInfo(category, currPage);

                    if (produceInfos != null) {
                        if (produceInfos.size() > 0) {

                            if (maxPage == 0) {
                                maxPage = produceInfos.get(0).getTotalPage();
                            }

                            for (ProduceInfo produceInfo : produceInfos) {

                                //商品所属企业的url
                                String pCurl = produceInfo.getpCUrl();

                                //查询商品对应的企业信息
                                CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());

                                //查看是否有缓存
                                if (companyInfo == null) {
                                    //调用根据url获取企业的的对象
                                    companyInfo = baseService.requestCompanyInfo(pCurl);

                                    //加入缓存
                                    CompanyCache.add(companyInfo);
                                }

                                if (companyInfo != null) {
                                    //存储企业信息
                                    baseDao.companyReplace(companyInfo);
                                    //设置分类
                                    produceInfo.setCategory(category);
                                    //设置企业信息
                                    produceInfo.setCompanyInfo(companyInfo);

                                    //存储产品信息
                                    baseDao.produceReplace(produceInfo);
                                    log.i("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + category.getC_name());
                                } else {
                                    log.i("ERROR： requestCompanyInfo 方法返回的 CompanyInfo对象是空 ， 查询URL = " + pCurl + " ， 相关参数 =  " + new Gson().toJson(companyInfo));
                                }
                            }
                        }

                        if (maxPage <= 0)
                            break;

                        currPage++;
                    }
                } while (currPage <= maxPage);
            }
        }
    }
}
