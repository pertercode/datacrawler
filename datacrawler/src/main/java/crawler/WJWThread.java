//package crawler;
//
//import bean.Category;
//import bean.CompanyInfo;
//import bean.ProduceInfo;
//import cache.CompanyCache;
//import com.google.gson.Gson;
//import dao.BaseDao;
//import services.WJWService;
//import services.YunZhuService;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 五金网抓取数据
// */
//public class WJWThread implements Runnable {
//
//    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WJWThread.class);
//
//    private static final String platform = "wujinwang";
//
//    private WJWService baseService = new WJWService();
//    private BaseDao baseDao = new BaseDao();
//
//    @Override
//    public void run() {
//        List<Category> categories = new ArrayList<Category>();
//        List<Category> list = baseService.wujinrequestCateogry();
//        for (Category l1:list) {
//            List<Category> list2 = baseService.wujinrequestCateogry2(list.get(0));
//            for (Category l2:list2) {
//                if(l2.getC_islow()==0){
//                    List<Category> list3 = baseService.wujinrequestCateogry3(list2.get(0));
//                    for (Category l3:list3) {
//                        if(l3.getC_islow()==0){
//                            List<Category> list4=baseService.wujinrequestCateogry4(list3.get(0));
//                            if(list4!=null){
//                                categories.addAll(list4);
//                            }
//                        }else{
//                            categories.add(l3);
//                        }
//                    }
//                }else{
//                    categories.add(l2);
//                }
//            }
//        }
//        /**
//         *  获取categories
//         */
//        if (categories != null && categories.size() > 0) {
//            for (int i = 0; i < categories.size(); i++) {
//                Category category = categories.get(i);
//                Integer maxPage = 0;
//
//                int currentPage = 1;
//
//                // 从数据库里查询最大页数 - 5 为当前页
//                int dbMaxPage = baseDao.produceMaxPage(platform, category.get_id());
//
//                currentPage = dbMaxPage - 2;
//
//                if (currentPage < 1) {
//                    currentPage = 1;
//                }
//
//                do {
//                    //获取产品信息
//                    List<ProduceInfo> produceInfos = baseService.wujinwujinrequestCateogry5(category,currentPage);
//
//                    if (produceInfos != null) {
//
//                        if (produceInfos.size() > 0) {
//
//                            if (maxPage == 0)
//                                maxPage = produceInfos.get(0).getTotalPage();
//
//                            for (int j = 0; j < produceInfos.size(); j++) {
//                                ProduceInfo produceInfo = produceInfos.get(j);
//                                // 最大页数
//                                if (maxPage == 0)
//                                    maxPage = produceInfo.getTotalPage();
//                                String url = "";
//
//                                // 商品详情连接
//                                String purl = produceInfo.getpUrl();
//                                // 商品对应得企业信息连接
//                                String pCurl = produceInfo.getpCUrl();
//
//                                // 查询商品对应得企业信息，由于商品详情+ 企业信息+ 分类都在一个页面，所以 company不仅有企业信息还有分类信息
//                                CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());
//
//                                // 看看是否有缓存
//                                if (companyInfo == null) {
//                                    //获取公司信息
//                                    companyInfo = baseService.wujinqiye(pCurl);
//                                    url = pCurl;
//                                    // 加入缓存
//                                    CompanyCache.add(companyInfo);
//                                }
//
//                                if (companyInfo != null) {
//                                    produceInfo.setCategory(category);
//
//                                    // 设置企业信息
//                                    baseDao.companyReplace(companyInfo);
//                                    produceInfo.setCompanyInfo(companyInfo);
//
//                                    // 存储产品信息
//                                    baseDao.produceReplace(produceInfo);
//
//                                    log.info("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + category.getC_name());
//                                } else {
//                                    log.error("ERROR： requestCompanyInfo 方法返回的 CompanyInfo对象是空 ， 查询URL = " + url + " ， 相关参数 =  " + new Gson().toJson(produceInfo));
//                                }
//
//                            }
//
//                        } else {
//                            log.info(category.getC_name() + " 下的商品数量为 0 , url =  " + category.getC_url());
//                        }
//
//                        if (maxPage <= 0)
//                            break;
//
//                        currentPage++;
//                    } else {
//                        log.error("requestProduces 返回的值是 null ,  category  =  " + new Gson().toJson(produceInfos) + " , currentPage =  " + currentPage);
//                    }
//                } while (currentPage <= maxPage);
//
//
//            }
//        } else {
//            log.error("requestCategory  返回的 categories 为空，或者 categories.size()  < 1");
//        }
//
//
//    }
//}
