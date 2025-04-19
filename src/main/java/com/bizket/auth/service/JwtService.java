package com.bizket.auth.service;

import com.bizket.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtTokenProvider jwtTokenProvider;

    public String createToken(String memberId) {
        return jwtTokenProvider.createToken(memberId);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public String getMemberIdFromToken(String token) {
        return jwtTokenProvider.getMemberId(token);
    }

    public String resolveToken(HttpServletRequest request) {
        return jwtTokenProvider.resolveToken(request);
    }
}
