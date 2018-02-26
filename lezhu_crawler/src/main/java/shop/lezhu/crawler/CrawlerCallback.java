package shop.lezhu.crawler;

import shop.lezhu.crawler.bean.CompanyInfoBean;
import shop.lezhu.crawler.services.CompanyInfoService;
import shop.lezhu.crawler.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class CrawlerCallback implements Callable<List<CompanyInfoBean>> {

    private String page;

    private String key;

    private String location;

    private CompanyInfoService service = null;

    public CrawlerCallback(String page, String key, String location) {
        this.page = page;
        this.key = key;
        this.location = location;
        service = new CompanyInfoService();
    }

    @Override
    public List<CompanyInfoBean> call() throws Exception {


        List<CompanyInfoBean> companyInfoBeanList = null;
        String[] ids = service.requestComponyIdsWithPage(key, location, page);

        System.out.println("page>> " + page + " , ids.length>>  " + ids.length);
        System.out.println("开始抓取企业详情 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");


        if (ids.length > 0) {
            long start = System.currentTimeMillis();

            companyInfoBeanList = new ArrayList<CompanyInfoBean>();
            for (int i = 1; i <= ids.length; i++) {
                CompanyInfoBean infoBean = service.requestComponyInfoWithId(ids[i - 1]);
                if (infoBean != null) {
                    infoBean.setNo(i);
                    infoBean.setKey(key);
                    infoBean.setLocation(location);
                    infoBean.setPage(Integer.parseInt(page));
                    companyInfoBeanList.add(infoBean);
                }
            }
            long end = System.currentTimeMillis();

            float miao = (float) ((end - start) / 1000);

            System.out.println("page>> " + page + " , 抓取数据: companyInfoBeanList.size>>  " + companyInfoBeanList.size() + "   耗时（秒）>>>> : " + miao);
        }

        return companyInfoBeanList;
    }


}
