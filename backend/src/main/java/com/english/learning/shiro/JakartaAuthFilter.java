package com.english.learning.shiro;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import java.io.PrintWriter;

/**
 * 自定义 Jakarta 认证过滤器，对于未认证的请求返回 401 错误码而非重置到登录页
 */
public class JakartaAuthFilter extends FormAuthenticationFilter {

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        try (PrintWriter out = httpServletResponse.getWriter()) {
            out.println("{\"code\":401,\"message\":\"未登录或登录过期\"}");
            out.flush();
        }
        return false;
    }
}
