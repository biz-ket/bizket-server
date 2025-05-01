package com.bizket.insight.dto;

import com.bizket.common.member.domain.Member;

public record MemberPatchDto(
    String nickname,
    String email,
    String instagramAccountId,
    String threadsAccountId
) {
    public static MemberPatchDto of(Member member) {
        return new MemberPatchDto(
            member.getNickname(),
            member.getEmail(),
            member.getInstagramAccountId(),
            member.getThreadsAccountId()
        );
    }
}