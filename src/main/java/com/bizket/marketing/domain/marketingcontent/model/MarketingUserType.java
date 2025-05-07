package com.bizket.marketing.domain.marketingcontent.model;

import com.bizket.marketing.api.dto.request.MarketingContentRequest;
import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import java.util.List;
import java.util.stream.Collectors;

public enum MarketingUserType {

    GUEST {
        @Override
        public String createPrompt(MarketingContentRequest request) {
            return """
                - 사용자 요청: %s
                - 강조할 키워드: %s
                """.formatted(
                request.prompt(),
                formatTags(request.emphasisTags())
            );
        }
    },

    MEMBER {
        @Override
        public String createPrompt(MarketingContentRequest request) {
            return """
                - 브랜드: %s
                - 요청 내용: %s
                - 강조 키워드: %s
                """.formatted(
                request.brandName(),
                request.prompt(),
                formatTags(request.emphasisTags())
            );
        }
    },

    BUSINESS {
        @Override
        public String createPrompt(MarketingContentRequest request) {
            return """
                - 브랜드: %s
                - 요청 내용: %s
                - 강조 키워드: %s
                """.formatted(
                request.brandName(),
                request.prompt(),
                formatTags(request.emphasisTags())
            );
        }
    };

    public abstract String createPrompt(MarketingContentRequest request);

    protected String formatTags(List<KeywordType> tags) {
        return tags.stream()
            .map(KeywordType::getDescription)
            .collect(Collectors.joining(", "));
    }

    public boolean isGuest() {
        return this == GUEST;
    }

    public boolean isMember() {
        return this == MEMBER;
    }

    public boolean isBusiness() {
        return this == BUSINESS;
    }
}
