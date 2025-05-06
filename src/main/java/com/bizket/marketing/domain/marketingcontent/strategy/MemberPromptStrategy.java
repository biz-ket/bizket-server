package com.bizket.marketing.domain.marketingcontent.strategy;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.api.dto.request.MemberMarketingContentRequest;
import java.util.stream.Collectors;

public class MemberPromptStrategy implements PromptStrategy {

    @Override
    public String createPrompt(BaseMarketingContentRequest request) {
        MemberMarketingContentRequest member = (MemberMarketingContentRequest) request;
        String tags = request.emphasisTags().stream()
            .map(tag -> tag.getDescription())
            .collect(Collectors.joining(", "));

        return String.format("""
                - 플랫폼: %s
                - 사용자 요청: %s
                - 강조할 요소: %s
                """,
            member.platform(),
            member.prompt(),
            String.join(", ", tags)
        );
    }
}
