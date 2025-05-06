package com.bizket.marketing.infrastructure.clova;

import com.bizket.config.ClovaConfig;
import com.bizket.marketing.api.dto.request.clova.ClovaRequest;
import com.bizket.marketing.api.dto.response.clova.ClovaResponse;
import com.bizket.marketing.api.dto.response.clova.ClovaResult;
import com.bizket.marketing.domain.clova.ClovaMessage;
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

    public ClovaResult send(List<ClovaMessage> messages) {
        HttpHeaders headers = buildHeaders();
        ClovaRequest body = buildRequestBody(messages);

        HttpEntity<ClovaRequest> entity = new HttpEntity<>(body, headers);
        String url = clovaConfig.baseUrl() + "/v3/chat-completions/" + clovaConfig.model();

        ResponseEntity<ClovaResponse> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            ClovaResponse.class
        );
        ClovaResponse responseBody = response.getBody();

        String content = responseBody.result().message().content();
        return parseClovaContent(content);
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

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("마케팅 문구:")) {
                marketingContent = line.replace("마케팅 문구:", "").trim();
            } else if (line.startsWith("해시태그:")) {
                String raw = line.replace("해시태그:", "").trim();
                hashtags = Arrays.stream(raw.split("[#,\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(tag -> "#" + tag)
                    .collect(Collectors.toList());
            }
        }

        if (marketingContent == null) {
            throw new IllegalStateException("Clova 응답에서 마케팅 문구를 찾을 수 없습니다.");
        }

        return new ClovaResult(marketingContent, hashtags);
    }

}
