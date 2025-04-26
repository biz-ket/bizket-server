// src/main/java/com/bizket/trend/service/TrendService.java
package com.bizket.trend.service;

import com.bizket.trend.dto.MonthlyTrend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

@Service
public class TrendService {
    private final RestTemplate rest = new RestTemplate();
    private final String baseUrl;

    public TrendService(@Value("${py.trends-service-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<MonthlyTrend> fetchMonthlyTrend(String keyword) {
        String url = String.format("%s/monthly?keyword=%s", baseUrl, keyword);
        ResponseEntity<List<MonthlyTrend>> resp = rest.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {}
        );
        return resp.getBody();

    }
}
