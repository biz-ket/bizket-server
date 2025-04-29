package com.bizket.common.member.domain;

import static jakarta.persistence.FetchType.*;

import com.bizket.common.model.BaseEntity;
import com.bizket.insight.domain.BusinessPlace;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nickname;

    @Column(nullable = true, length = 255, unique = true)
    private String email;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "oauth2_provider", nullable = false)
    private String oauth2Provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "instagram_account_id", length = 255)
    private String instagramAccountId;

    @Column(name = "threads_account_id", length = 255)
    private String threadsAccountId;

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "business_place_id")
    private BusinessPlace businessPlace;

    public void updateBusinessPlace(BusinessPlace businessPlace) {
        this.businessPlace = businessPlace;
    }

    public void updateSnsAccount(String instagramAccountId, String threadsAccountId) {
        this.instagramAccountId = instagramAccountId;
        this.threadsAccountId = threadsAccountId;
    }

    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;
}
