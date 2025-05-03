package com.bizket.auth.dto;

public record AuthResponse(
    String jwtToken,
    String tokenType,
    Long memberId,
    String nickname
) {

    public static AuthResponse ofBearer(String token,
        Long memberId,
        String nickname) {
        return new AuthResponse(token, "Bearer", memberId, nickname);
    }
}
