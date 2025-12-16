package com.whd.cube.interceptor;

import com.whd.cube.common.UserContext;
import com.whd.cube.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ★★★ 核心修复点：如果是 OPTIONS 请求，直接放行 ★★★
        // 浏览器发起的预检请求不带 Token，必须放行，否则会报 CORS 错误
        if (RequestMethod.OPTIONS.name().equals(request.getMethod())) {
            return true;
        }

        // 1. 从请求头中获取 token
        String token = request.getHeader("token");

        // 2. 校验 token 是否存在
        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            return false;
        }

        // 3. 解析 token
        Claims claims = jwtUtils.getClaimsByToken(token);
        if (claims == null) {
            response.setStatus(401);
            return false;
        }

        // 4. 解析成功，获取 userId
        String userIdStr = claims.getSubject();

        // 5. 将 userId 存入 ThreadLocal
        UserContext.setUserId(Long.parseLong(userIdStr));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}