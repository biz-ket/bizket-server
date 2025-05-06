package com.bizket.marketing.infrastructure.clova;

import com.bizket.config.ClovaConfig;
import com.bizket.marketing.api.dto.request.clova.ClovaRequest;
import com.bizket.marketing.domain.clova.ClovaMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public String send(List<ClovaMessage> messages) {
        HttpHeaders headers = buildHeaders();
        ClovaRequest body = buildRequestBody(messages);

        HttpEntity<ClovaRequest> entity = new HttpEntity<>(body, headers);
        String url = clovaConfig.baseUrl() + "/v3/chat-completions/" + clovaConfig.model();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        throw new RuntimeException("Clova API 응답 실패: " + response.getStatusCode());
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(clovaConfig.apiKey());
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
}
