package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "userType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = GuestMarketingContentRequest.class, name = "GUEST"),
    @JsonSubTypes.Type(value = MemberMarketingContentRequest.class, name = "MEMBER"),
    @JsonSubTypes.Type(value = BusinessMarketingContentRequest.class, name = "BUSINESS")
})
public interface BaseMarketingContentRequest {

    MarketingUserType userType();

    String platform();

    String prompt();

    List<KeywordType> emphasisTags();

    List<String> imageUrls();

}
