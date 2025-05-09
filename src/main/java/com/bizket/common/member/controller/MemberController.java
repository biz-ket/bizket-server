package com.bizket.common.member.controller;


import com.bizket.insight.dto.BusinessProfileDto;
import com.bizket.insight.dto.MemberDto;
import com.bizket.insight.dto.MemberPatchDto;
import com.bizket.common.member.dto.MessageResponse;
import com.bizket.insight.dto.MypageDto;
import com.bizket.common.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/member/me")
    public ResponseEntity<MemberDto> getMyInfo(HttpServletRequest request) {
        String jwtToken = null;
        try {
            jwtToken = resolveJwt(request);
        } catch (IllegalArgumentException e) {
            // JWT 없으면 null 로 반환
            return ResponseEntity.ok(
                new MemberDto(null, null, null, null, null, null)
            );
        }

        MemberDto response = memberService.getMyMemberInfo(jwtToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/business-report/me/profile")
    public ResponseEntity<?> getMyBusinessProfile(HttpServletRequest request) {
        String jwtToken = resolveJwt(request);
        Object response = memberService.getMyBusinessProfile(jwtToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mypage/me")
    public ResponseEntity<MypageDto> getMyMypage(HttpServletRequest request) {
        String jwtToken = resolveJwt(request);
        MypageDto mypage = memberService.getMyMypageInfo(jwtToken);
        return ResponseEntity.ok(mypage);
    }

    @PatchMapping("/member/me/nickname")
    public ResponseEntity<Void> updateNickname(HttpServletRequest request, @RequestParam String nickname) {
        String jwtToken = resolveJwt(request);
        memberService.updateMemberNickname(jwtToken, nickname);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/business-report/me")
    public ResponseEntity<Void> updateBusinessPlace(
        HttpServletRequest request,
        @RequestBody BusinessProfileDto dto
    ) {
        String jwt = resolveJwt(request);
        memberService.updateBusinessPlace(jwt, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/business-report/me")
    public ResponseEntity<Void> createBusinessPlace(
        HttpServletRequest request,
        @RequestBody BusinessProfileDto dto
    ) {
        String jwt = resolveJwt(request);
        memberService.createBusinessPlace(jwt, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/business-report/me")
    public ResponseEntity<Void> deleteBusinessPlace(HttpServletRequest request) {
        String jwtToken = resolveJwt(request);
        memberService.deleteBusinessPlace(jwtToken);
        return ResponseEntity.ok().build();
    }

    private String resolveJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("JWT 토큰이 없습니다.");
    }

    @PatchMapping("/member/me")
    public ResponseEntity<Void> updateMemberInfo(
        HttpServletRequest request,
        @RequestBody MemberPatchDto dto
    ) {
        String jwtToken = resolveJwt(request);
        memberService.updateMemberInfo(jwtToken, dto);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<MessageResponse> handleBadRequest(Exception ex) {
        return ResponseEntity
            .badRequest()
            .body(new MessageResponse(ex.getMessage()));
    }
}
