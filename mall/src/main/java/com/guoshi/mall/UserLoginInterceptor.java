package com.guoshi.mall;

import com.guoshi.mall.consts.MallConst;
import com.guoshi.mall.exception.UserLoginException;
import com.guoshi.mall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 判断用户登录 拦截器
 */
@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {

    /**
     * true 表示继续  false 表示中断
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle...");

        User user = (User) request.getSession().getAttribute(MallConst.CURRENT_USER);
        if (user == null) {
            log.info("user == null");
            // 用自定义异常捕获处理来返回未登录的json
            throw new UserLoginException();
        }

        return true;
    }
}
