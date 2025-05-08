package com.bizket.marketing.infrastructure.clova;

import com.bizket.config.ClovaConfig;
import com.bizket.marketing.api.dto.request.clova.ClovaRequest;
import com.bizket.marketing.api.dto.response.clova.ClovaResponse;
import com.bizket.marketing.api.dto.response.clova.ClovaResult;
import com.bizket.marketing.domain.clova.ClovaMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClovaApiClient {

    private static final double DEFAULT_TOP_P = 0.8;
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int DEFAULT_MAX_TOKENS = 1024;
    private static final double DEFAULT_PENALTY = 1.1;
    private static final boolean DEFAULT_INCLUDE_AI_FILTERS = false;

    private final ClovaConfig clovaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;  // Jackson

    @Retryable(
        value = { java.io.IOException.class, java.lang.IllegalStateException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public ClovaResult send(List<ClovaMessage> messages) {
        HttpHeaders headers = buildHeaders();
        ClovaRequest body = buildRequestBody(messages);
        String url = clovaConfig.baseUrl() + "/v3/chat-completions/" + clovaConfig.model();

        try {
            // ① 요청 전체 로깅
            log.info("▶ Clova 요청 URL       : {}", url);
            log.info("▶ Clova 요청 헤더     : {}", objectMapper.writeValueAsString(headers.toSingleValueMap()));
            log.info("▶ Clova 요청 바디(JSON): {}", objectMapper.writeValueAsString(body));

            // ② API 호출
            ResponseEntity<ClovaResponse> resp = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), ClovaResponse.class);

            // ③ 원시 응답 로깅
            log.info("◀ Clova 응답 상태코드  : {}", resp.getStatusCode());
            log.info("◀ Clova 응답 바디(JSON): {}", objectMapper.writeValueAsString(resp.getBody()));

            // ④ content 추출 전 로깅
            String content = resp.getBody().result().message().content();
            log.info("◀ Clova 응답 content  : {}", content);

            return parseClovaContent(content);

        } catch (Exception ex) {
            log.error("Clova API 호출 오류", ex);
            throw new IllegalStateException("Clova 호출에 실패했습니다.", ex);
        }
    }

    @Recover
    public ClovaResult recover(RuntimeException e, List<ClovaMessage> messages) {
        log.error("Clova API 재시도에도 실패했습니다:", e);
        throw new IllegalStateException("Clova 호출에 실패했습니다.", e);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(clovaConfig.clovaApiKey());
        headers.set("X-NCP-APIGW-API-KEY-ID", clovaConfig.apiKeyId());
        return headers;
    }

    private ClovaRequest buildRequestBody(List<ClovaMessage> messages) {
        return new ClovaRequest(
            messages,
            clovaConfig.model(),
            DEFAULT_TOP_P,
            DEFAULT_TEMPERATURE,
            DEFAULT_MAX_TOKENS,
            DEFAULT_PENALTY,
            DEFAULT_INCLUDE_AI_FILTERS
        );
    }

    private ClovaResult parseClovaContent(String content) {
        String[] lines = content.split("\n");

        String marketingContent = null;
        List<String> hashtags = List.of();

        for (String rawLine : lines) {
            // 줄 전체에 "마케팅 문구:"가 포함되어 있으면 그 뒤만 꺼내고,
            if (rawLine.contains("마케팅 문구:")) {
                marketingContent = rawLine
                    .substring(rawLine.indexOf("마케팅 문구:") + "마케팅 문구:".length())
                    .trim();
            }
            // 줄 전체에 "해시태그:"가 포함되어 있으면 그 뒤만 꺼내서 #붙이기
            else if (rawLine.contains("해시태그:")) {
                String raw = rawLine
                    .substring(rawLine.indexOf("해시태그:") + "해시태그:".length())
                    .trim();
                hashtags = Arrays.stream(raw.split("[#,\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(tag -> "#" + tag)
                    .collect(Collectors.toList());
            }
        }

        if (marketingContent == null) {
            log.error("▶ parseClovaContent 실패, 받은 content = {}", content);
            throw new IllegalStateException("Clova 응답에서 마케팅 문구를 찾을 수 없습니다.");
        }

        return new ClovaResult(marketingContent, hashtags);
    }

}
