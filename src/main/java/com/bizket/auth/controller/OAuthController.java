package com.bizket.auth.controller;

import com.bizket.auth.dto.AuthResponse;
import com.bizket.auth.dto.InstagramCodeRequest;
import com.bizket.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class OAuthController {
    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.instagram.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.instagram.redirect-uri}")
    private String redirectUri;

    @GetMapping("/api/auth/instagram/login")
    public void login(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();  // CSRF 방어용
        String authorizeUrl = UriComponentsBuilder
            .fromHttpUrl("https://www.instagram.com/oauth/authorize")
            .queryParam("enable_fb_login", 0)
            .queryParam("force_authentication", 1)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
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

    @PostMapping("/api/auth/instagram/exchange")
    public AuthResponse exchangeCode(@RequestBody InstagramCodeRequest request) {
        return authService.loginWithInstagramCode(request.getCode());
    }

//    프론트엔드로 리다이렉트 uri 변경 시 적용
//    @PostMapping("/oauth2/instagram")
//    public AuthResponse exchangeCode(@RequestBody InstagramCodeRequest request) {
//        return authService.loginWithInstagramCode(request.getCode());
//    }
}