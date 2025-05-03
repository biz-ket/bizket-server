package com.bizket.marketing.api.service.response;

import com.bizket.marketing.domain.hashtag.Hashtag;
import com.bizket.marketing.domain.marketingcontent.MarketingContent;
import java.util.List;

public record ContentResponse(
    String profileText,
    String generatedText,
    String platform,
    String imageUrl,
    List<String> hashtags
) {

    public static ContentResponse of(MarketingContent marketingContent) {
        return new ContentResponse(
            marketingContent.getProfileText(),
            marketingContent.getGeneratedText(),
            marketingContent.getPlatform(),
            marketingContent.getImageUrl(),
            marketingContent.getHashtags().stream()
                .map(Hashtag::getName)
                .toList()
        );
    }
}
