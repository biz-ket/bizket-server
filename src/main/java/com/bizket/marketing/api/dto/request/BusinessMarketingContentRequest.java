package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import java.util.List;

public record BusinessMarketingContentRequest(
    String brandName,
    String account,
    String industry,
    String targetAgeGroup,
    String prompt,
    String platform,
    List<String> emphasisTags
) implements BaseMarketingContentRequest {

    @Override
    public MarketingUserType userType() {
        return MarketingUserType.BUSINESS;
    }
}
