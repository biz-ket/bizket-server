package com.bizket.marketing.api.dto.request;

import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import java.util.List;

public interface BaseMarketingContentRequest {

    MarketingUserType userType();
    
    String prompt();

    List<String> emphasisTags();
}
