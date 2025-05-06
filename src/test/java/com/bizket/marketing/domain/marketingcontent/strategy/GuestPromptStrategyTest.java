//package com.bizket.marketing.domain.marketingcontent.strategy;
//
//import com.bizket.marketing.api.dto.request.GuestMarketingContentRequest;
//import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class GuestPromptStrategyTest {
//
//    private final PromptStrategy strategy = new GuestPromptStrategy();
//
//    @DisplayName("비로그인 사용자 요청에 대한 프롬프트 문장 생성")
//    @Test
//    void createGuestPrompt() {
//        // given
//        GuestMarketingContentRequest request = new GuestMarketingContentRequest(
//            "짧고 유쾌하게 써줘",
//            List.of(KeywordType.DESIGN, KeywordType.PRICE),
//            List.of("image1.jpg", "image2.jpg")
//        );
//
//        // when
//        String result = strategy.createPrompt(request);
//
//        // then
//        assertThat(result).contains("짧고 유쾌하게");
//        assertThat(result).contains("디자인, 가격");
//    }
//}
