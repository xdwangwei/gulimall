package com.vivi.gulimall.order.interceptor;

import com.vivi.common.constant.AuthServerConstant;
import com.vivi.common.vo.MemberInfoVO;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
        String uri = request.getRequestURI();
        // 有些远程调用不需要登录，直接放行
        boolean match = new AntPathMatcher().match("/api/**", uri);
        // 支付宝异步通知不需要拦截
        boolean match1 = new AntPathMatcher().match("/alipay/notify", uri);
        if (match || match1) {
            return true;
        }
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
