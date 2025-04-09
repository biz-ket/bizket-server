package com.bizket.config;


import static org.springframework.security.config.Customizer.withDefaults;

import com.bizket.auth.filter.JwtAuthenticationFilter;
import com.bizket.auth.util.JwtUtil;
import com.bizket.member.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    // 인증 없이 허용할 URL 경로 목록을 정의합니다.
    public static final String[] ALLOWED_URLS = {
        "/v3/api-docs/**",
        "/api/v1/posts/**",
        "/api/v1/replies/**",
        "/login",
        "/auth/login/kakao/**",
        "/no-auth/**"
    };
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    public SecurityConfig(JwtUtil jwtUtil, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            // CSRF를 비활성화합니다.
            .csrf(csrf -> csrf.disable())
            // 세션 관리를 Stateless로 설정합니다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers(ALLOWED_URLS).permitAll()  // 인증 없이 접근 가능
                    .anyRequest().authenticated()  // 나머지는 인증 필요
            );

        // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, memberRepository),
            UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
