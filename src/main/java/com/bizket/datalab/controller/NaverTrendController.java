package com.bizket.datalab.controller;

import com.bizket.datalab.dto.forecastInterest.ForecastInterestResponseDto;
import com.bizket.datalab.dto.forecastInterest.ForecastUserFriendlyResponseDto;
import com.bizket.datalab.dto.monthlyInterest.MonthlyInterestResponseDto;
import com.bizket.datalab.dto.relatedInterest.RelatedSuggestDto;
import com.bizket.datalab.dto.saturation.CombinedSaturationResponseDto;
import com.bizket.datalab.dto.saturation.SaturationResponseDto;
import com.bizket.datalab.dto.weekday.WeekdayRatioResponseDto;
import com.bizket.datalab.service.NaverTrendService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 1. 월별 관심도 반환 ( 최근 6개월 )
 * 2. 키워드 탑10 추천 키워드
 * 3. 블로그 전체 검색량, 뉴스 전체 검색량 ( 카페는 네이버 oauth로그인 구현 필수라 집계 불가 )
 * 4. 블로그, 뉴스 키워드에 따른 검색 포화도
 * 5. 다음달 검색 비율 예측 ( 최근 6개월 )
 */

@RestController
@RequestMapping("/datalab/trends")
public class NaverTrendController {

    private final NaverTrendService service;

    public NaverTrendController(NaverTrendService service) {
        this.service = service;
    }

    // 월 별 관심도 반환
    // curl -s http://localhost:8080/datalab/trends/java/monthly | jq .
    @GetMapping("/{keyword}/monthly")
    public List<MonthlyInterestResponseDto> monthly(
            @PathVariable String keyword,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        String defaultEnd   = LocalDate.now().toString();
        String defaultStart = LocalDate.now().minusMonths(6).toString();
        return service.getMonthlyTrends(
                keyword,
                start != null ? start : defaultStart,
                end   != null ? end   : defaultEnd
        );
    }

    // 탑10 추천 키워드
    // curl -s "http://localhost:8080/datalab/trends/자바/related" | jq .
    @GetMapping("/{keyword}/related")
    public RelatedSuggestDto related(@PathVariable String keyword) {
        return service.getRelatedSuggestions(keyword);
    }

    // 블로그 전체 검색량
    // curl -s http://localhost:8080/datalab/trends/java/blog/total | jq .
    @GetMapping("/{kw}/blog/total")
    public Map<String,Object> blogTotal(@PathVariable("kw") String kw) {
        long total = service.fetchBlogTotalCount(kw);
        return Map.of("keyword", kw, "blogTotalCount", total);
    }

    // 뉴스 전체 검색량
    // curl -s http://localhost:8080/datalab/trends/java/news/total | jq .
    @GetMapping("/{kw}/news/total")
    public Map<String,Object> newsTotal(@PathVariable("kw") String kw) {
        long total = service.fetchNewsTotalCount(kw);
        return Map.of("keyword", kw, "newsTotalCount", total);
    }

    // 블로그 포화지수
    // curl -s http://localhost:8080/datalab/trends/java/saturation-blog | jq .
    @GetMapping("/{kw}/saturation-blog")
    public SaturationResponseDto satBlog(@PathVariable String kw) {
        return service.calculateBlogSaturation(kw);
    }

    // 뉴스 포화지수
    // curl -s http://localhost:8080/datalab/trends/java/saturation-news | jq .
    @GetMapping("/{kw}/saturation-news")
    public SaturationResponseDto satNews(@PathVariable String kw) {
        return service.calculateNewsSaturation(kw);
    }

    // 블로그 + 뉴스 포화지수 합침
    @GetMapping("/{kw}/saturation")
    public CombinedSaturationResponseDto combinedSaturation(@PathVariable String kw) {
        return service.calculateCombinedSaturation(kw);
    }

    // 다음 달 관심도 비율 예측
    // curl -s "http://localhost:8080/datalab/trends/양파/forecast-friendly" | jq .
    @GetMapping("/{kw}/forecast-friendly")
    public ForecastUserFriendlyResponseDto forecastFriendly(
            @PathVariable String kw,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        String defaultStart = LocalDate.now().minusMonths(5).withDayOfMonth(1).toString();
        String defaultEnd   = LocalDate.now().toString();
        return service.forecastNextMonthInterestFriendly(
                kw,
                start != null ? start : defaultStart,
                end   != null ? end   : defaultEnd
        );
    }

     // 요일별 검색 비율
     // curl -s "http://localhost:8080/datalab/trends/{kw}/weekday-ratio?start=2025-01-01&end=2025-05-06" | jq .
    @GetMapping("/{kw}/weekday-ratio")
    public WeekdayRatioResponseDto weekdayRatio(
            @PathVariable("kw") String kw,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        String defaultEnd   = LocalDate.now().toString();
        String defaultStart = LocalDate.now().minusMonths(6).toString();
        return service.getWeekdayRatio(
                kw,
                start != null ? start : defaultStart,
                end   != null ? end   : defaultEnd
        );
    }
}
