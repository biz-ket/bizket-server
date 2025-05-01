package com.bizket.insight.dto;


import com.bizket.insight.domain.BusinessDetailCategory;

public record BusinessDetailCategoryDto(
    Long id,
    String name
) {
    public static BusinessDetailCategoryDto of(BusinessDetailCategory e) {
        return new BusinessDetailCategoryDto(e.getId(), e.getName());
    }
}
