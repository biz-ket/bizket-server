package com.bizket.trend.dto;

public record SearchCountResponse(
        String keyword,
        long searchCount
) {}