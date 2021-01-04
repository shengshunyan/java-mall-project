package com.guoshi.mall.controller;

import com.github.pagehelper.PageInfo;
import com.guoshi.mall.consts.MallConst;
import com.guoshi.mall.form.ShippingForm;
import com.guoshi.mall.pojo.User;
import com.guoshi.mall.service.IShippingService;
import com.guoshi.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
public class ShippingController {

    @Autowired
    private IShippingService shippingService;

    @PostMapping("/shippings")
    public ResponseVo add(@Valid @RequestBody ShippingForm shippingForm, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return shippingService.add(uid, shippingForm);
    }

    @DeleteMapping("/shippings/{shippingId}")
    public ResponseVo delete(@PathVariable Integer shippingId, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return shippingService.delete(uid, shippingId);
    }

    @PutMapping("/shippings/{shippingId}")
    public ResponseVo update(@PathVariable Integer shippingId, @Valid @RequestBody ShippingForm shippingForm, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return shippingService.update(uid, shippingId, shippingForm);
    }

    @GetMapping("/shippings")
    public ResponseVo<PageInfo> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpSession session
    ) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();

        return shippingService.list(uid, pageNum, pageSize);
    }

}
