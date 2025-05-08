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
    public MarketingContentRequest withImageUrls(List<String> newImageUrls) {
        return new MarketingContentRequest(
            this.userType,
            this.memberId,
            this.clientToken,
            this.brandName,
            this.account,
            this.industry,
            this.targetAgeGroup,
            this.prompt,
            this.platform,
            this.emphasisTags,
            newImageUrls
        );
    }
}
