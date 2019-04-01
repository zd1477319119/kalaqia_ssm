package com.kalaqia.mapper;

import com.kalaqia.pojo.Category;
import com.kalaqia.pojo.CategoryExample;
import java.util.List;

public interface CategoryMapper {
    /*delete*/
    int deleteByPrimaryKey(Integer id);
    /*add，增加分类*/
    int insert(Category record);

    int insertSelective(Category record);
    /*list查询*/
    List<Category> selectByExample(CategoryExample example);
    /*编辑分类*/
    Category selectByPrimaryKey(Integer id);
    /*只修改变化了的字段，未变化的字段就不修改了。*/
    int updateByPrimaryKeySelective(Category record);
    /*update，修改分类*/
    int updateByPrimaryKey(Category record);
}