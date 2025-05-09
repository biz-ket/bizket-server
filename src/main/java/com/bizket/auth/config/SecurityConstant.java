package com.bizket.auth.config;

public final class SecurityConstant {

    public static final String[] WHITE_LIST = {
        "/oauth2/**",
        "/favicon.ico",
        "/error",
        "/auth/instagram/**",
        "/login/callback",
        "/trends/**",
        "/datalab/trends/**",
        "/marketing/contents",
        "/marketing/contents/*",
        "/docs/index.html",
        "/business/**",
        "/instagram/insight/me/profile"
    };
}
