package dao;

import bean.Category;
import bean.CompanyInfo;
import bean.ProduceInfo;
import org.apache.ibatis.annotations.Param;

public interface BaseMapper {

    ///// 分类
    int categoryCount(@Param("cname") String cname);

    void categoryInsert(@Param("category") Category category);

    Category categorySelect(@Param("cname") String cname);

    ////// 企业
    //int companyExists() ;

    void companyReplace(@Param("companyInfo") CompanyInfo companyInfo);

    //    /// 产品
    void produceReplace(@Param("produceInfo") ProduceInfo produceInfo);

    int produceMaxPage(@Param("platform") String platform);
}
