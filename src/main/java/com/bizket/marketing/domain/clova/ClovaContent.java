package com.bizket.marketing.domain.clova;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClovaContent(
    String type,
    String text,

    @JsonProperty("imageUrl")
    ImageUrl imageUrl
) {

    public record ImageUrl(String url) {

    }
}
