package com.bizket.insight.domain;

import com.bizket.common.member.domain.Member;
import com.bizket.insight.dto.BusinessProfileDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    // 대분류
    @ManyToOne
    @JoinColumn(name = "business_category_id", nullable = false)
    private BusinessCategory businessCategory;

    // 중분류
    @ManyToOne
    @JoinColumn(name = "business_sub_category_id", nullable = false)
    private BusinessSubCategory businessSubCategory;
    
    // 소분류
    @ManyToOne
    @JoinColumn(name = "business_detail_category_id", nullable = true)
    private BusinessDetailCategory businessDetailCategory;

    @ManyToOne
    @JoinColumn(name = "customer_age_group_id", nullable = true)
    private CustomerAgeGroup customerAgeGroup;

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

    public void update(
        BusinessProfileDto dto,
        CustomerAgeGroup ageGroup,
        BusinessCategory category,
        BusinessSubCategory subCategory,
        BusinessDetailCategory detailCategory
    ) {
        if (dto.placeName() != null)        this.placeName = dto.placeName();
        if (dto.openDate() != null)         this.openDate = dto.openDate();
        if (dto.address() != null)          this.address = dto.address();
        if (dto.placeEmail() != null)       this.placeEmail = dto.placeEmail();
        if (dto.placePhoneNumber() != null) this.placePhoneNumber = dto.placePhoneNumber();

        if (ageGroup != null)          this.customerAgeGroup    = ageGroup;
        if (category != null)          this.businessCategory    = category;
        if (subCategory != null)       this.businessSubCategory = subCategory;
        if (detailCategory != null)    this.businessDetailCategory = detailCategory;
    }
}
