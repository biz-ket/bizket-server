package com.bizket.marketing.api.dto.response;

import com.bizket.marketing.domain.hashtag.model.Hashtag;
import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import com.bizket.marketing.domain.marktingimage.model.MarketingImage;
import java.time.LocalDateTime;
import java.util.List;

public record ContentResponse(
    String generatedContent,
    String platform,
    List<String> hashtags,
    List<String> imageUrls,
    LocalDateTime createdAt
) {

    public static ContentResponse of(MarketingContent content) {
        return new ContentResponse(
            content.getGeneratedContent(),
            content.getPlatform(),
            extractHashtagNames(content),
            extractImageUrls(content),
            content.getCreatedAt()
        );
    }

    private static List<String> extractHashtagNames(MarketingContent content) {
        return content.getHashtags().stream()
            .map(Hashtag::getName)
            .toList();
    }

    private static List<String> extractImageUrls(MarketingContent content) {
        return content.getImages().stream()
            .map(MarketingImage::getUrl)
            .toList();
    }
    
}
