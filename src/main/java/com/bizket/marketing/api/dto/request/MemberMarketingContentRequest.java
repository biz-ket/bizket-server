package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import java.util.List;
import java.util.Objects;

public record MemberMarketingContentRequest(
    Long memberId,
    String prompt,
    String platform,
    List<KeywordType> emphasisTags,
    List<String> rawImageUrls
) implements BaseMarketingContentRequest {

    @Override
    public MarketingUserType userType() {
        return MarketingUserType.MEMBER;
    }

    @Override
    public List<String> imageUrls() {
        return Objects.requireNonNullElse(rawImageUrls, List.of());
    }
}
