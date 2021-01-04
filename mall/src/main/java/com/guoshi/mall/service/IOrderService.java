package com.guoshi.mall.service;

import com.github.pagehelper.PageInfo;
import com.guoshi.mall.vo.OrderVo;
import com.guoshi.mall.vo.ResponseVo;

public interface IOrderService {

    ResponseVo<OrderVo> create(Integer uid, Integer shippingId);

    ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize);

    ResponseVo<OrderVo> detail(Integer uid, Long orderNo);

    /**
     * 取消订单
     *
     * @param uid
     * @param orderNo
     * @return
     */
    ResponseVo cancel(Integer uid, Long orderNo);


    /**
     * 修改订单状态为已支付
     * @param orderNo
     */
    void paid(Long orderNo);

}
