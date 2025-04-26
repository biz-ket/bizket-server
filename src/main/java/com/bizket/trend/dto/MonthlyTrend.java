package com.bizket.trend.dto;

public record MonthlyTrend(
        String keyword,
        String yearMonth,
        double searchVolume
) {}
