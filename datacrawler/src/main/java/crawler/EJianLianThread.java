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
import utils.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        log.e("抓取完成.. ", null);
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

                        String companyUrl = produceInfo.getpCUrl();

                        String cid = "";
                        String regexp = "shop(\\d+)";
                        Pattern pattern = Pattern.compile(regexp);
                        Matcher matcher = pattern.matcher(companyUrl);
                        if (matcher.find()) {
                            cid = matcher.group(1);
                        }

                        // 查询商品对应得企业信息，由于商品详情+ 企业信息+ 分类都在一个页面，所以 company不仅有企业信息还有分类信息
                        CompanyInfo companyInfo = CompanyCache.get(produceInfo.getcName());

                        // 看看是否有缓存
                        if (companyInfo == null) {
                            // 首先从联系我们中查询
                            companyInfo = baseService.requestCompanyWithCId(cid);
                            CompanyCache.add(companyInfo);
                        }

                        if (companyInfo != null) {
                            if (StringUtils.isEmpty(companyInfo.getcName())) {
                                companyInfo.setcConcat(produceInfo.getcName());
                            }
                            baseDao.companyReplace(companyInfo);

                            produceInfo.setCategory(category);
                            produceInfo.setCompanyInfo(companyInfo);
                            // 存储产品信息
                            baseDao.produceReplace(produceInfo);

                            log.i("[page = " + produceInfo.getPage() + "] 产品入库 ： " + produceInfo.getpName() + "  ,  企业入库： " + produceInfo.getCompanyInfo().getcName() + " ,  类型入库 ： " + category.getC_name());
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
