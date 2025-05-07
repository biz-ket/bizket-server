package com.bizket.marketing.api.controller;

import com.bizket.common.dto.Response;
import com.bizket.marketing.api.dto.request.MarketingContentRequest;
import com.bizket.marketing.api.dto.response.ContentResponse;
import com.bizket.marketing.application.service.MarketingContentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/marketing/contents")
@RestController
public class MarketingContentController {

    private final MarketingContentService contentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<ContentResponse> createContentWithFile(
        @RequestPart(value = "request", required = true) MarketingContentRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws JsonProcessingException {
        return Response.of(contentService.createContent(request, images));
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
