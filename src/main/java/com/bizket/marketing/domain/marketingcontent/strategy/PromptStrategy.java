package com.bizket.marketing.domain.marketingcontent.strategy;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;

public interface PromptStrategy {

    String createPrompt(BaseMarketingContentRequest request);
}
