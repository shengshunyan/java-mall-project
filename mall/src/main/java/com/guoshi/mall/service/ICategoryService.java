package com.guoshi.mall.service;

import com.guoshi.mall.vo.CategoryVo;
import com.guoshi.mall.vo.ResponseVo;

import java.util.List;
import java.util.Set;

public interface ICategoryService {

    /**
     * 查询所有类目
     * @return
     */
    ResponseVo<List<CategoryVo>> selectAll();

    /**
     * 查找子类目id
     * @param id
     * @param resultSet
     */
    void findSubCategoryId(Integer id, Set<Integer> resultSet);

}
