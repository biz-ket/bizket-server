//package com.bizket.marketing.domain.marketingcontent.strategy;
//
//import com.bizket.marketing.api.dto.request.MemberMarketingContentRequest;
//import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class MemberPromptStrategyTest {
//
//    private final PromptStrategy strategy = new MemberPromptStrategy();
//
//    @DisplayName("Member 요청으로부터 프롬프트 문장을 생성한다")
//    @Test
//    void createMemberPrompt() {
//        // given
//        MemberMarketingContentRequest request = new MemberMarketingContentRequest(
//            "INSTAGRAM",
//            "트렌디하고 임팩트 있게 작성해줘",
//            List.of(KeywordType.DESIGN, KeywordType.PRICE),
//            List.of("image1.jpg", "image2.jpg")
//        );
//
//        // when
//        String result = strategy.createPrompt(request);
//
//        // then
//        assertThat(result).contains("INSTAGRAM");
//        assertThat(result).contains("트렌디하고 임팩트 있게 작성해줘");
//        assertThat(result).contains("디자인, 가격");
//    }
//}
