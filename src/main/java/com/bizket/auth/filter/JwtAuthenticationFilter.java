package com.bizket.auth.filter;

import com.bizket.auth.util.JwtUtil;
import com.bizket.member.domain.Member;
import com.bizket.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter  extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {
        // 요청 헤더에서 Authorization 값을 가져옵니다.
        String header = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7); // "Bearer " 접두어 제거
        }

        // 토큰이 존재하고 유효하면 사용자 정보를 파싱해서 SecurityContext에 등록합니다.
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(member, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }
}
