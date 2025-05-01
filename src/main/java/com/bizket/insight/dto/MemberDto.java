package com.bizket.insight.dto;

import com.bizket.common.member.domain.Member;

public record MemberDto(
    Long id,
    String nickname,
    String email,
    String instagramAccountId,
    String threadsAccountId
) {
    public static MemberDto of(Member member) {
        return new MemberDto(
            member.getId(),
            member.getNickname(),
            member.getEmail(),
            member.getInstagramAccountId(),
            member.getThreadsAccountId()
        );
    }
}