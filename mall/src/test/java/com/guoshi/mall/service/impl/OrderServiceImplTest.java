package com.guoshi.mall.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guoshi.mall.MallApplicationTests;
import com.guoshi.mall.enums.ResponseEnum;
import com.guoshi.mall.form.CartAddForm;
import com.guoshi.mall.service.ICartService;
import com.guoshi.mall.service.IOrderService;
import com.guoshi.mall.vo.CartVo;
import com.guoshi.mall.vo.OrderVo;
import com.guoshi.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class OrderServiceImplTest extends MallApplicationTests {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ICartService cartService;

    private Integer uid = 3;

    private Integer shippingId = 6;

    private Integer productId = 26;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Before
    public void before() {
        CartAddForm cartAddForm = new CartAddForm();
        cartAddForm.setProductId(productId);
        cartAddForm.setSelected(true);

        ResponseVo<CartVo> responseVo = cartService.add(uid, cartAddForm);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void create() {
        ResponseVo<OrderVo> responseVo = orderService.create(uid, shippingId);
        log.info("responseVo={}", gson.toJson(responseVo));
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void list() {
        ResponseVo<PageInfo> responseVo = orderService.list(uid, 1, 10);
        log.info("responseVo={}", gson.toJson(responseVo));
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    public ResponseVo<OrderVo> createTest() {
        ResponseVo<OrderVo> responseVo = orderService.create(uid, shippingId);
        return responseVo;
    }

    @Test
    public void detail() {
        ResponseVo<OrderVo> vo = createTest();
        ResponseVo<OrderVo> responseVo = orderService.detail(uid, vo.getData().getOrderNo());
        log.info("responseVo={}", gson.toJson(responseVo));
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void cancel() {
        ResponseVo<OrderVo> vo = createTest();
        ResponseVo responseVo = orderService.cancel(uid, vo.getData().getOrderNo());
        log.info("responseVo={}", gson.toJson(responseVo));
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }
}
