package com.guoshi.pay.service;

import com.guoshi.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface IPayService {

    /**
     * 创建/发起 支付
     * @param orderId
     * @param amount
     * @return
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 异步通知处理
     * @param notifyData
     */
    String asyncNotify(String notifyData);

    /**
     * 通过订单号查询订单信息
     * @param orderId
     * @return
     */
    PayInfo queryByOrderId(String orderId);

}
