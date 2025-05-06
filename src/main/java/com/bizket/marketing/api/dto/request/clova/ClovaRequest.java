package com.bizket.marketing.api.dto.request.clova;

import com.bizket.marketing.domain.clova.ClovaMessage;
import java.util.List;

public record ClovaRequest(
    List<ClovaMessage> messages,
    String model,
    double topP,
    double temperature,
    int maxTokens,
    double repetitionPenalty,
    boolean includeAiFilters
) {

}
