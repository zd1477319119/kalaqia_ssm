package com.kalaqia.interceptor;

import com.kalaqia.pojo.User;
import com.kalaqia.service.CategoryService;
import com.kalaqia.service.OrderItemService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    CategoryService categoryService;
    @Autowired
    OrderItemService orderItemService;

    /**
     * 在业务处理器处理请求之前被调用
     * 如果返回false
     * 从当前的拦截器往回执行所有拦截器的afterCompletion(),再退出拦截器链
     * 如果返回true
     * 执行下一个拦截器,直到所有的拦截器都执行完毕
     * 再执行被拦截的Controller
     * 然后进入拦截器链,
     * 从最后一个拦截器往回执行所有的postHandle()
     * 接着再从最后一个拦截器往回执行所有的afterCompletion()
     */
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        String contextPath = session.getServletContext().getContextPath();
        /*准备字符串数组 noNeedAuthPage，存放哪些不需要登录也能访问的路径*/
        String[] noNeedAuthPage = new String[]{
                "home",
                "checkLogin",
                "register",
                "loginAjax",
                "login",
                "product",
                "category",
                "search"};

        /*获取uri*/
        String uri = request.getRequestURI();
        /*去掉前缀/kalaqia_ssm*/
        uri = StringUtils.remove(uri, contextPath);
//        System.out.println(uri);
        /*如果访问的地址是/fore开头*/
        if (uri.startsWith("/fore")) {
            /*取出fore后面的字符串，比如是forecart,那么就取出cart*/
            String method = StringUtils.substringAfterLast(uri, "/fore");
            /*判断cart是否是在noNeedAuthPage */
            /*如果不在，那么就需要进行是否登录验证*/
            if (!Arrays.asList(noNeedAuthPage).contains(method)) {
                /*从session中取出"user"对象*/
                User user = (User) session.getAttribute("user");
                if (null == user) {
                    /*如果对象不存在，就客户端跳转到login.jsp*/
                    response.sendRedirect("loginPage");
                    return false;
                }
            }
        }

        /*否则就正常执行*/
        return true;

    }

    /**
     * 在业务处理器处理请求执行完成后,生成视图之前执行的动作
     * 可在modelAndView中加入数据，比如当前时间
     */

    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    /**
     * 在DispatcherServlet完全处理完请求后被调用,可用于清理资源等
     * <p>
     * 当有拦截器抛出异常时,会从当前拦截器往回执行所有的拦截器的afterCompletion()
     */

    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }

}