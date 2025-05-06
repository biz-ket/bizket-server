package com.bizket.marketing.domain.clova;

import java.util.List;

public record ClovaMessage(
    String role,
    List<ClovaContent> content
) {

}
