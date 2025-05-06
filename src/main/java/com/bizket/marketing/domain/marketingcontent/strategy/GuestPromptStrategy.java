package com.bizket.marketing.domain.marketingcontent.strategy;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;

public class GuestPromptStrategy implements PromptStrategy {

    @Override
    public String createPrompt(BaseMarketingContentRequest request) {
        return String.format("""
                - 사용자 요청: %s
                - 강조할 요소: %s
                """,
            request.prompt(),
            String.join(", ", request.emphasisTags())
        );
    }
}
