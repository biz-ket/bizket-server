package com.bizket.common.member.dto;

import com.bizket.common.member.domain.Member;
import com.bizket.insight.domain.BusinessPlace;

public record BusinessProfileResponse(
    String placeName,
    String businessInfo,
    String customerAgeGroup,
    String openDate,
    String address,
    int followerCount,
    String instagramAccountId
) {
    public static BusinessProfileResponse of(Member member, BusinessPlace businessPlace, int followerCount) {
        return new BusinessProfileResponse(
            businessPlace.getPlaceName(),
            businessPlace.getBusinessInfo(),
            businessPlace.getCustomerAgeGroup(),
            businessPlace.getOpenDate(),
            businessPlace.getAddress(),
            followerCount,
            member.getInstagramAccountId()
        );
    }
}