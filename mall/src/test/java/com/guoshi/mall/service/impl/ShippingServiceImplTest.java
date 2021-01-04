package com.guoshi.mall.service.impl;

import com.guoshi.mall.MallApplicationTests;
import com.guoshi.mall.enums.ResponseEnum;
import com.guoshi.mall.form.ShippingForm;
import com.guoshi.mall.service.IShippingService;
import com.guoshi.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Transactional // 测试结束后，回滚数据
public class ShippingServiceImplTest extends MallApplicationTests {

    @Autowired
    private IShippingService shippingService;

    private Integer uid = 1;

    private Integer shippingId;

    private ShippingForm shippingForm;

    @Before
    public void before() {
        ShippingForm shippingForm = new ShippingForm();
        shippingForm.setReceiverName("果实o");
        shippingForm.setReceiverAddress("小新村");
        shippingForm.setReceiverCity("深圳");
        shippingForm.setReceiverMobile("12312312312");
        shippingForm.setReceiverPhone("123123123");
        shippingForm.setReceiverProvince("广东");
        shippingForm.setReceiverDistrict("宝安区");
        shippingForm.setReceiverZip("000000");

        this.shippingForm = shippingForm;
        add();
    }

    public void add() {
        ResponseVo<Map<String, Integer>> responseVo = shippingService.add(uid, shippingForm);
        log.info("result = {}", responseVo);
        this.shippingId = responseVo.getData().get("shippingId");
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @After
    public void delete() {
        ResponseVo responseVo = shippingService.delete(uid, shippingId);
        log.info("result = {}", responseVo);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void update() {
        shippingForm.setReceiverName("果实1111o");
        ResponseVo responseVo = shippingService.update(uid, shippingId, shippingForm);
        log.info("result = {}", responseVo);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void list() {
        ResponseVo responseVo = shippingService.list(uid, 1, 10);
        log.info("result = {}", responseVo);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }
}
