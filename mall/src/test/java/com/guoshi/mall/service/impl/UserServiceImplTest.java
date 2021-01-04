package com.guoshi.mall.service.impl;

import com.guoshi.mall.MallApplicationTests;
import com.guoshi.mall.enums.ResponseEnum;
import com.guoshi.mall.enums.RoleEnum;
import com.guoshi.mall.pojo.User;
import com.guoshi.mall.service.IUserService;
import com.guoshi.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional // 测试结束后，回滚数据
public class UserServiceImplTest extends MallApplicationTests {

    public static final String USERNAME = "jack";

    public static final String PASSWORD = "123466";

    @Autowired
    private IUserService userService;

    @Before
    public void register() {
        User user = new User(USERNAME, PASSWORD, "jack@qq.com", RoleEnum.CUSTOMER.getCode());
        userService.register(user);
    }

    @Test
    public void login() {
        ResponseVo<User> responseVo = userService.login(USERNAME, PASSWORD);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }
}
