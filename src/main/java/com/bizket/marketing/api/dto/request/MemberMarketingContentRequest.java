package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import java.util.List;
import java.util.Objects;

public record MemberMarketingContentRequest(
    String prompt,
    String platform,
    List<String> emphasisTags,
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
