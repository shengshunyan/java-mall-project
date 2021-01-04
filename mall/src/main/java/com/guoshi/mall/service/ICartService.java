package com.guoshi.mall.service;

import com.guoshi.mall.form.CartAddForm;
import com.guoshi.mall.form.CartUpdateForm;
import com.guoshi.mall.pojo.Cart;
import com.guoshi.mall.vo.CartVo;
import com.guoshi.mall.vo.ResponseVo;

import java.util.List;

public interface ICartService {

    ResponseVo<CartVo> add(Integer uid, CartAddForm cartAddForm);

    ResponseVo<CartVo> list (Integer uid);

    ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm cartUpdateForm);

    ResponseVo<CartVo> delete(Integer uid, Integer productId);

    ResponseVo<CartVo> selectAll(Integer uid);

    ResponseVo<CartVo> unSelectAll(Integer uid);

    ResponseVo<Integer> sum(Integer uid);

    List<Cart> listForCart(Integer uid);

}
