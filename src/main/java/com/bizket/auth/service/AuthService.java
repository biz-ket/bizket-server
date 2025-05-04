package com.bizket.auth.service;

import com.bizket.auth.domain.InstagramToken;
import com.bizket.auth.domain.RefreshToken;
import com.bizket.auth.dto.AuthResponse;
import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.auth.repository.InstagramTokenRepository;
import com.bizket.auth.repository.RefreshTokenRepository;
import com.bizket.common.member.domain.Member;
import com.bizket.common.member.repository.MemberRepository;
import com.bizket.exception.BizExceptionType;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final InstagramTokenRepository instagramTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.security.oauth2.client.registration.instagram.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.instagram.client-secret}")
    private String clientSecret;

    public AuthResponse loginWithInstagramCode(String rawCode, String redirectUri) {
        String code = cleanAuthorizationCode(rawCode);
        Map<String, Object> body = requestAccessToken(code, redirectUri);
        String accessToken = (String) body.get("access_token");
        String userId = String.valueOf(body.get("user_id"));

        Map<String, Object> userInfo = fetchInstagramUserInfo(accessToken);
        String username = (String) userInfo.get("username");
        String provider = "instagram";
        Member member = findOrCreateMember(userId, username, provider);

        InstagramToken instaToken = instagramTokenRepository
            .findById(member.getId())
            .orElse(null);

        String longLivedToken;

        if (instaToken == null) {
            longLivedToken = exchangeToLongLivedToken(accessToken);
        } else if (instaToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            longLivedToken = refreshLongLivedToken(
                instaToken.getAccessToken());
        } else {
            longLivedToken = instaToken.getAccessToken();
        }
        if (instaToken == null || instaToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            saveOrUpdateLongToken(member, longLivedToken);
        }

        String accessJwt  = jwtTokenProvider.createAccessToken(member.getId().toString());
        String refreshJwt = jwtTokenProvider.createRefreshToken(member.getId().toString());

        RefreshToken rtEntity = RefreshToken.builder()
            .memberId(member.getId())
            .token(refreshJwt)
            .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs()))
            .build();
        refreshTokenRepository.save(rtEntity);

        return new AuthResponse(
            accessJwt,
            "Bearer",
            member.getId(),
            member.getNickname(),
            refreshJwt
        );    }

    /**
     * Refresh Token으로 새 Access/Refresh Token을 발급
     */
    @Transactional
    public AuthResponse refreshTokens(String refreshToken) {
        // 1) 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BizExceptionType.UNAUTHORIZED.of("Refresh Token이 유효하지 않습니다");
        }
        // 2) 토큰에서 memberId 추출
        String memberId = jwtTokenProvider.getMemberId(refreshToken);
        // 3) 저장소에서 RefreshToken 엔티티 조회
        RefreshToken stored = refreshTokenRepository.findById(Long.valueOf(memberId))
            .orElseThrow(() -> BizExceptionType.UNAUTHORIZED.of("Refresh Token이 없습니다"));
        if (!stored.getToken().equals(refreshToken)
            || stored.getExpiresAt().isBefore(Instant.now())) {
            throw BizExceptionType.UNAUTHORIZED.of("Refresh Token이 만료되었거나 일치하지 않습니다");
        }
        // 4) 새 토큰 발급
        String newAccess  = jwtTokenProvider.createAccessToken(memberId);
        String newRefresh = jwtTokenProvider.createRefreshToken(memberId);
        // 5) 저장소 업데이트
        stored.setToken(newRefresh);
        stored.setExpiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs()));
        refreshTokenRepository.save(stored);
        // 6) 응답
        Member member = memberRepository.findById(Long.valueOf(memberId))
            .orElseThrow();
        return new AuthResponse(
            newAccess,
            "Bearer",
            Long.valueOf(memberId),
            member.getNickname(),
            newRefresh
        );
    }

    private String cleanAuthorizationCode(String rawCode) {
        return rawCode.replaceAll("#_$", "");
    }

    private Map<String, Object> requestAccessToken(String code, String redirectUri) {
        String tokenUrl = "https://api.instagram.com/oauth/access_token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map> tokenResp = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            return tokenResp.getBody();
        } catch (HttpClientErrorException e) {
            // Instagram API 호출 에러
            throw BizExceptionType.UNAUTHORIZED.of(
                "Instagram access_token 교환 실패: " + e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            // 기타 예외
            throw BizExceptionType.SERVER_ERROR.of(
                "Instagram access_token 처리 중 오류: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> fetchInstagramUserInfo(String accessToken) {
        String userInfoUrl = UriComponentsBuilder
            .fromHttpUrl("https://graph.instagram.com/me")
            .queryParam("fields", "id,username")
            .queryParam("access_token", accessToken)
            .toUriString();
        try {
            ResponseEntity<Map> userResp = restTemplate.getForEntity(userInfoUrl, Map.class);
            if (!userResp.getStatusCode().is2xxSuccessful() || userResp.getBody() == null) {
                throw BizExceptionType.SERVER_ERROR.of("Instagram 사용자 정보 조회 실패");
            }
            return userResp.getBody();
        } catch (HttpClientErrorException e) {
            throw BizExceptionType.UNAUTHORIZED.of("Instagram 사용자 정보 조회 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw BizExceptionType.SERVER_ERROR.of("Instagram 사용자 정보 조회 중 오류: " + e.getMessage());
        }
    }

    private Member findOrCreateMember(String userId, String username, String provider) {
        try {
            return memberRepository
                .findByProviderIdAndOauth2Provider(userId, provider)
                .map(existingMember -> {
                    // 항상 instagramAccountId를 인스타그램 username으로 덮어쓰기
                    existingMember.updateSnsAccount(username, existingMember.getThreadsAccountId());
                    return memberRepository.save(existingMember);
                })
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                        .nickname(username)
                        .oauth2Provider(provider)
                        .providerId(userId)
                        .instagramAccountId(username)   // username 저장
                        .build();
                    return memberRepository.save(newMember);
                });
        } catch (Exception ex) {
            throw BizExceptionType.SERVER_ERROR.of("Member 저장 중 예외 발생: " + ex.getMessage());
        }
    }

    private void saveOrUpdateLongToken(Member member, String longLivedToken) {
        instagramTokenRepository.findById(member.getId())
            .ifPresentOrElse(
                token -> token.renew(longLivedToken,
                    LocalDateTime.now().plusDays(60)),
                () -> instagramTokenRepository.save(
                    InstagramToken.builder()
                        .member(member)
                        .accessToken(longLivedToken)
                        .expiresAt(LocalDateTime.now().plusDays(60))
                        .build())
            );
    }

    private String exchangeToLongLivedToken(String shortLivedToken) {
        String url = UriComponentsBuilder
            .fromHttpUrl("https://graph.instagram.com/access_token")
            .queryParam("grant_type", "ig_exchange_token")
            .queryParam("client_secret", clientSecret)
            .queryParam("access_token", shortLivedToken)
            .toUriString();

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw BizExceptionType.SERVER_ERROR.of("장기 액세스 토큰 교환 실패");
            }
            return (String) response.getBody().get("access_token");
        } catch (HttpClientErrorException e) {
            throw BizExceptionType.UNAUTHORIZED.of("장기 액세스 토큰 교환 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw BizExceptionType.SERVER_ERROR.of("장기 토큰 교환 중 오류: " + e.getMessage());
        }
    }

    private String refreshLongLivedToken(String longLivedToken) {
        String url = UriComponentsBuilder
            .fromHttpUrl("https://graph.instagram.com/refresh_access_token")
            .queryParam("grant_type", "ig_refresh_token")
            .queryParam("access_token", longLivedToken)
            .toUriString();

        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw BizExceptionType.SERVER_ERROR.of("장기 토큰 리프레시 실패");
            }
            return (String) resp.getBody().get("access_token");
        } catch (HttpClientErrorException e) {
            throw BizExceptionType.UNAUTHORIZED.of("장기 토큰 리프레시 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw BizExceptionType.SERVER_ERROR.of("장기 토큰 리프레시 중 오류: " + e.getMessage());
        }
    }
}
