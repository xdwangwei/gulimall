package com.vivi.gulimall.cart.interceptor;

import com.vivi.common.constant.AuthServerConstant;
import com.vivi.common.constant.CartConstant;
import com.vivi.common.vo.MemberInfoVO;
import com.vivi.gulimall.cart.vo.UserLoginStatusTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author wangwei
 * 2021/1/16 12:34
 *
 * 拦截器，访问目标方法之前，先判断用户的登录状态并保存进threadlocal
 *
 * 每个请求过来，tomcat都会为其分配一个线程，走完整个业务流程，借助threadLocal共享数据
 *
 * 在MVCConfig中addInterceptors
 */
public class CartInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserLoginStatusTO> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行前，判断用户登录状态
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserLoginStatusTO loginStatusTO = new UserLoginStatusTO();
        // 判断该用户是否登录过
        MemberInfoVO loginUser = (MemberInfoVO) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER_KEY);
        // 已登录
        if (loginUser != null) {
            // 记录其登录状态，id标识
            loginStatusTO.setId(loginUser.getId());
        }
        // 未登录也不用管，因为临时用户也可以
        // 判断请求cookie中是否存在user-key，也就是说该用户是否已拿到过user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.COOKIE_TEMP_USER_KEY)) {
                    // 记录其user-key
                    loginStatusTO.setUserKey(cookie.getValue());
                }
            }
        }

        // 如果该用户既没有登录，也没有user-key，一定要为其分配，保证业务能正常处理
        // TODO 然后返回的时候再告诉浏览器把这个key保存进cookie，下次访问句会携带 postHandle()
        if (StringUtils.isEmpty(loginStatusTO.getUserKey())) {
            // 分配user-key
            String key = UUID.randomUUID().toString().replace("-", "");
            loginStatusTO.setUserKey(key);
            // 标记他是第一次访问
            loginStatusTO.setFirstVisit(true);
        }

        // 将用户登录状态存入threadlocal
        threadLocal.set(loginStatusTO);
        return true;
    }

    /**
     * 业务结束，如果该用户是【第一次访问】，系统为其生成了临时key作为它的身份标识，之后的操作都是基于这个临时key完成的
     * 所以这种情况下，返回时一定要告诉浏览器这个key，并让他保存进cookie，之后再次访问就会自动携带
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserLoginStatusTO userLoginStatusTO = threadLocal.get();
        // 的确是第一次访问的用户，系统为其分配了user-key，告知浏览器保存
        if (userLoginStatusTO.isFirstVisit()) {
            // 系统为其分配的user-key
            String key = userLoginStatusTO.getUserKey();
            // 命令浏览器保存,下次访问会自动携带
            Cookie cookie = new Cookie(CartConstant.COOKIE_TEMP_USER_KEY, key);
            // 设置有效期
            cookie.setMaxAge(CartConstant.COOKIE_TEMP_USER_KEY_TIMEOUT);
            // 设置作用域范围，该键值仅用于购物车服务，所以默认就是当前域，不用设置
            // cookie.setDomain();
            // 命令浏览器保存
            response.addCookie(cookie);
        }
    }
}
