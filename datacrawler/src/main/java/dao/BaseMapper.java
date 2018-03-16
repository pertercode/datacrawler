package dao;

import bean.*;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface BaseMapper {

    ///// 分类
    int categoryCount(@Param("cname") String cname);

    void categoryInsert(@Param("category") Category category);

    void categoryReplace(@Param("category") Category category);

    void categoryReplaceList(List<Category> categories);

    Category categorySelect(@Param("cname") String cname);

    ////// 企业
    //int companyExists() ;

    void companyReplace(@Param("companyInfo") CompanyInfo companyInfo);

    //    /// 产品
    void produceReplace(@Param("produceInfo") ProduceInfo produceInfo);


    int produceMaxPage(@Param("platform") String platform);

    int produceMaxPageByCategory(@Param("platform") String platform, @Param("categoryId") String categoryId);

    //   ////  规格型号
    void typeNameReplace(@Param("typeName") TypeName typeName);

    // 根据类型ID删除规格型号值
    void typeNameDelete(@Param("categoryId") String categoryId);


    void typeValueReplace(@Param("typeValue") TypeValue typeValue);

    // 根据规格型号 删除规格型号值
    void typeValueDelete(@Param("categoryId") String categoryId);


}
