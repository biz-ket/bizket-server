package com.bizket.common.member.dto;


import com.bizket.common.member.domain.Member;
import com.bizket.insight.domain.BusinessPlace;

public record MypageDto(
    String nickname,
    String placeEmail,
    String placePhoneNumber
) {
    public static MypageDto of(Member member, BusinessPlace place) {
        return new MypageDto(
            member.getNickname(),
            place != null ? place.getPlaceEmail() : null,
            place != null ? place.getPlacePhoneNumber() : null
        );
    }
}
