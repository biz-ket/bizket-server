package com.bizket.trend.service;

import com.bizket.trend.dto.MonthlyTrend;
import com.bizket.trend.dto.RegionTrend;
import com.bizket.trend.dto.RelatedSuggestResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TrendService {
    private final RestTemplate rest = new RestTemplate();
    private final String baseUrl;

    public TrendService(@Value("${py.trends-service-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // 1) 월별 관심도 반환
    public List<MonthlyTrend> fetchMonthlyTrend(String keyword) {
        String url = String.format("%s/monthly?keyword=%s", baseUrl, keyword);
        ResponseEntity<List<MonthlyTrend>> resp = rest.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<MonthlyTrend>>() {}
        );
        return resp.getBody();
    }

    // 2) 지역별 관심도 반환
    public List<RegionTrend> fetchRegionInterest(String keyword, String geo, String resolution) {
        String url = String.format(
                "%s/region?keyword=%s&geo=%s&resolution=%s",
                baseUrl, keyword, geo, resolution
        );
        ResponseEntity<List<RegionTrend>> resp = rest.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<RegionTrend>>() {}
        );
        return resp.getBody();
    }

    // 3) 키워드에 따른 키워드 TOP10 반환
    public RelatedSuggestResponse fetchRelatedSuggestions(String keyword) {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = String.format("%s/related?keyword=%s", baseUrl, encoded);
        ResponseEntity<RelatedSuggestResponse> resp = rest.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<RelatedSuggestResponse>() {}
        );
        return resp.getBody();
    }
}
