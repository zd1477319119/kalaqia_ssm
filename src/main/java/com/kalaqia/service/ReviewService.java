package com.kalaqia.service;

import com.kalaqia.pojo.Review;

import java.util.List;

public interface ReviewService {

    void add(Review c);

    void delete(int id);
    void update(Review c);
    Review get(int id);
    List list(int pid);
    /*通过产品获取评价方法*/
    int getCount(int pid);
}