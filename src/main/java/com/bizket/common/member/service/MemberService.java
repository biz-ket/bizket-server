package com.bizket.common.member.service;


import com.bizket.auth.jwt.JwtTokenProvider;
import com.bizket.common.member.domain.Member;
import com.bizket.common.member.dto.BusinessProfileDto;
import com.bizket.common.member.dto.BusinessProfileResponse;
import com.bizket.common.member.dto.MemberDto;
import com.bizket.common.member.dto.MessageResponse;
import com.bizket.common.member.dto.MypageDto;
import com.bizket.common.member.repository.MemberRepository;
import com.bizket.insight.domain.BusinessPlace;
import com.bizket.insight.repository.BusinessPlaceRepository;
import com.bizket.insight.service.InstagramInsightService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final BusinessPlaceRepository businessPlaceRepository;
    private final InstagramInsightService instagramInsightService;

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
    public void updateMemberNickname(String jwtToken, String newNickname) {
        Member member = getMemberFromToken(jwtToken);
        member.updateNickname(newNickname);
    }

    @Transactional
    public void updateBusinessPlace(String jwtToken, BusinessProfileDto dto) {
        Member member = getMemberFromToken(jwtToken);
        BusinessPlace businessPlace = member.getBusinessPlace();
        if (businessPlace == null) {
            throw new IllegalStateException("사업장이 존재하지 않습니다.");
        }
        businessPlace.update(dto);
    }

    @Transactional
    public void createBusinessPlace(String jwtToken, BusinessProfileDto dto) {
        Member member = getMemberFromToken(jwtToken);
        BusinessPlace newPlace = dto.toEntity();
        businessPlaceRepository.save(newPlace);
        member.updateBusinessPlace(newPlace);
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
    public void updateMemberInfo(String jwtToken, MemberDto dto) {
        Member member = getMemberFromToken(jwtToken);
        member.updateInfo(dto);
    }
}
