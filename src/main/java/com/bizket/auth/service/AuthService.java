package com.bizket.auth.service;
import com.bizket.auth.domain.InstagramToken;
import com.bizket.auth.dto.AuthResponse;
import com.bizket.auth.repository.InstagramTokenRepository;
import com.bizket.common.member.domain.Member;
import com.bizket.common.member.repository.MemberRepository;
import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.exception.BizExceptionType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final InstagramTokenRepository instagramTokenRepository;

    @Value("${spring.security.oauth2.client.registration.instagram.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.instagram.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.instagram.redirect-uri}")
    private String redirectUri;

    public AuthResponse loginWithInstagramCode(String rawCode) {

        String code = cleanAuthorizationCode(rawCode);
        Map<String, Object> body = requestAccessToken(code);
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
            // 신규 가입: short → long 교환
            longLivedToken = exchangeToLongLivedToken(accessToken);  // 60일 유효 :contentReference[oaicite:0]{index=0}
        } else if (instaToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 기존 토큰 만료: long → long 리프레시
            longLivedToken = refreshLongLivedToken(instaToken.getAccessToken());  // 60일 연장 :contentReference[oaicite:1]{index=1}
        } else {
            // 아직 유효한 토큰이 있으면 그대로 재사용
            longLivedToken = instaToken.getAccessToken();
        }

        if (instaToken == null || instaToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            saveOrUpdateLongToken(member, longLivedToken);
        }

        String jwt = jwtTokenProvider.createToken(member.getId().toString());

        return buildAuthResponse(member, jwt);
    }

    private String cleanAuthorizationCode(String rawCode) {
        return rawCode.replaceAll("#_$", "");
    }

    private Map<String, Object> requestAccessToken(String code) {
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
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                        .nickname(username)
                        .oauth2Provider(provider)
                        .providerId(userId)
                        .build();
                    return memberRepository.save(newMember);
                });
        } catch (Exception ex) {
            throw BizExceptionType.SERVER_ERROR.of("Member 저장 중 예외 발생: " + ex.getMessage());
        }
    }

    private AuthResponse buildAuthResponse(Member member, String jwt) {
        return new AuthResponse(
            jwt, "Bearer", member.getId(), member.getNickname());
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
