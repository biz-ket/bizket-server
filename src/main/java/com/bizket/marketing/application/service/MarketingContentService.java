package com.bizket.marketing.application.service;

import com.bizket.common.member.domain.Member;
import com.bizket.common.member.repository.MemberRepository;
import com.bizket.exception.BizExceptionType;
import com.bizket.firebase.FirebaseStorageService;
import com.bizket.marketing.api.dto.request.MarketingContentRequest;
import com.bizket.marketing.api.dto.response.ContentResponse;
import com.bizket.marketing.api.dto.response.clova.ClovaResult;
import com.bizket.marketing.builder.MarketingContentBuilder;
import com.bizket.marketing.domain.clova.ClovaMessage;
import com.bizket.marketing.domain.hashtag.model.Hashtag;
import com.bizket.marketing.domain.hashtag.repository.HashtagRepository;
import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import com.bizket.marketing.domain.marketingcontent.repository.MarketingContentRepository;
import com.bizket.marketing.domain.marketingkeyword.model.MarketingKeyword;
import com.bizket.marketing.domain.marktingimage.model.MarketingImage;
import com.bizket.marketing.infrastructure.clova.ClovaApiClient;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.bizket.exception.BizExceptionType.BAD_REQUEST;

@RequiredArgsConstructor
@Service
public class MarketingContentService {

    private final MarketingContentBuilder builder;
    private final ClovaApiClient clovaApiClient;
    private final HashtagRepository hashtagRepository;
    private final MarketingContentRepository marketingContentRepository;
    private final MemberRepository memberRepository;
    private final FirebaseStorageService firebaseStorageService;

    public ContentResponse createContent(MarketingContentRequest request, List<MultipartFile> images) {
        List<String> imageUrls = firebaseStorageService.uploadAll(images);

        MarketingContentRequest enrichedRequest = request.withImageUrls(imageUrls);

        List<ClovaMessage> messages = builder.build(enrichedRequest);
        ClovaResult clovaResult = clovaApiClient.send(messages);

        List<Hashtag> hashtags = clovaResult.hashtags().stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(tag -> hashtagRepository.findByName(tag)
                .orElseGet(() -> hashtagRepository.save(Hashtag.of(tag))))
            .toList();

        MarketingContent content = MarketingContent.of(
            enrichedRequest.platform(),
            enrichedRequest.prompt(),
            clovaResult.marketingContent(),
            hashtags
        );

        switch (enrichedRequest.userType().toUpperCase()) {
            case "GUEST" -> content.assignClientToken(enrichedRequest.clientToken());
            case "MEMBER", "BUSINESS" -> {
                Long memberId = enrichedRequest.memberId();
                if (memberId == null) {
                    throw BAD_REQUEST.of("회원 ID가 누락되었습니다.");
                }
                Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> BAD_REQUEST.of("회원 정보를 찾을 수 없습니다."));
                content.assignMember(member);
            }
            default -> throw BAD_REQUEST.of("지원하지 않는 사용자 유형입니다.");
        }

        enrichedRequest.emphasisTags().stream()
            .map(MarketingKeyword::of)
            .forEach(content::addKeyword);

        buildImages(imageUrls, content);

        MarketingContent saved = marketingContentRepository.save(content);
        return ContentResponse.of(saved);
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

    public List<ContentResponse> getContents(Long memberId, String clientToken) {
        return marketingContentRepository.findByMemberIdOrClientToken(memberId, clientToken)
            .stream()
            .map(ContentResponse::of)
            .toList();
    }

    public List<ContentResponse> getAllContents(Long memberId, String clientToken) {
        return marketingContentRepository
            .findByMemberIdOrClientTokenOrderByCreatedAtDesc(memberId, clientToken)
            .stream()
            .map(ContentResponse::of)
            .toList();
    }


    /**
     * 페이징 + 검색어(prompt, generatedContent, hashtag.name) 처리
     * - pageable.isUnpaged(): page/size 파라미터가 없었다고 간주
     */
    public Page<ContentResponse> searchContents(
        Long memberId,
        String clientToken,
        String keyword,
        Pageable pageable
    ) {
        // 전체 리스트 모드
        if (pageable.isUnpaged()) {
            // 검색어 없는 전체
            if (keyword == null || keyword.isBlank()) {
                List<ContentResponse> list = getAllContents(memberId, clientToken);
                return new PageImpl<>(list);
            }
            // 검색어 있는 전체
            Page<MarketingContent> filtered =
                marketingContentRepository.searchByPromptContentOrHashtag(
                    memberId, clientToken, keyword, Pageable.unpaged());
            return filtered.map(ContentResponse::of);
        }

        // 페이징 모드
        if (keyword == null || keyword.isBlank()) {
            return marketingContentRepository
                .findByMemberIdOrClientToken(memberId, clientToken, pageable)
                .map(ContentResponse::of);
        } else {
            return marketingContentRepository
                .searchByPromptContentOrHashtag(memberId, clientToken, keyword, pageable)
                .map(ContentResponse::of);
        }
    }

    public ContentResponse getById(Long id) {
        return marketingContentRepository.findById(id)
            .map(ContentResponse::of)
            .orElseThrow(() -> BAD_REQUEST.of("콘텐츠를 찾을 수 없습니다."));
    }

    public void deleteContent(Long id) {
        MarketingContent content = marketingContentRepository.findById(id)
            .orElseThrow(() -> BizExceptionType.BAD_REQUEST.of("삭제할 콘텐츠를 찾을 수 없습니다."));
        marketingContentRepository.delete(content);
    }
}
