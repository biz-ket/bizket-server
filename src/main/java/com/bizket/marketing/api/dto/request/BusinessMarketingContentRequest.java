package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import java.util.List;
import java.util.Objects;

public record BusinessMarketingContentRequest(
    Long memberId,
    String brandName,
    String account,
    String industry,
    String targetAgeGroup,
    String prompt,
    String platform,
    List<KeywordType> emphasisTags,
    List<String> rawImageUrls
) implements BaseMarketingContentRequest {

    @Override
    public MarketingUserType userType() {
        return MarketingUserType.BUSINESS;
    }

    @Override
    public List<String> imageUrls() {
        return Objects.requireNonNullElse(rawImageUrls, List.of());
    }

}
