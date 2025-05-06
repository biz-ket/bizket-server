package com.bizket.marketing.api.controller;

import com.bizket.common.dto.Response;
import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.api.dto.response.ContentResponse;
import com.bizket.marketing.application.service.MarketingContentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/marketing/contents")
@RestController
public class MarketingContentController {

    private final MarketingContentService contentService;

    @PostMapping
    public Response<ContentResponse> createContent(@RequestBody BaseMarketingContentRequest request) {
        Long memberId = 1L;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            memberId = Long.valueOf(authentication.getName());
        }

        return Response.of(contentService.createContent(request, memberId));
    }

    @GetMapping
    public Response<List<ContentResponse>> getDummyContents(
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) String clientToken
    ) {
        return Response.of(contentService.getContents(memberId, clientToken));
    }

    @GetMapping("/{id}")
    public Response<ContentResponse> getOne(@PathVariable Long id) {
        return Response.of(contentService.getById(id));
    }

}
