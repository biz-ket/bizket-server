package com.bizket.auth.service;

import com.bizket.auth.dto.KakaoDTO;
import com.bizket.auth.util.JwtUtil;
import com.bizket.auth.util.KakaoUtil;
import com.bizket.member.domain.Member;
import com.bizket.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final KakaoUtil kakaoUtil;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public Member oAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);
        String email = kakaoProfile.getKakao_account().getEmail();
        Member member = memberRepository.findByEmail(email).orElseGet(()->createNewMember(kakaoProfile));

        String rawToken = jwtUtil.createAccessToken(member.getEmail());
        String bearerToken = "Bearer " + rawToken;
        httpServletResponse.setHeader("Authorization", bearerToken);
        System.out.println("토큰 생성 확인 ------ JWT Token: " + rawToken + " email : " + jwtUtil.getEmailFromToken(rawToken));

        return member;
    }

    private Member createNewMember(KakaoDTO.KakaoProfile kakaoProfile) {
        Member newMember = new Member(kakaoProfile.getKakao_account().getEmail(), kakaoProfile.getKakao_account().getProfile()
            .getNickname());
        return memberRepository.save(newMember);
    }

}
