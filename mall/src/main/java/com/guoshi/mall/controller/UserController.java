package com.guoshi.mall.controller;

import com.guoshi.mall.consts.MallConst;
import com.guoshi.mall.form.UserLoginForm;
import com.guoshi.mall.form.UserRegisterForm;
import com.guoshi.mall.pojo.User;
import com.guoshi.mall.service.IUserService;
import com.guoshi.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/user/register")
    public ResponseVo<User> register(@Valid @RequestBody UserRegisterForm userForm) {
        User user = new User();
        BeanUtils.copyProperties(userForm, user);

        return userService.register(user);
    }

    @PostMapping("/user/login")
    public ResponseVo<User> login(@Valid @RequestBody UserLoginForm userForm, HttpSession httpSession) {
        ResponseVo<User> userResponseVo = userService.login(userForm.getUsername(), userForm.getPassword());

        // 设置session
        httpSession.setAttribute(MallConst.CURRENT_USER, userResponseVo.getData());

        return userResponseVo;
    }

    @GetMapping("/user")
    public ResponseVo<User> userInfo(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);

        return ResponseVo.success(user);
    }

    @PostMapping("/user/logout")
    public ResponseVo logout(HttpSession session) {
        log.info("/user/logout sessionId={}", session.getId());
        session.removeAttribute(MallConst.CURRENT_USER);

        return ResponseVo.success();
    }

}
