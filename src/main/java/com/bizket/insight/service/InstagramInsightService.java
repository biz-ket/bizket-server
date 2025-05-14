package com.bizket.insight.service;

import com.bizket.auth.domain.InstagramToken;
import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.auth.repository.InstagramTokenRepository;
import com.bizket.common.member.domain.Member;
import com.bizket.common.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 사용자의 모든 미디어 + 각 미디어 인사이트를 합쳐서 반환
     */
    public JsonNode getAllMediaWithInsights(String jwtToken) {
        // 1) 액세스 토큰 획득
        String accessToken = resolveAccessToken(jwtToken);

        // 2) 미디어 목록 조회
        JsonNode mediaArray = getUserMedia(accessToken);  // ArrayNode

        // 3) 결과를 담을 ArrayNode
        ArrayNode result = MAPPER.createArrayNode();

        // 4) 각 미디어에 대해 인사이트 조회 후, 배열 풀어서 merged 노드에 넣기
        for (JsonNode media : mediaArray) {
            String mediaId = media.get("id").asText();
            JsonNode insights = getMediaInsights(mediaId, accessToken);

            // media 필드를 ObjectNode 로 복사
            ObjectNode merged = MAPPER.createObjectNode();
            media.fieldNames().forEachRemaining(field ->
                merged.set(field, media.get(field))
            );

            // insights 배열을 순회하며 name:value 형태로 바로 put()
            for (JsonNode metricNode : insights) {
                String name = metricNode.get("name").asText();
                int value  = metricNode
                    .get("values")
                    .get(0)
                    .get("value")
                    .asInt();
                merged.put(name, value);
            }

            // platform 필드 추가
            merged.put("platform", "instagram");

            result.add(merged);
        }

        return result;
    }

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

    public JsonNode getProfileInfo(String jwtToken) {
        // JWT → Member → InstagramToken → accessToken
        String accessToken = resolveAccessToken(jwtToken);

        // DB에 저장된 Instagram Account ID
        Member member = findMemberByToken(jwtToken);
        String igAccountId = member.getProviderId();
        if (igAccountId == null || igAccountId.isBlank()) {
            log.error("Member#{} 에 instagramAccountId 가 없습니다.", member.getId());
            throw new IllegalStateException("인스타그램 계정이 연결되어 있지 않습니다.");
        }

        // Graph API 호출: id, username, profile_picture_url
        String url = UriComponentsBuilder
            .fromHttpUrl(GRAPH_API_HOST + "/" + igAccountId)
            .queryParam("fields", "id,username,profile_picture_url")
            .queryParam("access_token", accessToken)
            .toUriString();

        log.debug("→ GET {}", url);
        JsonNode resp = rt.getForObject(url, JsonNode.class);
        if (resp == null || resp.get("username") == null) {
            log.error("Instagram 프로필 정보 호출 실패: {}", resp);
            throw new IllegalStateException("인스타그램 프로필 정보를 가져오지 못했습니다.");
        }

        // 필요한 필드만 골라서 반환하거나, 그대로 반환해도 무방합니다.
        ObjectNode profile = MAPPER.createObjectNode();
        profile.put("instaUsername", resp.get("username").asText());
        profile.put("profilePictureUrl", resp.has("profile_picture_url")
            ? resp.get("profile_picture_url").asText()
            : "");

        return profile;
    }

    /**
     * 특정 미디어 ID의 인사이트 조회 (comments, likes, shares, saved, lifetime 고정)
     */
    public JsonNode getMediaInsights(String mediaId, String accessToken) {
        List<String> fixedMetrics = List.of("comments", "likes", "shares", "saved");
        String fixedPeriod = "lifetime";

        log.debug("[Fixed Media Insights] mediaId={}, metrics={}, period={}",
            mediaId, fixedMetrics, fixedPeriod);

        String url = UriComponentsBuilder
            .fromHttpUrl(GRAPH_API_HOST + "/" + API_VERSION + "/" + mediaId + "/insights")
            .queryParam("metric", String.join(",", fixedMetrics))
            .queryParam("period", fixedPeriod)
            .queryParam("access_token", accessToken)
            .toUriString();
        log.debug("→ GET {}", url);

        try {
            JsonNode response = rt.getForObject(url, JsonNode.class);
            if (response == null || response.get("data") == null) {
                log.error("Fixed Media Insights API 호출 실패 for mediaId={}", mediaId);
                throw new IllegalStateException("인사이트 데이터를 가져오지 못했습니다.");
            }
            log.debug("[Fixed Media Insights response] {}", response);
            return response.get("data");
        } catch (HttpClientErrorException.BadRequest e) {
            // Instagram error 응답에서 subcode 파싱
            try {
                JsonNode error = MAPPER.readTree(e.getResponseBodyAsString()).path("error");
                int subcode = error.path("error_subcode").asInt(-1);
                if (subcode == 2108006) {
                    log.warn("미디어 {} 는 비즈니스 전환 이전 게시물로, 인사이트를 null로 반환합니다.", mediaId);
                    // metrics 수만큼 null 값을 가진 ArrayNode 생성
                    ArrayNode result = MAPPER.createArrayNode();
                    for (String metric : fixedMetrics) {
                        ObjectNode metricNode = MAPPER.createObjectNode();
                        metricNode.put("name", metric);
                        // values: [{ "value": null }]
                        ArrayNode values = MAPPER.createArrayNode();
                        ObjectNode valueNode = MAPPER.createObjectNode();
                        valueNode.putNull("value");
                        values.add(valueNode);
                        metricNode.set("values", values);
                        result.add(metricNode);
                    }
                    return result;
                }
            } catch (Exception ignore) {
                // 파싱 실패 시 그냥 아래에서 예외 다시 던짐
            }
            // 그 외 BadRequest 는 그대로 전파
            throw e;
        }
    }


    /**
     * Instagram 비즈니스 계정의 현재 팔로워 수를 가져온다.
     */
    public int getFollowerCount(String jwtToken) {
        String accessToken = resolveAccessToken(jwtToken);
        String userId = fetchInstagramUserId(accessToken);

        String url = UriComponentsBuilder
            .fromHttpUrl("https://graph.instagram.com/" + API_VERSION + "/" + userId)
            .queryParam("fields", "followers_count")
            .queryParam("access_token", accessToken)
            .toUriString();

        log.debug("→ GET {}", url);

        JsonNode response = rt.getForObject(url, JsonNode.class);
        if (response == null || response.get("followers_count") == null) {
            log.error("followers_count 가져오기 실패");
            throw new IllegalStateException("팔로워 수를 가져오지 못했습니다.");
        }

        return response.get("followers_count").asInt();
    }

}