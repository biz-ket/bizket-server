package com.bizket.auth.config;

public final class SecurityConstant {

    public static final String[] WHITE_LIST = {
        "/oauth2/**",
        "/favicon.ico",
        "/error",
        "/auth/instagram/login",
        "/auth/instagram/exchange",
        "/login/callback"
    };
}
