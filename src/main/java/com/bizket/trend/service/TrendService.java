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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    // ====================================================================================

    // 6) 다음 달 예측 검색량 계산
    public ForecastResponse forecastNextMonthSearchVolume(String keyword) {
        List<MonthlyTrend> trends = fetchMonthlyTrend(keyword);
        int n = trends.size();
        if (n < 2) {
            // 데이터가 부족하면 마지막 값 그대로 반환
            double last = (n == 1 ? trends.get(0).searchVolume() : 0.0);
            return new ForecastResponse(keyword, last);
        }

        // 6-1) 각 달 변화량 합산
        double sumDelta = 0;
        for (int i = 1; i < n; i++) {
            sumDelta += trends.get(i).searchVolume() - trends.get(i - 1).searchVolume();
        }
        // 6-2) 평균 변화량
        double avgDelta = sumDelta / (n - 1);
        // 6-3) 예측 = 마지막 달 + 평균 변화량
        double lastVol = trends.get(n - 1).searchVolume();
        double forecast = lastVol + avgDelta;

        return new ForecastResponse(keyword, Math.max(forecast, 0));
    }

    // ====================================================================================

    // 7) 요일별 검색량 비율 계산
    public WeekdayRatioResponse fetchWeekdayRatio(String keyword, Integer year, Integer month) {
        // daily API URL 구성
        StringBuilder url = new StringBuilder();
        url.append(baseUrl)
                .append("/daily?keyword=")
                .append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        if (year != null && month != null) {
            url.append("&year=").append(year)
                    .append("&month=").append(month);
        }

        ResponseEntity<List<DailyTrend>> resp = rest.exchange(
                url.toString(),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {}
        );
        List<DailyTrend> daily = Optional.ofNullable(resp.getBody()).orElse(List.of());

        // 요일별 평균 계산
        Map<DayOfWeek, Double> avgByDow = daily.stream()
                .collect(Collectors.groupingBy(
                        dt -> LocalDate.parse(dt.date()).getDayOfWeek(),
                        Collectors.averagingDouble(DailyTrend::searchVolume)
                ));

        // 모든 요일 포함 및 비율 계산
        Map<String, Double> avgMap = new LinkedHashMap<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            avgMap.put(dow.name(), avgByDow.getOrDefault(dow, 0.0));
        }
        double total = avgMap.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, Double> ratio = avgMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> total > 0 ? e.getValue() / total : 0.0,
                        (a,b) -> a,
                        LinkedHashMap::new
                ));

        return new WeekdayRatioResponse(keyword, ratio);
    }

    // ====================================================================================
    // 8) N개월 전 한 달 동안의 콘텐츠 발행량(근사치) 계산
    public ContentVolumeResponse fetchContentVolume(String keyword, int monthsAgo) {
        // 1) 조회하려는 월의 첫째/마지막 날짜 계산
        LocalDate now = LocalDate.now();
        LocalDate target = now.minusMonths(monthsAgo);
        LocalDate firstDay = target.withDayOfMonth(1);
        LocalDate lastDay  = target.withDayOfMonth(target.lengthOfMonth());

        // 2) CSE용 쿼리에 날짜 필터 추가 (예: "java after:2025-04-01 before:2025-04-30")
        String dateFilter = String.format(" after:%s before:%s",
                firstDay, lastDay);
        String q = keyword + dateFilter;

        // 3) 검색 결과 수 가져오기
        long count = fetchSearchResultCount(q);

        return new ContentVolumeResponse(keyword, monthsAgo, count);
    }

    /**
     * 구글 Custom Search JSON API로 검색 결과 건수(totalResults) 반환
     */



}
