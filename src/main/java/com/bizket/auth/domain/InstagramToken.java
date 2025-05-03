package com.bizket.auth.domain;

import com.bizket.common.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@NoArgsConstructor
@Entity
public class InstagramToken {

    @Id
    private Long Id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public InstagramToken(Member member) {
        this.member = member;
    }

    public void renew(String newToken, LocalDateTime newExpiry) {
        this.accessToken = newToken;
        this.expiresAt = newExpiry;
    }
}
