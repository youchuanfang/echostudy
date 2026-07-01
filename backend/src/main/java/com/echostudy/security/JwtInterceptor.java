package com.echostudy.security;

import com.echostudy.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        try {
            LoginUser loginUser = jwtUtils.parseToken(authorization.substring(7));
            UserContext.set(loginUser);
            String uri = request.getRequestURI();
            if (uri.startsWith("/api/student") && !"STUDENT".equals(loginUser.getRole())) {
                throw new BusinessException(403, "学生接口仅允许学生访问");
            }
            if (uri.startsWith("/api/admin") && !"ADMIN".equals(loginUser.getRole())) {
                throw new BusinessException(403, "管理员接口仅允许管理员访问");
            }
            return true;
        } catch (Exception ex) {
            if (ex instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(401, "登录状态已失效");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
