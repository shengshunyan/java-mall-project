package com.guoshi.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.guoshi.mall.dao.ShippingMapper;
import com.guoshi.mall.enums.ResponseEnum;
import com.guoshi.mall.form.ShippingForm;
import com.guoshi.mall.pojo.Shipping;
import com.guoshi.mall.service.IShippingService;
import com.guoshi.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ResponseVo<Map<String, Integer>> add(Integer uid, ShippingForm shippingForm) {
        Shipping shipping = new Shipping();
        BeanUtils.copyProperties(shippingForm, shipping);
        shipping.setUserId(uid);
        int row = shippingMapper.insertSelective(shipping);

        if (row == 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        HashMap<String, Integer> map = new HashMap<>();
        map.put("shippingId", shipping.getId());

        return ResponseVo.success(map);
    }

    @Override
    public ResponseVo delete(Integer uid, Integer shippingId) {
        int row = shippingMapper.deleteByIdAndUid(uid, shippingId);

        if (row == 0) {
            return ResponseVo.error(ResponseEnum.DELETE_SHIPPING_FAIL);
        }

        return ResponseVo.successByMsg("删除地址成功");
    }

    @Override
    public ResponseVo update(Integer uid, Integer shippingId, ShippingForm shippingForm) {
        Shipping shipping = new Shipping();
        BeanUtils.copyProperties(shippingForm, shipping);
        shipping.setUserId(uid);
        shipping.setId(shippingId);

        int row = shippingMapper.updateByPrimaryKeySelective(shipping);

        if (row == 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        return ResponseVo.successByMsg("更新地址成功");
    }

    @Override
    public ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUid(uid);
        PageInfo pageInfo = new PageInfo(shippingList);

        return ResponseVo.success(pageInfo);
    }
}
