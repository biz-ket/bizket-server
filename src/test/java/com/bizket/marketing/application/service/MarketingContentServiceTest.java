//package com.bizket.marketing.application.service;
//
//import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
//import com.bizket.marketing.api.dto.request.GuestMarketingContentRequest;
//import com.bizket.marketing.builder.MarketingContentBuilder;
//import com.bizket.marketing.domain.clova.ClovaMessage;
//import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
//import com.bizket.marketing.domain.marketingcontent.repository.MarketingContentRepository;
//import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
//import com.bizket.marketing.infrastructure.clova.ClovaApiClient;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.mockito.BDDMockito.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.mock;
//import static org.mockito.BDDMockito.then;
//
//@ActiveProfiles("test")
//@ExtendWith(MockitoExtension.class)
//class MarketingContentServiceTest {
//
//    private MarketingContentBuilder builder;
//    private ClovaApiClient clovaApiClient;
//    private MarketingContentRepository repository;
//    private MarketingContentService service;
//
//    @BeforeEach
//    void setUp() {
//        builder = mock(MarketingContentBuilder.class);
//        clovaApiClient = mock(ClovaApiClient.class);
//        repository = mock(MarketingContentRepository.class);
//        service = new MarketingContentService(builder, clovaApiClient, repository);
//    }
//
//    @Test
//    @DisplayName("비로그인 사용자 콘텐츠 생성 성공")
//    void createContent_GuestUser() {
//        // given
//        BaseMarketingContentRequest request = new GuestMarketingContentRequest(
//            "감성적으로 써줘",
//            List.of(KeywordType.DESIGN, KeywordType.PRICE),
//            List.of("image1.jpg", "image2.jpg")
//        );
//
//        List<ClovaMessage> mockMessages = List.of();
//        given(builder.build(request)).willReturn(mockMessages);
//
//        String clovaResponse = """
//            1. 감성적인 문구 예시입니다.
//            2. 감성, 디자인
//            """;
//
//        given(clovaApiClient.send(mockMessages)).willReturn(clovaResponse);
//
//        MarketingContent mockContent = mock(MarketingContent.class);
//        given(repository.save(any(MarketingContent.class))).willReturn(mockContent);
//
//        // when
//        service.createContent(request);
//
//        // then
//        then(builder).should().build(request);
//        then(clovaApiClient).should().send(mockMessages);
//        then(repository).should().save(any(MarketingContent.class));
//    }
//}
