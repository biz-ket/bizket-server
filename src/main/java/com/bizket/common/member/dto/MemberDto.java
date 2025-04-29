package com.bizket.common.member.dto;

import com.bizket.common.member.domain.Member;

public record MemberDto(
    Long id,
    String nickname,
    String email,
    String profileImageUrl,
    String instagramAccountId,
    String threadsAccountId
) {
    public static MemberDto of(Member member) {
        return new MemberDto(
            member.getId(),
            member.getNickname(),
            member.getEmail(),
            member.getProfileImageUrl(),
            member.getInstagramAccountId(),
            member.getThreadsAccountId()
        );
    }
}