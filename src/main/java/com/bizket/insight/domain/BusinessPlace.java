package com.bizket.insight.domain;

import com.bizket.common.member.domain.Member;
import com.bizket.common.member.dto.BusinessProfileDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "business_place")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_name", nullable = false, length = 255)
    private String placeName;

    @Column(name = "business_info", nullable = false, length = 1000)
    private String businessInfo;

    @Column(name = "customer_age_group", nullable = true, length = 255)
    private String customerAgeGroup;

    @Column(name = "open_date", nullable = true)
    private String openDate;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "place_email", nullable = true, length = 255)
    private String placeEmail;

    @Column(name = "place_phone_number", nullable = true, length = 50)
    private String placePhoneNumber;
    @OneToOne(mappedBy = "businessPlace")
    private Member member;

    public void update(BusinessProfileDto dto) {
        if (dto.placeName() != null) this.placeName = dto.placeName();
        if (dto.businessInfo() != null) this.businessInfo = dto.businessInfo();
        if (dto.customerAgeGroup() != null) this.customerAgeGroup = dto.customerAgeGroup();
        if (dto.openDate() != null) this.openDate = dto.openDate();
        if (dto.address() != null) this.address = dto.address();
        if (dto.placeEmail() != null) this.placeEmail = dto.placeEmail();
        if (dto.placePhoneNumber() != null) this.placePhoneNumber = dto.placePhoneNumber();
    }
}
