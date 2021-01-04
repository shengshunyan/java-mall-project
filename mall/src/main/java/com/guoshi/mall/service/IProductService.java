package com.guoshi.mall.service;

import com.github.pagehelper.PageInfo;
import com.guoshi.mall.vo.ProductDetailVo;
import com.guoshi.mall.vo.ResponseVo;

public interface IProductService {

    /**
     * 根据类目，获取商品列表
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize);

    /**
     * 根据id查询商品详情
     * @param productId
     * @return
     */
    ResponseVo<ProductDetailVo> detail(Integer productId);

}
