package com.bizket.marketing.api.controller;

import com.bizket.common.dto.Response;
import com.bizket.marketing.api.dto.request.MarketingContentRequest;
import com.bizket.marketing.api.dto.response.ContentResponse;
import com.bizket.marketing.application.service.MarketingContentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
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


//    @GetMapping
//    public Response<List<ContentResponse>> getDummyContents(
//        @RequestParam(required = false) Long memberId,
//        @RequestParam(required = false) String clientToken
//    ) {
//        return Response.of(contentService.getContents(memberId, clientToken));
//    }

    @GetMapping
    public ResponseEntity<Response<?>> getContents(
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) String clientToken,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasPaging  = (page != null || size != null);

        Object result;
        if (hasKeyword) {
            // 1) 검색(페이징 유무 따라)
            Pageable pageable = hasPaging
                ? PageRequest.of(page != null ? page : 0, size != null ? size : 10)
                : Pageable.unpaged();
            result = contentService.searchContents(memberId, clientToken, keyword.trim(), pageable);
        }
        else if (!hasPaging) {
            // 2) 전체 리스트
            result = contentService.getAllContents(memberId, clientToken);
        }
        else {
            // 3) 전체 페이징
            Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10);
            result = contentService.searchContents(memberId, clientToken, null, pageable);
        }

        var body = Response.of(result);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body);
    }

    @GetMapping("/{id}")
    public Response<ContentResponse> getOne(@PathVariable Long id) {
        return Response.of(contentService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(Response.of(null, "마케팅 콘텐츠가 삭제되었습니다."));
    }
}
