package com.scriptkill.config;

import com.scriptkill.common.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 登录和注册接口放行
        String path = request.getRequestURI();
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                // 将用户信息存入request
                request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
                request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
                request.setAttribute("role", jwtUtil.getRoleFromToken(token));
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
