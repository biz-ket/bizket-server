package com.bizket.datalab.dto.saturation;

public record SaturationResponseDto(
        String keyword,
        long contentCount,
        long searchCount,
        double saturationIndex
) {}