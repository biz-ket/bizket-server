package com.bizket.datalab.dto.forecastInterest;

import java.time.YearMonth;

public record ForecastInterestResponseDto(
        String keyword,
        YearMonth forecastMonth,
        double forecastRatio
) {}