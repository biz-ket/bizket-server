package com.bizket.datalab.dto.weekday;

import java.util.Map;

public record WeekdayRatioResponseDto(
        String keyword,
        Map<String, Double> ratioByDay
) {
}
