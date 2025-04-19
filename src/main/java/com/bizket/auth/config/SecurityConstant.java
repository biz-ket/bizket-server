package com.bizket.auth.config;

public final class SecurityConstant {
    public static final String[] WHITE_LIST = {
        "/api/oauth2/authorization/**",
        "/api/login/oauth2/code/**",
        "/api/auth/reissue",
        "/api/auth/**",
        "/oauth2/**",
        "/favicon.ico",
        "/error",
        "/auth/instagram/login",
        "/auth/instagram/exchange"
    };
}
