package com.bizket.insight.dto;

import com.bizket.insight.domain.BusinessCategory;

public record BusinessCategoryDto(
    Long id,
    String name
) {
    public static BusinessCategoryDto of(BusinessCategory e) {
        return new BusinessCategoryDto(e.getId(), e.getName());
    }
}