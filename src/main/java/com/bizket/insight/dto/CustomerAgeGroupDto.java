package com.bizket.insight.dto;

import com.bizket.insight.domain.CustomerAgeGroup;

public record CustomerAgeGroupDto(
    Long id,
    String label
) {
    public static CustomerAgeGroupDto of(CustomerAgeGroup e) {
        return new CustomerAgeGroupDto(e.getId(), e.getLabel());
    }
}