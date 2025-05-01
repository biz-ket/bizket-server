package com.bizket.insight.dto;

import com.bizket.insight.domain.BusinessSubCategory;

public record BusinessSubCategoryDto(
    Long id,
    String name
) {
    public static BusinessSubCategoryDto of(BusinessSubCategory e) {
        return new BusinessSubCategoryDto(e.getId(), e.getName());
    }
}