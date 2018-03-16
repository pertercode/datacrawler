package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import cache.CompanyCache;
import com.google.gson.Gson;
import dao.BaseDao;
import services.GCWService;
import services.HuiCongService;
import utils.LogUtils;

import java.awt.print.Pageable;
import java.io.FileReader;
import java.util.List;

/**
 * Created by Administrator on 2018-03-09.
 * 慧聪网抓取数据
 *
 * @author chenyong
 */
public class HuiCongWangThread implements Runnable {
    private LogUtils log = new LogUtils(HuiCongService.platform, HuiCongWangThread.class);

    private HuiCongService baseService = new HuiCongService();
    private BaseDao baseDao = new BaseDao();

    @Override
    public void run() {
        boolean first = true;
        System.out.println(first);
        while (true) {
            crawler(first);
            first = false;
        }
    }

    public void crawler(boolean first) {
        //最低级分类
        List<Category> categories = baseService.requestCateogry();

        if (categories != null && categories.size() > 0) {
            for (int i = 0; i < categories.size(); i++) {
                Category category = categories.get(i);
                Integer maxPage = 0;
                Integer currPage = 1;

                if (first) {
                    //从数据库里查询最大页数
                    int dbMaxPage = baseDao.produceMaxPage(HuiCongService.platform, category.get_id());
                    currPage = dbMaxPage - 2;
                }
                if (currPage < 1) {
                    currPage = 1;
                }
                do {
                    //抓取商品信息
                    List<ProduceInfo> produceInfos = baseService.requestProduce(category, currPage);
                    if (produceInfos != null && produceInfos.size() > 0) {
                        if (maxPage == 0) {
                            maxPage = produceInfos.get(0).getTotalPage();
                        }
                        for (ProduceInfo produceInfo : produceInfos) {
                            //最大页
                            if (maxPage == 0) {
                                maxPage = produceInfo.getTotalPage();
                            }
                            String url = "";

                            //商品详情链接
                            String pUrl = produceInfo.getpUrl();

                            //商品所属企业url
                            String pCurl = produceInfo.getpCUrl();

                            //查询商品对应的企业信息
                            CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());
                            //看看是否有缓存
                            if (companyInfo == null) {
                                //调用根据url获取企业信息的方法
                                companyInfo = baseService.requestCompanyInfo(pCurl);

                                //加入缓存
                                CompanyCache.add(companyInfo);
                            }
                            if (companyInfo != null) {
                                produceInfo.setCategory(category);
                                //设置企业信息
                                baseDao.companyReplace(companyInfo);
                                produceInfo.setCompanyInfo(companyInfo);

                                //存储产品信息
                                baseDao.produceReplace(produceInfo);
                                log.i("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + category.getC_name());
                            } else {
                                log.e("ERROR： requestCompanyInfo 方法返回的 CompanyInfo对象是空 ， 查询URL = " + pCurl + " ， 相关参数 =  " + new Gson().toJson(companyInfo), null);
                            }
                        }

                        if (maxPage <= 0)
                            break;

                        currPage++;
                    } else {
                        log.e("requestProduces 返回的值是 " + produceInfos + " ,  category  =  " + new Gson().toJson(produceInfos) + " , currentPage =  " + currPage, null);
                    }
                } while (currPage <= maxPage);
            }
        }
    }
}
