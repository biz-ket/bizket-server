package com.bizket.marketing.api.dto.response;

import com.bizket.marketing.domain.hashtag.model.Hashtag;
import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import java.time.LocalDateTime;
import java.util.List;

public record ContentResponse(
    String generatedText,
    String platform,
    List<String> hashtags,
    String imageUrl,
    LocalDateTime createdAt
) {

    public static ContentResponse of(MarketingContent content) {
        return new ContentResponse(
            content.getGeneratedText(),
            content.getPlatform(),
            extractHashtagNames(content),
            content.getImageUrl(),
            content.getCreatedAt()
        );
    }

    private static List<String> extractHashtagNames(MarketingContent content) {
        return content.getHashtags().stream()
            .map(Hashtag::getName)
            .toList();
    }
}
