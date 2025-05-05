package com.bizket.auth.jwt;

import com.bizket.exception.BizExceptionType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration-ms}")
    private Long validityInMilliseconds;

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshInMilliseconds;


    private Key signingKey;

    @PostConstruct
    public void init() {
        // Base64 디코딩 후 키 초기화
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 회원 ID를 기반으로 JWT 토큰을 생성
     *
     * @param memberId 인증된 회원 고유 ID
     * @return 생성된 JWT 토큰 문자열
     */
    public String createAccessToken(String memberId) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(memberId)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + validityInMilliseconds))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String createRefreshToken(String memberId) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(memberId)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + refreshInMilliseconds))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * HTTP Authorization 헤더에서 Bearer 토큰을 추출
     *
     * @param request HTTP 요청
     * @return 토큰 문자열 또는 null
     */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public long getRefreshExpirationMs() {
        return refreshInMilliseconds;
    }

    public long getAccessExpirationMs() {
        return validityInMilliseconds;
    }

    /**
     * 토큰 유효성 검사 (만료 및 서명 검증)
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
            return true;
        }  catch (ExpiredJwtException e) {
            throw BizExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of();
        } catch (JwtException | SignatureException e) {
            throw BizExceptionType.UNAUTHORIZED_INVALID_TOKEN.of();
        }
    }

    /**
     * 토큰에서 회원 ID를 추출
     *
     * @param token JWT 토큰
     * @return 회원 ID 문자열
     */
    public String getMemberId(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw BizExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of();
        } catch (JwtException | SignatureException e) {
            throw BizExceptionType.UNAUTHORIZED_INVALID_TOKEN.of();
        }
    }

    /**
     * 토큰 기반으로 Authentication 객체 생성
     *
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        String memberId = getMemberId(token);
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(memberId, token, List.of());
        return auth;
    }

    /**
     * 현재 SecurityContext에서 Authentication 가져오기
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
