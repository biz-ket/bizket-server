package com.bizket.insight.dto;

import com.bizket.common.member.domain.Member;

public record MemberDto(
    Long id,
    String nickname,
    String email,
    String instagramAccountId,
    String threadsAccountId,
    String profileImageUrl          // ← 새로 추가
) {
    public static MemberDto of(Member member, String profileImageUrl) {
        return new MemberDto(
            member.getId(),
            member.getNickname(),
            member.getEmail(),
            member.getInstagramAccountId(),
            member.getThreadsAccountId(),
            profileImageUrl
        );
    }
}