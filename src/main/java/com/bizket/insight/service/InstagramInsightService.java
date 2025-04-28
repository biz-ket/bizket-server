package com.bizket.insight.service;

import com.bizket.auth.domain.InstagramToken;
import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.auth.repository.InstagramTokenRepository;
import com.bizket.common.member.domain.Member;
import com.bizket.common.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@AllArgsConstructor
@Service
public class InstagramInsightService {
    private static final String GRAPH_API_HOST = "https://graph.instagram.com";
    private static final String API_VERSION = "v22.0";

    private final RestTemplate rt;
    private final InstagramTokenRepository tokenRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    /**
     * Account insights 조회 (JWT 기반)
     */
    public JsonNode getAccountInsights(String jwtToken, List<String> metrics, String period) {
        String accessToken = resolveAccessToken(jwtToken);
        String userId = fetchInstagramUserId(accessToken);
        List<String> realMetrics = mapDeprecatedMetrics(metrics);
        return fetchInsights(userId, realMetrics, period, accessToken);
    }

    private void validateToken(String jwtToken) {
        log.debug("[Step 1] validateToken: {}", jwtToken);
        if (!jwtTokenProvider.validateToken(jwtToken)) {
            log.error("Invalid JWT token");
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    private Member findMemberByToken(String jwtToken) {
        log.debug("[Step 2] extract memberId from token");
        String memberIdStr = jwtTokenProvider.getMemberId(jwtToken);
        log.debug("→ memberId: {}", memberIdStr);

        log.debug("[Step 3] lookup Member by id");
        return memberRepository.findById(Long.parseLong(memberIdStr))
            .orElseThrow(() -> {
                log.error("Member not found for id={}", memberIdStr);
                return new IllegalStateException("멤버가 존재하지 않습니다.");
            });
    }

    private InstagramToken findInstagramTokenByMember(Member member) {
        log.debug("[Step 4] lookup InstagramToken for Member");
        return tokenRepo.findByMember(member)
            .orElseThrow(() -> {
                log.error("InstagramToken not found for memberId={}", member.getId());
                return new IllegalStateException("인스타그램 토큰이 없습니다.");
            });
    }

    /**
     * Service 내부에서 accessToken을 얻는 헬퍼
     */
    public String resolveAccessToken(String jwtToken) {
        // 기존 로직: validateToken → findMemberByToken → findInstagramTokenByMember
        validateToken(jwtToken);
        Member member = findMemberByToken(jwtToken);
        InstagramToken instaToken = findInstagramTokenByMember(member);
        return instaToken.getAccessToken();
    }

    private String fetchInstagramUserId(String accessToken) {
        log.debug("[Step 5] fetch Instagram User ID via /me");
        String meUrl = UriComponentsBuilder
            .fromHttpUrl("https://graph.instagram.com/me")
            .queryParam("fields", "id")
            .queryParam("access_token", accessToken)
            .toUriString();
        log.debug("→ GET {}", meUrl);

        JsonNode response = rt.getForObject(meUrl, JsonNode.class);
        if (response == null || response.get("id") == null) {
            log.error("Instagram /me API 실패");
            throw new IllegalStateException("인스타그램 사용자 ID를 가져오지 못했습니다.");
        }
        return response.get("id").asText();
    }

    private List<String> mapDeprecatedMetrics(List<String> metrics) {
        log.debug("[Step 6] map deprecated metrics");
        List<String> realMetrics = metrics.stream()
            .map(m -> m.equals("impressions") ? "views" : m)
            .toList();
        log.debug("→ final metrics: {}", realMetrics);
        return realMetrics;
    }

    private JsonNode fetchInsights(String userId, List<String> metrics, String period, String accessToken) {
        log.debug("[Step 7] calling Insights API");
        String insightsUrl = UriComponentsBuilder
            .fromHttpUrl("https://graph.instagram.com/{userId}/insights")
            .queryParam("metric", String.join(",", metrics))
            .queryParam("period", period)
            .queryParam("access_token", accessToken)
            .buildAndExpand(userId)
            .toUriString();
        log.debug("→ GET {}", insightsUrl);

        JsonNode response = rt.getForObject(insightsUrl, JsonNode.class);
        if (response == null || response.get("data") == null) {
            log.error("Insights API 호출 실패");
            throw new IllegalStateException("인사이트 데이터를 가져오지 못했습니다.");
        }
        log.debug("[Step 8] Insights response: {}", response);
        return response.get("data");
    }

    /**
     * 사용자의 모든 게시물(id 포함) 조회
     */
    public JsonNode getUserMedia(String accessToken) {
        log.debug("→ [Instagram] GET /me/media");
        String url = UriComponentsBuilder
            .fromHttpUrl("https://graph.instagram.com/me/media")
            .queryParam("fields", "id,caption,media_type,media_url,timestamp")
            .queryParam("access_token", accessToken)
            .toUriString();
        JsonNode resp = rt.getForObject(url, JsonNode.class);
        log.debug("→ media response: {}", resp);
        return resp.get("data");
    }

    /**
     * 특정 미디어 ID의 인사이트 조회 (metrics + period)
     */
    public JsonNode getMediaInsights(String mediaId, List<String> metrics, String period, String accessToken) {
        log.debug("[Step] calling Media Insights API for mediaId={}", mediaId);
        String url = UriComponentsBuilder
            .fromHttpUrl(GRAPH_API_HOST + "/" + API_VERSION + "/" + mediaId + "/insights")
            .queryParam("metric", String.join(",", metrics))
            .queryParam("period", period)
            .queryParam("access_token", accessToken)
            .toUriString();
        log.debug("→ GET {}", url);

        JsonNode response = rt.getForObject(url, JsonNode.class);
        if (response == null || response.get("data") == null) {
            log.error("Media Insights API 호출 실패 for mediaId={}", mediaId);
            throw new IllegalStateException("인사이트 데이터를 가져오지 못했습니다.");
        }
        log.debug("[Step] Media Insights response: {}", response);
        return response.get("data");
    }
}