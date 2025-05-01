package com.bizket.insight.dto;

import com.bizket.common.member.domain.Member;
import com.bizket.insight.domain.BusinessPlace;

public record BusinessProfileResponse(
    String placeName,
    Long customerAgeGroupId,
    String customerAgeGroupLabel,
    Long businessCategoryId,
    String businessCategoryName,
    Long businessSubCategoryId,
    String businessSubCategoryName,
    Long businessDetailCategoryId,
    String businessDetailCategoryName,
    String openDate,
    String address,
    String placeEmail,
    String placePhoneNumber,
    int followerCount,
    String instagramAccountId
) {
    public static BusinessProfileResponse of(Member m, BusinessPlace p, int follower) {
        return new BusinessProfileResponse(
            p.getPlaceName(),
            p.getCustomerAgeGroup().getId(), p.getCustomerAgeGroup().getLabel(),
            p.getBusinessCategory().getId(), p.getBusinessCategory().getName(),
            p.getBusinessSubCategory().getId(), p.getBusinessSubCategory().getName(),
            p.getBusinessDetailCategory().getId(), p.getBusinessDetailCategory().getName(),
            p.getOpenDate(), p.getAddress(), p.getPlaceEmail(), p.getPlacePhoneNumber(),
            follower, m.getInstagramAccountId()
        );
    }
}
