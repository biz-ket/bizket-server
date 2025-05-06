package com.bizket.marketing.domain.marketingcontent.strategy;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.api.dto.request.BusinessMarketingContentRequest;
import java.util.stream.Collectors;

public class BusinessPromptStrategy implements PromptStrategy {

    @Override
    public String createPrompt(BaseMarketingContentRequest request) {
        BusinessMarketingContentRequest business = (BusinessMarketingContentRequest) request;
        String tags = request.emphasisTags().stream()
            .map(tag -> tag.getDescription())
            .collect(Collectors.joining(", "));

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
            String.join(", ", tags)
        );
    }
}

