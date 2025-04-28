package com.bizket.insight.service;

import com.bizket.auth.domain.InstagramToken;
import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.auth.repository.InstagramTokenRepository;
import com.bizket.common.member.domain.Member;
import com.bizket.common.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@AllArgsConstructor
@Service
public class InstagramInsightService {
    private final RestTemplate rt;
    private final InstagramTokenRepository tokenRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public JsonNode getAccountInsights(String jwtToken, List<String> metrics, String period) {
        validateToken(jwtToken);
        Member member = findMemberByToken(jwtToken);
        InstagramToken instaToken = findInstagramTokenByMember(member);
        String accessToken = instaToken.getAccessToken();
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
}