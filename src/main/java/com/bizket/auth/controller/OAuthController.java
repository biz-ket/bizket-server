package com.bizket.auth.controller;

import com.bizket.auth.dto.AuthResponse;
import com.bizket.auth.dto.InstagramCodeRequest;
import com.bizket.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OAuthController {
    private final AuthService authService;

    // 임시
    @GetMapping("/oauth2/callback/instagram")
    public AuthResponse instagramCallback(@RequestParam("code") String code) {
        System.out.println("-------------User code??? : " + code);
        return authService.loginWithInstagramCode(code);
    }

    // 프론트엔드로 리다이렉트 uri 변경 시 적용
    @PostMapping("/oauth2/instagram")
    public AuthResponse exchangeCode(@RequestBody InstagramCodeRequest request) {
        return authService.loginWithInstagramCode(request.getCode());
    }
}