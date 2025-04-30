package com.bizket.trend.service;

import com.bizket.trend.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TrendService {

    @Value("${google.cse.key}")
    private String cseKey;
    @Value("${google.cse.cx}")
    private String cseCx;


    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;

    public TrendService(@Value("${py.trends-service-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // 1) 월별 관심도 반환 ==============================================
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

//    // 2) 지역별 관심도 반환
//    public List<RegionTrend> fetchRegionInterest(String keyword, String geo, String resolution) {
//        String url = String.format(
//                "%s/region?keyword=%s&geo=%s&resolution=%s",
//                baseUrl, keyword, geo, resolution
//        );
//        ResponseEntity<List<RegionTrend>> resp = rest.exchange(
//                url,
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                new ParameterizedTypeReference<List<RegionTrend>>() {}
//        );
//        return resp.getBody();
//    }

    // 3) 키워드에 따른 키워드 TOP10 반환 ==============================================
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

    // 4)콘텐츠 포화지수 (영어만 가능) ==============================================
    public Saturation calculateSaturation(String keyword) {
        long searchCount  = fetchSearchResultCount(keyword);
        long contentCount = fetchNewsArticleCount(keyword);

        double index = searchCount > 0
                ? (contentCount * 100.0 / searchCount)
                : 0.0;

        return new Saturation(keyword, contentCount, searchCount, index);
    }

    private long fetchSearchResultCount(String keyword) {
        try {
            String q = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://www.googleapis.com/customsearch/v1"
                            + "?key=%s&cx=%s&q=%s",
                    cseKey, cseCx, q
            );
            @SuppressWarnings("unchecked")
            Map<String,Object> resp = rest.getForObject(url, Map.class);
            Map<String,Object> info = (Map<String,Object>) resp.get("searchInformation");
            return Long.parseLong(info.get("totalResults").toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private long fetchNewsArticleCount(String keyword) {
        try {
            String q = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://www.googleapis.com/customsearch/v1"
                            + "?key=%s&cx=%s&q=%s&siteSearch=news.google.com",
                    cseKey, cseCx, q
            );
            @SuppressWarnings("unchecked")
            Map<String,Object> resp = rest.getForObject(url, Map.class);
            Map<String,Object> info = (Map<String,Object>) resp.get("searchInformation");
            return Long.parseLong(info.get("totalResults").toString());
        } catch (Exception e) {
            return 0L;
        }
    }
    // ====================================================================================

    // 5) 키워드에 따른 검색 횟수 반환
    public SearchCountResponse fetchSearchCount(String keyword) {
        long count = fetchSearchResultCount(keyword);
        return new SearchCountResponse(keyword, count);
    }
}
