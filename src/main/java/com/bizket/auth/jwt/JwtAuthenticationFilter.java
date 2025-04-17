package com.bizket.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {
        System.out.println("[JwtFilter] URI = " + request.getRequestURI());
        System.out.println("[JwtFilter] Authorization 헤더 = " + request.getHeader("Authorization"));

        String token = tokenProvider.resolveToken(request);

        System.out.println("[JwtFilter] Extracted token = " + token);

        try {
            if (token != null && tokenProvider.validateToken(token)) {
                Authentication auth = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("[JwtFilter] SecurityContext 인증 완료: " + auth);

            }
        } catch (Exception ex) {
            // 검증 중 문제가 발생하면 SecurityContext 초기화
            SecurityContextHolder.clearContext();
            System.out.println("[JwtFilter] Token validation error: " + ex.getMessage());

            // 예외를 상위로 전달하여 JwtExceptionHandler에서 처리
            throw ex;
        }
        filterChain.doFilter(request, response);
    }
}
