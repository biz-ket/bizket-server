package com.bizket.trend.dto;

import java.util.Map;

public record WeekdayRatioResponse(
        String keyword,
        Map<String, Double> ratio   // key: "MONDAY", "TUESDAY", …, "SUNDAY"
) {}