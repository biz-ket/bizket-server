package com.bizket.marketing.domain.marketingcontent.strategy;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.api.dto.request.BusinessMarketingContentRequest;

public class BusinessPromptStrategy implements PromptStrategy {

    @Override
    public String createPrompt(BaseMarketingContentRequest request) {
        BusinessMarketingContentRequest business = (BusinessMarketingContentRequest) request;
        return String.format("""
                - 상호명: %s
                - 계정: %s
                - 업종: %s
                - 고객 연령층: %s
                - 플랫폼: %s
                - 강조할 요소: %s
                - 사용자 요청: %s
                """,
            business.brandName(),
            business.account(),
            business.industry(),
            business.targetAgeGroup(),
            business.platform(),
            business.prompt(),
            String.join(", ", business.emphasisTags())
        );
    }
}

