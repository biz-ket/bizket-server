package com.bizket.auth.jwt;

import com.bizket.exception.BizException;
import com.bizket.exception.BizExceptionType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
        String token = tokenProvider.resolveToken(request);

        try {
            if (token == null) {
                throw BizExceptionType.UNAUTHORIZED_TOKEN_MISSING.of();
            }
            if (!tokenProvider.validateToken(token)) {
                throw BizExceptionType.UNAUTHORIZED_INVALID_TOKEN.of();
            }
        } catch (ExpiredJwtException ex) {
            SecurityContextHolder.clearContext();
            throw BizExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of();
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            throw BizExceptionType.UNAUTHORIZED_INVALID_TOKEN.of();
        } catch (BizException ex) {
            SecurityContextHolder.clearContext();
            throw ex;
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            throw BizExceptionType.SERVER_ERROR.of("토큰 처리 중 오류가 발생했습니다.");
        }

        filterChain.doFilter(request, response);
    }
}
