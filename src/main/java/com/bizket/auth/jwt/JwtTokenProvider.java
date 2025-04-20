package com.bizket.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration-ms}")
    private Long validityInMilliseconds;

    private Key signingKey;

    @PostConstruct
    public void init() {
        // Base64 디코딩 후 키 초기화
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("secretKey: {}", secretKey);
        log.info("validityInMilliseconds: {}", validityInMilliseconds);
    }

    /**
     * 회원 ID를 기반으로 JWT 토큰을 생성
     *
     * @param memberId 인증된 회원 고유 ID
     * @return 생성된 JWT 토큰 문자열
     */
    public String createToken(String memberId) {
        Claims claims = Jwts.claims().setSubject(memberId).build();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiry)
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
        } catch (Exception e) {
            // 토큰 만료, 서명 불일치 등 예외 발생 시 false
            return false;
        }
    }

    /**
     * 토큰에서 회원 ID를 추출
     *
     * @param token JWT 토큰
     * @return 회원 ID 문자열
     */
    public String getMemberId(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }

    /**
     * 토큰 기반으로 Authentication 객체 생성
     *
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        String memberId = getMemberId(token);
        // 권한 정보가 없는 경우 빈 리스트로 처리
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
