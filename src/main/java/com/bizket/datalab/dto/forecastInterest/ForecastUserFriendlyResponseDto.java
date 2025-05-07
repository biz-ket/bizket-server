package com.bizket.datalab.dto.forecastInterest;

import java.time.YearMonth;

public record ForecastUserFriendlyResponseDto(
        String keyword,             // 입력 키워드
        YearMonth forecastMonth,    // 예측 연월
        double changePercent,       // 전월 대비 증감 퍼센트 (ex: +0.9)
        String trendLabel           // "관심도 상승 예상", "관심도 감소 예상"
) {}