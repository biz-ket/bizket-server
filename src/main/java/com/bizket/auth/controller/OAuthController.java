package com.bizket.auth.controller;

import com.bizket.auth.dto.AuthResponse;
import com.bizket.auth.dto.InstagramCodeRequest;
import com.bizket.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import com.bizket.auth.dto.RefreshRequest;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OAuthController {

    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.instagram.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.instagram.prod-req-origin}")
    private String prodReqOrigin;

    @Value("${spring.security.oauth2.client.registration.instagram.prod-redirect-uri}")
    private String prodRedirectUri;

    @Value("${spring.security.oauth2.client.registration.instagram.dev-redirect-uri}")
    private String devRedirectUri;

    @GetMapping("/auth/instagram/login")
    public void login(
        @RequestParam(value = "redirect_uri", required = false) String redirectUriParam,
        HttpServletResponse response
    ) throws IOException {
        // CSRF 방어용 state 생성
        String state = UUID.randomUUID().toString();

        // 전달받은 redirect_uri가 있을 경우 우선 사용
        String callbackUri;
        if (redirectUriParam != null && !redirectUriParam.isBlank()) {
            // host 뒤에 '/login/callback' 추가
            String base = redirectUriParam.endsWith("/")
                ? redirectUriParam.substring(0, redirectUriParam.length() - 1)
                : redirectUriParam;
            callbackUri = base + "/login/callback";
        } else {
            // 기존 prod/dev 로직
            String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

            callbackUri = baseUrl.startsWith(prodReqOrigin)
                ? prodRedirectUri
                : devRedirectUri;
        }

        log.info("[★★★★★ /auth/instagram/login] Redirect URI for login: {}", callbackUri);


        String authorizeUrl = UriComponentsBuilder
            .fromHttpUrl("https://www.instagram.com/oauth/authorize")
            .queryParam("enable_fb_login", 0)
            .queryParam("force_authentication", 1)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", callbackUri)
            .queryParam("response_type", "code")
            .queryParam("scope",
                "instagram_business_basic," +
                    "instagram_business_manage_messages," +
                    "instagram_business_manage_comments," +
                    "instagram_business_content_publish," +
                    "instagram_business_manage_insights"
            )
            .queryParam("state", state)
            .build()
            .toUriString();
        response.sendRedirect(authorizeUrl);
    }

    @PostMapping("/auth/instagram/exchange")
    public AuthResponse exchangeCode(
        @RequestBody InstagramCodeRequest request,
        @RequestParam(value = "redirect_uri", required = false) String redirectUriParam
    ) {
        String code = request.getCode();

        String callbackUri;
        if (redirectUriParam != null && !redirectUriParam.isBlank()) {
            String base = redirectUriParam.endsWith("/")
                ? redirectUriParam.substring(0, redirectUriParam.length() - 1)
                : redirectUriParam;
            callbackUri = base + "/login/callback";
        } else {
            String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();
            callbackUri = baseUrl.startsWith(prodReqOrigin)
                ? prodRedirectUri
                : devRedirectUri;
        }

        log.info("[★★★★★ /auth/instagram/exchange] Redirect URI for login: {}", callbackUri);

        return authService.loginWithInstagramCode(code, callbackUri);
    }

    @PostMapping("/auth/instagram/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refreshTokens(request.refreshToken());
    }
}
