package com.bizket.insight.controller;


import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.insight.service.InstagramInsightService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/instagram/insight")
public class InstagramInsightController {
    private final InstagramInsightService insightService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/me")
    public ResponseEntity<JsonNode> getInsights(
        HttpServletRequest request,
        @RequestParam(defaultValue = "impressions,reach,profile_views") String metrics,
        @RequestParam(defaultValue = "day") String period) {

        // 1) 요청 헤더에서 JWT 꺼내기
        String jwtToken = jwtTokenProvider.resolveToken(request);
        if (jwtToken == null) {
            return ResponseEntity
                .badRequest()
                .body(null);
        }

        // 2) metrics 리스트로 변환
        List<String> metricList = Arrays.asList(metrics.split(","));

        // 3) Service 호출
        JsonNode data = insightService.getAccountInsights(jwtToken, metricList, period);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/me/media")
    public ResponseEntity<JsonNode> getAllMedia(HttpServletRequest request) {
        String jwt = jwtTokenProvider.resolveToken(request);
        if (jwt == null) {
            return ResponseEntity.badRequest().body(null);
        }
        String accessToken = insightService.resolveAccessToken(jwt);
        JsonNode mediaData = insightService.getUserMedia(accessToken);
        return ResponseEntity.ok(mediaData);
    }

    @GetMapping("/me/media-with-insights")
    public ResponseEntity<JsonNode> getAllMediaWithInsights(
        HttpServletRequest request
    ) {
        // 1) JWT 추출
        String jwt = jwtTokenProvider.resolveToken(request);
        if (jwt == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // 2) Service 호출 (metrics, period 파라미터 제거)
        JsonNode combined = insightService.getAllMediaWithInsights(jwt);

        // 3) 응답
        return ResponseEntity.ok(combined);
    }
    /**
     * 특정 미디어 ID의 인사이트(댓글·좋아요·공유·저장) 가져오기
     */
    @GetMapping("/me/media/{mediaId}/insights")
    public ResponseEntity<JsonNode> getOneMediaInsights(
        HttpServletRequest request,
        @PathVariable String mediaId
    ) {
        String jwt = jwtTokenProvider.resolveToken(request);
        if (jwt == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String accessToken = insightService.resolveAccessToken(jwt);
        JsonNode insights = insightService.getMediaInsights(mediaId, accessToken);
        return ResponseEntity.ok(insights);
    }


}


