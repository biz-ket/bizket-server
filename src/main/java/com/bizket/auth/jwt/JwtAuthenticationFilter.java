package com.bizket.auth.jwt;

import com.bizket.auth.config.SecurityConstant;
import com.bizket.common.dto.Response;
import com.bizket.exception.BizException;
import com.bizket.exception.BizExceptionType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        for (String pattern : SecurityConstant.WHITE_LIST) {
            if (new AntPathMatcher().match(pattern, request.getServletPath())) {
                return true;
            }
        }
        return false;
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
            Authentication auth = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            SecurityContextHolder.clearContext();
            throw BizExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of();
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            throw BizExceptionType.UNAUTHORIZED_INVALID_TOKEN.of();
        } catch (BizException ex) {
            sendError(response, ex.getBizExceptionType(), ex.getMessage());
        }
    }
    private void sendError(HttpServletResponse response, BizExceptionType type) throws IOException {
        sendError(response, type, type.getDefaultMessage());
    }

    private void sendError(HttpServletResponse response,
        BizExceptionType type,
        String message) throws IOException {
        SecurityContextHolder.clearContext();

        response.setStatus(type.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        Response<String> body = Response.of("", message);
        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
    }
}
