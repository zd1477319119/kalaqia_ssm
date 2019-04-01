package com.kalaqia.service;

import com.kalaqia.pojo.Category;
import com.kalaqia.util.Page;

import java.util.List;

public interface CategoryService {
    /*显示分类*/
    List<Category> list();
    /*增加分类*/
    void add(Category category);
    /*删除分类*/
    void delete(int id);
    /*编辑分类*/
    Category get(int id);
    /*修改分类*/
    void update(Category category);
}
