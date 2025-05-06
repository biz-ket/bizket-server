package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import java.util.List;

public record GuestMarketingContentRequest(
    String prompt,
    List<String> emphasisTags
) implements BaseMarketingContentRequest {

    @Override
    public MarketingUserType userType() {
        return MarketingUserType.GUEST;
    }
}
