package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.WuAGeService;
import utils.LogUtils;

import java.util.List;


public class WuAGwThread implements Runnable {
    private LogUtils log = new LogUtils(WuAGeService.platform, WuAGwThread.class);
    private WuAGeService baseService = new WuAGeService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        try {

            crawler();
        } catch (Exception e) {
            e.printStackTrace();
            log.e(e.getMessage(), e);
        }
        log.e("抓取完成.. ", null);
    }


    public void crawler() {

        //最底层分类
        List<Category> categories = baseService.requestCateogry();

        if (categories != null && categories.size() > 0) {
            for (Category category : categories) {

                Integer maxPage = 0;
                Integer currPage = 1;
                //从数据库里查询最大页数
                int dbMaxPage = baseDao.produceMaxPage(WuAGeService.platform, category.get_id());
                currPage = dbMaxPage - 5;

                if (currPage < 1) {
                    currPage = 1;
                }

                do {
                    //抓取商品信息
                    List<ProduceInfo> produceInfos = baseService.requestProduceInfo(category, currPage);

                    System.out.println("produceInfos.size() = " + produceInfos.size());

                    if (produceInfos != null && produceInfos.size() > 0) {
                        if (maxPage == 0) {
                            maxPage = produceInfos.get(0).getTotalPage();
                        }

                        for (ProduceInfo produceInfo : produceInfos) {

                            //商品所属企业Url
                            String pCUrl = produceInfo.getpCUrl();

                            //查询商品对应的企业信息
                            CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());

                            //看看是否有缓存
                            if (companyInfo == null) {
                                //调用根据url获取企业信息的方法
                                companyInfo = baseService.requestCompanyInfo(pCUrl);
                                //加入缓存
                                CompanyCache.add(companyInfo);
                            }

                            if (companyInfo != null) {
                                baseDao.companyReplace(companyInfo);
                                produceInfo.setCategory(category);
                                produceInfo.setCompanyInfo(companyInfo);
                                //存储产品信息
                                baseDao.produceReplace(produceInfo);

                                log.i("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + category.getC_name());
                            } else {
                                log.e("companyInfo is null , pCUrl =  " + pCUrl, null);
                            }
                        }


                        currPage++;

                    } else {
                        log.e("is null , category =  " + new Gson().toJson(category) + " ,  page = " + currPage, null);
                    }

                    if (maxPage <= 0)
                        break;

                } while (currPage <= maxPage);

            }
        }
    }
}
