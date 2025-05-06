package com.bizket.marketing.domain.marketingcontent.strategy;

import com.bizket.marketing.api.dto.request.BusinessMarketingContentRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessPromptStrategyTest {

    private final PromptStrategy strategy = new BusinessPromptStrategy();

    @DisplayName("비즈니스 사용자 요청에 대한 프롬프트 문장 생성")
    @Test
    void createBusinessPrompt() {
        // given
        BusinessMarketingContentRequest request = new BusinessMarketingContentRequest(
            "뷰티하우스", "@beautyhouse", "뷰티", "20대 여성",
            "INSTAGRAM", "감성적인 톤으로 써줘", List.of("디자인", "퀄리티")
        );

        // when
        String result = strategy.createPrompt(request);

        // then
        assertThat(result).contains("뷰티하우스");
        assertThat(result).contains("INSTAGRAM");
        assertThat(result).contains("디자인");
        assertThat(result).contains("감성적인 톤");
    }
}
