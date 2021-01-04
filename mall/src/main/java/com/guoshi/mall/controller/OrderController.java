package com.guoshi.mall.controller;

import com.github.pagehelper.PageInfo;
import com.guoshi.mall.consts.MallConst;
import com.guoshi.mall.form.OrderCreateForm;
import com.guoshi.mall.pojo.User;
import com.guoshi.mall.service.IOrderService;
import com.guoshi.mall.vo.OrderVo;
import com.guoshi.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @PostMapping("/orders")
    public ResponseVo<OrderVo> create(@Valid @RequestBody OrderCreateForm form, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return orderService.create(user.getId(), form.getShippingId());
    }

    @GetMapping("/orders")
    public ResponseVo<PageInfo> list(@RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(required = false, defaultValue = "10") Integer pageSze,
                                     HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return orderService.list(user.getId(), pageNum, pageSze);
    }

    @GetMapping("/orders/{orderNo}")
    public ResponseVo<OrderVo> detai(@PathVariable Long orderNo, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return orderService.detail(user.getId(), orderNo);
    }

    @PutMapping("/orders/{orderNo}")
    public ResponseVo cancel(@PathVariable Long orderNo, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return orderService.cancel(user.getId(), orderNo);
    }

}
