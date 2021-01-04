package com.guoshi.pay.service.impl;

import com.guoshi.pay.PayApplicationTests;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceTest extends PayApplicationTests {

    @Autowired
    private PayServiceImpl payService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void create() {
        payService.create("123456", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }

    @Test
    public void sendMQMsg() {
        amqpTemplate.convertAndSend("payNotify", "hello");
    }

}
