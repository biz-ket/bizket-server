package com.bizket.insight.dto;


public record BusinessProfileDto(
    String placeName,
    Long customerAgeGroupId,
    Long businessCategoryId,       // 대분류
    Long businessSubCategoryId,    // 중분류
    Long businessDetailCategoryId, // 소분류
     String openDate,
    String address,
    String placeEmail,
    String placePhoneNumber
) {
}