package cache;

import bean.CompanyInfo;

import java.util.HashMap;

public class CompanyCache {

    private static final int max = 5000;

    private static HashMap<String, CompanyInfo> cache = new HashMap<String, CompanyInfo>();


    public static CompanyInfo get(String cname) {
        if (cache.size() > max) cache.clear();
        return cache.get(cname.trim());
    }

    public static void add(CompanyInfo companyInfo) {
        if (companyInfo != null) {
            cache.put(companyInfo.getcName(), companyInfo);
        }
    }
}
