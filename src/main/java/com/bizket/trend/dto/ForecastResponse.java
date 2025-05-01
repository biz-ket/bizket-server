package com.bizket.trend.dto;

// 다음 달 예측 검색량 응답
public record ForecastResponse(
        String keyword,
        double forecastVolume
) {}