package com.bizket.marketing.domain.clova;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClovaContent(
    String type,
    String text,
    ClovaImageUrl imageUrl
) {

}
