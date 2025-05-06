package com.bizket.marketing.application.service;

import com.bizket.common.member.domain.Member;
import com.bizket.common.member.repository.MemberRepository;
import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.api.dto.request.GuestMarketingContentRequest;
import com.bizket.marketing.api.dto.response.ContentResponse;
import com.bizket.marketing.api.dto.response.clova.ClovaResult;
import com.bizket.marketing.builder.MarketingContentBuilder;
import com.bizket.marketing.domain.clova.ClovaMessage;
import com.bizket.marketing.domain.hashtag.model.Hashtag;
import com.bizket.marketing.domain.hashtag.repository.HashtagRepository;
import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import com.bizket.marketing.domain.marketingcontent.repository.MarketingContentRepository;
import com.bizket.marketing.domain.marketingkeyword.model.MarketingKeyword;
import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import com.bizket.marketing.domain.marktingimage.model.MarketingImage;
import com.bizket.marketing.infrastructure.clova.ClovaApiClient;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.bizket.exception.BizExceptionType.BAD_REQUEST;

@Slf4j
@RequiredArgsConstructor
@Service
public class MarketingContentService {

    private final MarketingContentBuilder builder;
    private final ClovaApiClient clovaApiClient;
    private final HashtagRepository hashtagRepository;
    private final MarketingContentRepository marketingContentRepository;
    private final MemberRepository memberRepository; // 추가됨

    public ContentResponse createContent(BaseMarketingContentRequest request, Long memberId) {
        List<ClovaMessage> messages = builder.build(request);
        ClovaResult clovaResult = clovaApiClient.send(messages);

        List<Hashtag> hashtags = clovaResult.hashtags().stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(tag -> hashtagRepository.findByName(tag)
                .orElseGet(() -> hashtagRepository.save(Hashtag.of(tag))))
            .toList();

        MarketingContent content = createContent(request, clovaResult.marketingContent(), hashtags, memberId);
        buildImages(request.imageUrls(), content);

        MarketingContent saved = marketingContentRepository.save(content);
        return ContentResponse.of(saved);
    }

    private MarketingContent createContent(BaseMarketingContentRequest request, String generatedText,
        List<Hashtag> hashtags, Long memberId) {
        MarketingContent content = MarketingContent.of(
            request.platform(),
            request.prompt(),
            generatedText,
            hashtags
        );

        if (request.userType().isGuest()) {
            String clientToken = ((GuestMarketingContentRequest) request).clientToken();
            content.assignClientToken(clientToken);
        } else if (request.userType().isMember() || request.userType().isBusiness()) {
            Member member = memberRepository.getReferenceById(memberId);
            content.assignMember(member);
        }

        toKeywords(request.emphasisTags()).forEach(content::addKeyword);
        return content;
    }

    public List<ContentResponse> getContents(Long memberId, String clientToken) {
        return marketingContentRepository.findByMemberIdOrClientToken(memberId, clientToken)
            .stream()
            .map(ContentResponse::of)
            .toList();
    }

    public ContentResponse getById(Long id) {
        return marketingContentRepository.findById(id)
            .map(ContentResponse::of)
            .orElseThrow(() -> BAD_REQUEST.of("콘텐츠를 찾을 수 없습니다."));
    }

    private List<MarketingKeyword> toKeywords(List<KeywordType> tags) {
        return tags.stream().map(MarketingKeyword::of).toList();
    }

    private void buildImages(List<String> imageUrls, MarketingContent content) {
        IntStream.range(0, imageUrls.size())
            .mapToObj(i -> {
                MarketingImage image = MarketingImage.of(imageUrls.get(i), i);
                image.assignContent(content);
                return image;
            })
            .forEach(content::addImage);
    }
}
