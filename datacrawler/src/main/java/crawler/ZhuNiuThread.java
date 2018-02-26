package crawler;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import com.google.gson.Gson;
import services.ZNBaseService;

import java.util.List;

/**
 * 筑牛网抓取程序
 */
public class ZhuNiuThread implements Runnable {

    private ZNBaseService baseService = new ZNBaseService();

    @Override
    public void run() {

        Integer maxPage = 0;

        int currentPage = 1;

        // 从数据库里查询最大页数 - 5 为当前页

        do {
            List<ProduceInfo> produceInfos = baseService.requestAllProduce(currentPage);

            if (produceInfos != null && produceInfos.size() > 0) {

                for (int i = 0; i < produceInfos.size(); i++) {

                    ProduceInfo produceInfo = produceInfos.get(i);

                    // 最大页数
                    if (maxPage == 0)
                        maxPage = produceInfo.getTotalPage();

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
                        Category category = baseService.requestCategory(companyInfo.category);

                        if (category == null) {
                            System.err.println("类别 Category is NULL !" + " , " + new Gson().toJson(companyInfo));
                        }
                        produceInfo.setCategory(category);


                        // 设置企业信息
                        baseService.companyReplace(companyInfo);
                        produceInfo.setCompanyInfo(companyInfo);

                        // 存储产品信息
                        baseService.produceReplace(produceInfo);

                        System.out.println("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + produceInfo.getCompanyInfo().category);


                    } else {
                        System.err.println("ERROR:  url =  " + url + " ,  requestCompanyInfo 返回得 companyInfo 对象 是 NULL!");
                        i--;
                    }

                    // 休眠
                    try {
                        Thread.sleep(1100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    client.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port)));

                }

                currentPage++;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } while (currentPage <= maxPage);
    }
}
