package com.bizket.marketing.domain.marketingcontent.strategy;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import java.util.stream.Collectors;

public class GuestPromptStrategy implements PromptStrategy {

    @Override
    public String createPrompt(BaseMarketingContentRequest request) {
        String tags = request.emphasisTags().stream()
            .map(tag -> tag.getDescription())
            .collect(Collectors.joining(", "));

        return String.format("""
                - 사용자 요청: %s
                - 강조할 요소: %s
                """,
            request.prompt(), tags
        );
    }
}
