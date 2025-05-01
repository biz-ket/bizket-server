package com.bizket.trend.controller;

import com.bizket.trend.dto.*;
import com.bizket.trend.service.TrendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trends")
public class TrendController {

    private final TrendService trendService;


    public TrendController(TrendService trendService) {
        this.trendService = trendService;
    }

    //키워드에 대한 관심도
    @GetMapping("/{kw}/monthly")
    public List<MonthlyTrend> getMonthlyTrend(@PathVariable("kw") String kw) {
        return trendService.fetchMonthlyTrend(kw);
    }

    //키워드 검색량
    @GetMapping("/{kw}")
    public SearchCountResponse getSearchCount(@PathVariable("kw") String kw) {
        return trendService.fetchSearchCount(kw);
    }

//    //키워드에 대한 지역별 관심도
//    @GetMapping("/{kw}/region")
//    public List<RegionTrend> getRegionInterest(
//            @PathVariable String kw,
//            @RequestParam(required = false, defaultValue = "") String geo,
//            @RequestParam(required = false, defaultValue = "COUNTRY") String resolution
//    ) {
//        return trendService.fetchRegionInterest(kw, geo, resolution);
//    }

    //키워드에 따른 연관검색어 Top10 반환
    @GetMapping("/{kw}/related")
    public RelatedSuggestResponse getRelatedSuggestions(@PathVariable("kw") String kw) {
        return trendService.fetchRelatedSuggestions(kw);
    }

    //키워드에 대한 콘텐츠 포화 지수 계산 (일별 기준, 기본 30일)
    @GetMapping("/{kw}/saturation")
    public Saturation getSaturation(@PathVariable("kw") String kw) {
        return trendService.calculateSaturation(kw);
    }

    //키워드에 대한 다음 달 예측 검색량
    @GetMapping("/{kw}/forecast")
    public ForecastResponse getNextMonthForecast(@PathVariable("kw") String kw) {
        return trendService.forecastNextMonthSearchVolume(kw);
    }

    // 키워드에 대한 요일별 검색 비율 (선택적으로 year, month 파라미터 지원)
    // 파라미터 없으면 1년치 전체 조회
    @GetMapping("/{kw}/weekday-ratio")
    public WeekdayRatioResponse getWeekdayRatio(
            @PathVariable("kw") String kw,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return trendService.fetchWeekdayRatio(kw, year, month);
    }

    // 키워드에 대한 N개월 전 한 달 동안의 콘텐츠 발행량(근사치)
    @GetMapping("/{kw}/content-volume")
    public ContentVolumeResponse getContentVolume(
            @PathVariable("kw") String kw,
            @RequestParam(defaultValue = "1") int monthsAgo
    ) {
        return trendService.fetchContentVolume(kw, monthsAgo);
    }
}
