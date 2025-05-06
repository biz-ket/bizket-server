package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import java.util.List;
import java.util.Objects;

public record GuestMarketingContentRequest(
    String clientToken,
    String prompt,
    List<KeywordType> emphasisTags,
    List<String> rawImageUrls
) implements BaseMarketingContentRequest {

    @Override
    public MarketingUserType userType() {
        return MarketingUserType.GUEST;
    }

    @Override
    public String platform() {
        return null;
    }

    @Override
    public List<String> imageUrls() {
        return Objects.requireNonNullElse(rawImageUrls, List.of());
    }

}
