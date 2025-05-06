//package com.bizket.marketing.builder;
//
//import com.bizket.marketing.api.dto.request.MemberMarketingContentRequest;
//import com.bizket.marketing.domain.clova.ClovaMessage;
//import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class MarketingContentBuilderTest {
//
//    private final MarketingContentBuilder builder = new MarketingContentBuilder();
//
//    @DisplayName("프롬프트와 이미지가 포함된 Clova 메시지를 생성")
//    @Test
//    void shouldCreateClovaMessageWithPromptAndImages() {
//        MemberMarketingContentRequest request = new MemberMarketingContentRequest(
//            "INSTAGRAM",
//            "감성적인 문구로 작성해줘",
//            List.of(KeywordType.DESIGN, KeywordType.PRICE),
//            List.of("image1.jpg", "image2.jpg")
//        );
//
//        List<ClovaMessage> messages = builder.build(request);
//
//        assertThat(messages).hasSize(2);
//
//        ClovaMessage system = messages.get(0);
//        assertThat(system.role()).isEqualTo("system");
//        assertThat(system.content().get(0).text()).contains("마케팅 콘텐츠 전문가");
//
//        ClovaMessage user = messages.get(1);
//        assertThat(user.role()).isEqualTo("user");
//        assertThat(user.content()).anySatisfy(c ->
//            assertThat(c.text()).contains("감성적인 문구")
//        );
//        assertThat(user.content()).filteredOn(c -> "image_url".equals(c.type()))
//            .hasSize(2)
//            .allSatisfy(c -> assertThat(c.imageUrl().url()).startsWith("image"));
//    }
//
//    @DisplayName("이미지가 없을 때 텍스트 메시지만 생성")
//    @Test
//    void shouldCreateTextOnlyMessage() {
//        MemberMarketingContentRequest request = new MemberMarketingContentRequest(
//            "THREADS",
//            "짧고 간결하게",
//            List.of(KeywordType.DESIGN),
//            List.of()
//        );
//
//        List<ClovaMessage> messages = builder.build(request);
//
//        ClovaMessage user = messages.get(1);
//        assertThat(user.content()).hasSize(1);
//        assertThat(user.content().get(0).type()).isEqualTo("text");
//    }
//}
