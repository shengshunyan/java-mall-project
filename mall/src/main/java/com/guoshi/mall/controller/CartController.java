package com.guoshi.mall.controller;

import com.guoshi.mall.consts.MallConst;
import com.guoshi.mall.form.CartAddForm;
import com.guoshi.mall.form.CartUpdateForm;
import com.guoshi.mall.pojo.User;
import com.guoshi.mall.service.ICartService;
import com.guoshi.mall.vo.CartVo;
import com.guoshi.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/carts")
    public ResponseVo<CartVo> list(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return cartService.list(uid);
    }

    @PostMapping("/carts")
    public ResponseVo<CartVo> add(@Valid @RequestBody CartAddForm cartAddForm, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return cartService.add(uid, cartAddForm);
    }

    @PutMapping("/carts/{productId}")
    public ResponseVo<CartVo> update(
            @PathVariable Integer productId,
            @Valid @RequestBody CartUpdateForm cartUpdateForm,
            HttpSession session
    ) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return cartService.update(uid, productId, cartUpdateForm);
    }

    @DeleteMapping("/carts/{productId}")
    public ResponseVo<CartVo> delete(@PathVariable Integer productId, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return cartService.delete(uid, productId);
    }

    @PutMapping("/carts/selectAll")
    public ResponseVo<CartVo> selectAll(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return cartService.selectAll(uid);
    }

    @PutMapping("/carts/unSelectAll")
    public ResponseVo<CartVo> unSelectAll(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return cartService.unSelectAll(uid);
    }

    @GetMapping("/carts/products/sum")
    public ResponseVo<Integer> sum(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return cartService.sum(uid);
    }

}
