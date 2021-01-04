package com.guoshi.mall.service;

import com.guoshi.mall.pojo.User;
import com.guoshi.mall.vo.ResponseVo;

public interface IUserService {

    /**
     * 注册
     * @param user
     */
    ResponseVo<User> register(User user);

    /**
     * 登录
     * @param username
     * @param password
     */
    ResponseVo<User> login(String username, String password);

}
