package com.bizket.auth.controller;

import com.bizket.auth.service.AuthService;
import com.bizket.auth.util.JwtUtil;
import com.bizket.member.domain.Member;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/auth/login/kakao")
    public ResponseEntity<Member> kakaoCallback(@RequestParam("code") String code,
        HttpServletResponse response) {
        // 전달받은 인가 코드를 이용해 로그인 또는 회원가입 처리
        Member member = authService.oAuthLogin(code, response);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/current-user")
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.bizket.member.domain.Member) {
                // principal을 Member 객체로 캐스팅하여 이메일을 가져옵니다.
                String email = ((com.bizket.member.domain.Member) principal).getEmail();
                return "현재 로그인한 사용자 이메일: " + email;
            } else {
                return "인증 정보가 Member 객체가 아닙니다.";
            }
        }
        return "로그인한 사용자가 없습니다.";
    }

    @GetMapping("/no-auth")
    public String test() {
        return "no auth test";
    }
}
