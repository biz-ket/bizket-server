package com.bizket.marketing.api.service;

import com.bizket.marketing.api.service.response.ContentResponse;
import com.bizket.marketing.domain.marketingcontent.MarketingContent;
import com.bizket.marketing.domain.marketingcontent.repository.MarketingContentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.bizket.exception.BizExceptionType.BAD_REQUEST;

@RequiredArgsConstructor
@Service
public class MarketingContentService {

    private final MarketingContentRepository marketingContentRepository;

    public List<ContentResponse> getContents(Long memberId, String clientToken) {
        List<MarketingContent> contents = marketingContentRepository.findByMemberIdOrClientToken(memberId, clientToken);
        return contents.stream()
            .map(ContentResponse::of)
            .toList();
    }

    public ContentResponse getById(Long id) {
        MarketingContent content = marketingContentRepository.findById(id)
            .orElseThrow(() -> BAD_REQUEST.of("콘텐츠를 찾을 수 없습니다."));
        return ContentResponse.of(content);
    }
}
