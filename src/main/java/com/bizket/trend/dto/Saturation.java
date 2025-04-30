package com.bizket.trend.dto;

public record Saturation(
        String keyword,
        long contentCount,
        long searchCount,
        double saturationIndex  // (contentCount/searchCount)*100
) {}