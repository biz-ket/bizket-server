package com.bizket.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 성공 시 클라이언트에 반환되는 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    // jwt token
    private final String jwtToken;

    private final String tokenType;

    private final Long memberId;

    private final String nickname;

    private final String email;
}
