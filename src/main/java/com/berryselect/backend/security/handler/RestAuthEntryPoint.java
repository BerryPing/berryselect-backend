package com.berryselect.backend.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 401 : 미인증 ( 로그인 안 했거나 토큰 없음/깨짐)
@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"Unauthorized\"}");
    }
}
