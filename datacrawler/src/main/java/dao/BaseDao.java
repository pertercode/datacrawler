package dao;

import bean.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.List;

public class BaseDao {

    private static final Logger log = Logger.getLogger(BaseDao.class);

    //    ///// 分类

    /**
     * 根据类型名称检测类型是否存在
     *
     * @param cname
     * @return
     */
    public boolean categoryExists(String cname) {
        boolean exists = false;
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            Integer count = mapper.categoryCount(cname);
            exists = count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return exists;
    }

    /**
     * 插入类型
     */
    public void categoryInsert(Category category) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.categoryInsert(category);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    /**
     * 替换类型
     */
    public void categoryReplace(Category category) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.categoryReplace(category);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }


    /**
     * 替换类型
     */
    public void categoryReplaceList(List<Category> categories) {
        if (categories != null && categories.size() > 0) {

            SqlSession sqlSession = null;
            try {
                sqlSession = MyBatisUtils.openSession(false);
                BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
                mapper.categoryReplaceList(categories);
                sqlSession.commit();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
                sqlSession.rollback();
            } finally {
                MyBatisUtils.closeSession(sqlSession);
            }
        }
    }


    /**
     * 根据名称查询类型
     *
     * @param cname
     * @return
     */
    public Category categorySelect(String cname) {
        SqlSession sqlSession = null;
        Category category = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            category = mapper.categorySelect(cname);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return category;
    }


    public List<Category> categorySelectList(String platform, Integer is_low) {
        SqlSession sqlSession = null;
        List<Category> categoryList = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            categoryList = mapper.categorySelectList(platform, is_low);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return categoryList;
    }


    //
//
//    /// 产品
    public void produceReplace(ProduceInfo produceInfo) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.produceReplace(produceInfo);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    //// 企业
    public void companyExists() {
    }


    public void companyReplace(CompanyInfo companyInfo) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.companyReplace(companyInfo);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    // 查询最大页数 ( 根据平台 )
    public int produceMaxPage(String platform) {
        int result = 1;
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession();
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            result = mapper.produceMaxPage(platform);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return result;
    }

    // 查询最大页数 ( 根据平台 和 分类ID )
    public int produceMaxPage(String platform, String categoryId) {
        int result = 1;
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession();
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            result = mapper.produceMaxPageByCategory(platform, categoryId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
        return result;
    }


    // ////////////  规格型号
    public void typeNameReplace(TypeName typeName) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.typeNameReplace(typeName);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    public void typeNameDelete(String categoryId) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.typeNameDelete(categoryId);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    //型号值
    public void typeValueReplace(TypeValue typeValue) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.typeValueReplace(typeValue);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    public void typeValueDelete(String categoryId) {
        SqlSession sqlSession = null;
        try {
            sqlSession = MyBatisUtils.openSession(false);
            BaseMapper mapper = sqlSession.getMapper(BaseMapper.class);
            mapper.typeValueDelete(categoryId);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            sqlSession.rollback();
        } finally {
            MyBatisUtils.closeSession(sqlSession);
        }
    }
}
