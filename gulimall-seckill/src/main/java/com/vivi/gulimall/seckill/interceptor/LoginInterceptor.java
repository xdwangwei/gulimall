package com.vivi.gulimall.seckill.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import com.vivi.common.constant.AuthServerConstant;
import com.vivi.common.vo.MemberInfoVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author wangwei
 * 2021/1/18 16:10
 *
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberInfoVO> threadLocal = new ThreadLocal<>();

    /**
     * 目标资源访问前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberInfoVO attribute = (MemberInfoVO) session.getAttribute(AuthServerConstant.LOGIN_USER_KEY);
        if (attribute == null) {
            // 用户未登录
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        } else {
            // 已登录，用threadLocal共享数据
            threadLocal.set(attribute);
            return true;
        }
    }
}
