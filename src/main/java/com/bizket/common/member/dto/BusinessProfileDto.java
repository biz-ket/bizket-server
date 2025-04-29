package com.bizket.common.member.dto;

import com.bizket.insight.domain.BusinessPlace;

public record BusinessProfileDto(
    String placeName,
    String businessInfo,
    String customerAgeGroup,
    String openDate,
    String address,
    String placeEmail,
    String placePhoneNumber
) {
    public BusinessPlace toEntity() {
        return BusinessPlace.builder()
            .placeName(this.placeName)
            .businessInfo(this.businessInfo)
            .customerAgeGroup(this.customerAgeGroup)
            .openDate(this.openDate)
            .address(this.address)
            .placeEmail(this.placeEmail)
            .placePhoneNumber(this.placePhoneNumber)
            .build();
    }
}
