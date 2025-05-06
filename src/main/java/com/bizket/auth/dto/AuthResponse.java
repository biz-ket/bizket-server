package com.bizket.auth.dto;

public record AuthResponse(
    String jwtToken,
    String tokenType,
    Long memberId,
    String nickname,
    String refreshToken
) {

    public static AuthResponse ofBearer(
        String accessToken,
        Long memberId,
        String nickname,
        String refreshToken
    ) {
        return new AuthResponse(
            accessToken,
            "Bearer",
            memberId,
            nickname,
            refreshToken
        );
    }
}
