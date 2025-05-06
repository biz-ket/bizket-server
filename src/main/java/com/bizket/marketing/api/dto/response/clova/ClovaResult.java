package com.bizket.marketing.api.dto.response.clova;

import java.util.List;

public record ClovaResult(
    String marketingContent,
    List<String> hashtags
) {

}
