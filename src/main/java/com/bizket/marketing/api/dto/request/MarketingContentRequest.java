package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import java.util.List;

public record MarketingContentRequest(
    String userType,
    Long memberId,
    String clientToken,
    String brandName,
    String account,
    String industry,
    String targetAgeGroup,
    String prompt,
    String platform,
    List<KeywordType> emphasisTags,
    List<String> imageUrls
) {

}
