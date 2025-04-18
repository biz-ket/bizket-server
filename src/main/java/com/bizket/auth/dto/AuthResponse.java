package com.bizket.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public record AuthResponse(
    String jwtToken,
    String tokenType,
    Long memberId,
    String nickname,
    String email
) {
    /* 필요 시, 추가 로직이나 별도 정적 팩토리를 정의할 수 있음. 예: */
    public static AuthResponse ofBearer(String token,
        Long memberId,
        String nickname,
        String email) {
        return new AuthResponse(token, "Bearer", memberId, nickname, email);
    }
}