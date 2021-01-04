package com.guoshi.pay.service.impl;

import com.google.gson.Gson;
import com.guoshi.pay.dao.PayInfoMapper;
import com.guoshi.pay.enums.PayPlatformEnum;
import com.guoshi.pay.pojo.PayInfo;
import com.guoshi.pay.service.IPayService;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PayServiceImpl implements IPayService {

    private final static String QUEUE_PAY_NOTIFY = "payNotify";

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {
        // 写入订单数据
        PayInfo payInfo = new PayInfo(
                Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        // 发起支付
        PayRequest payRequest = new PayRequest();
        payRequest.setOrderName("4964658-最好的支付sdk");
        payRequest.setOrderId(orderId);
        payRequest.setOrderAmount(amount.doubleValue());
        payRequest.setPayTypeEnum(bestPayTypeEnum);

        PayResponse payResponse = bestPayService.pay(payRequest);
        log.info("发起支付 response = {}", payResponse);

        return payResponse;
    }

    @Override
    public String asyncNotify(String notifyData) {
        // 1. 签名验证
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("异步通知 response = {}", payResponse);

        // 2. 金额校验（从数据库查订单）
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if (payInfo == null) {
            throw new RuntimeException("通过orderNo查询到的结果是null");
        }
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0) {
                throw new RuntimeException("异步通知中的金额和数据库里的不一致, orderNo = " + payResponse.getOrderId());
            }

            // 3. 修改订单支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());

            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        // 发送MQ消息
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY, new Gson().toJson(payInfo));

        // 4.1 告诉微信不要再通知了
        if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";

        } else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            // 4.2 告诉支付宝不要再通知了
            return "success";
        }

        throw new RuntimeException("异步通知中，错误的支付平台");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
