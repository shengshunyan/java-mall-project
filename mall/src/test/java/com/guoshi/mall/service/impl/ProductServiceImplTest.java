package com.guoshi.mall.service.impl;

import com.github.pagehelper.PageInfo;
import com.guoshi.mall.MallApplicationTests;
import com.guoshi.mall.enums.ResponseEnum;
import com.guoshi.mall.service.IProductService;
import com.guoshi.mall.vo.ProductDetailVo;
import com.guoshi.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ProductServiceImplTest extends MallApplicationTests {

    @Autowired
    private IProductService productService;

    @Test
    public void list() {
        ResponseVo<PageInfo> responseVo = productService.list(null, 1, 10);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void detail() {
        ResponseVo<ProductDetailVo> responseVo = productService.detail(26);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

}
