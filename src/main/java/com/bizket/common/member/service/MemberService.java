package com.bizket.common.member.service;


import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.common.member.domain.Member;
import com.bizket.insight.dto.BusinessProfileDto;
import com.bizket.insight.dto.BusinessProfileResponse;
import com.bizket.insight.dto.MemberDto;
import com.bizket.insight.dto.MemberPatchDto;
import com.bizket.common.member.dto.MessageResponse;
import com.bizket.insight.dto.MypageDto;
import com.bizket.common.member.repository.MemberRepository;
import com.bizket.insight.domain.BusinessCategory;
import com.bizket.insight.domain.BusinessDetailCategory;
import com.bizket.insight.domain.BusinessPlace;
import com.bizket.insight.domain.BusinessSubCategory;
import com.bizket.insight.domain.CustomerAgeGroup;
import com.bizket.insight.repository.BusinessCategoryRepository;
import com.bizket.insight.repository.BusinessPlaceRepository;
import com.bizket.insight.repository.BusinessSubCategoryRepository;
import com.bizket.insight.service.InstagramInsightService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bizket.insight.repository.CustomerAgeGroupRepository;
import com.bizket.insight.repository.BusinessDetailCategoryRepository;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final BusinessPlaceRepository businessPlaceRepository;
    private final InstagramInsightService instagramInsightService;
    private final CustomerAgeGroupRepository ageRepo;
    private final BusinessDetailCategoryRepository detailRepo;
    private final BusinessSubCategoryRepository subCategoryRepo;
    private final BusinessCategoryRepository categoryRepo;

    private Member getMemberFromToken(String jwtToken) {
        String memberIdStr = jwtTokenProvider.getMemberId(jwtToken);
        return memberRepository.findById(Long.parseLong(memberIdStr))
            .orElseThrow(() -> new IllegalStateException("멤버를 찾을 수 없습니다."));
    }

    public MemberDto getMyMemberInfo(String jwtToken) {
        Member member = getMemberFromToken(jwtToken);
        return MemberDto.of(member);
    }

    public Object getMyBusinessProfile(String jwtToken) {
        Member member = getMemberFromToken(jwtToken);
        BusinessPlace businessPlace = member.getBusinessPlace();
        if (businessPlace == null) {
            return new MessageResponse("아직 등록된 사업장 정보가 없습니다.");
        }

        int followerCount = instagramInsightService.getFollowerCount(jwtToken);
        return BusinessProfileResponse.of(member, businessPlace, followerCount);
    }


    public MypageDto getMyMypageInfo(String jwtToken) {
        Member member = getMemberFromToken(jwtToken);
        BusinessPlace businessPlace = member.getBusinessPlace();

        return MypageDto.of(member, businessPlace);
    }


    @Transactional
    public void updateBusinessPlace(String jwtToken, BusinessProfileDto dto) {
        Member member = getMemberFromToken(jwtToken);
        BusinessPlace place = member.getBusinessPlace();
        if (place == null) {
            throw new IllegalStateException("사업장이 존재하지 않습니다.");
        }

        CustomerAgeGroup ageGroup = null;
        if (dto.customerAgeGroupId() != null) {
            ageGroup = ageRepo.findById(dto.customerAgeGroupId())
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 연령대입니다."));
        }

        // (2) 대분류 조회
        BusinessCategory category = categoryRepo.findById(dto.businessCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("대분류가 없습니다."));

        // (3) 중분류 조회 & 검증
        BusinessSubCategory sub = subCategoryRepo.findById(dto.businessSubCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("중분류가 없습니다."));
        if (!sub.getParentCategory().getId().equals(category.getId())) {
            throw new IllegalArgumentException("중분류가 대분류에 속하지 않습니다.");
        }

        // (4) 소분류 조회 & 검증
        BusinessDetailCategory detail = detailRepo.findById(dto.businessDetailCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("소분류가 없습니다."));
        if (!detail.getSubCategory().getId().equals(sub.getId())) {
            throw new IllegalArgumentException("소분류가 중분류에 속하지 않습니다.");
        }

        place.update(dto, ageGroup, category, sub, detail);
    }


    @Transactional
    public void updateMemberNickname(String jwtToken, String newNickname) {
        Member member = getMemberFromToken(jwtToken);
        member.updateNickname(newNickname);
    }

    @Transactional
    public void createBusinessPlace(String jwt, BusinessProfileDto dto) {
        Member member = getMemberFromToken(jwt);

        // (1) 연령대
        CustomerAgeGroup ageGroup = dto.customerAgeGroupId() == null ? null :
            ageRepo.findById(dto.customerAgeGroupId())
                .orElseThrow(() -> new IllegalArgumentException("연령대가 없습니다."));

        // (2) 대분류 조회
        BusinessCategory category = categoryRepo.findById(dto.businessCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("대분류가 없습니다."));

        // (3) 중분류 조회 & 검증
        BusinessSubCategory sub = subCategoryRepo.findById(dto.businessSubCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("중분류가 없습니다."));
        if (!sub.getParentCategory().getId().equals(category.getId())) {
            throw new IllegalArgumentException("중분류가 대분류에 속하지 않습니다.");
        }

        // (4) 소분류 조회 & 검증
        BusinessDetailCategory detail = detailRepo.findById(dto.businessDetailCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("소분류가 없습니다."));
        if (!detail.getSubCategory().getId().equals(sub.getId())) {
            throw new IllegalArgumentException("소분류가 중분류에 속하지 않습니다.");
        }

        // (5) 엔티티 빌드/저장
        BusinessPlace place = BusinessPlace.builder()
            .placeName(dto.placeName())
            .customerAgeGroup(ageGroup)
            .businessCategory(category)             // 엔티티에 필드 추가 필요
            .businessSubCategory(sub)              // 엔티티에 필드 추가 필요
            .businessDetailCategory(detail)
            .openDate(dto.openDate())
            .address(dto.address())
            .placeEmail(dto.placeEmail())
            .placePhoneNumber(dto.placePhoneNumber())
            .build();
        businessPlaceRepository.save(place);
        member.updateBusinessPlace(place);
    }


    @Transactional
    public void deleteBusinessPlace(String jwtToken) {
        Member member = getMemberFromToken(jwtToken);
        BusinessPlace businessPlace = member.getBusinessPlace();
        if (businessPlace != null) {
            businessPlaceRepository.delete(businessPlace);
            member.updateBusinessPlace(null);
        }
    }

    @Transactional
    public void updateMemberInfo(String jwtToken, MemberPatchDto dto) {
        Member member = getMemberFromToken(jwtToken);
        member.updateInfo(dto);
    }
}
