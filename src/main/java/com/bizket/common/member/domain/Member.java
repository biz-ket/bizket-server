package com.bizket.common.member.domain;

import com.bizket.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nickname;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "oauth2_provider", nullable = false)
    private String oauth2Provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

//    @Column(name = "business_place_id")
//    private Long businessPlaceId;
//
//    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
//    private Boolean isDeleted;
}
