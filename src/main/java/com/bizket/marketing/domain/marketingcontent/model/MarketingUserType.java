package com.bizket.marketing.domain.marketingcontent.model;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.domain.marketingcontent.strategy.BusinessPromptStrategy;
import com.bizket.marketing.domain.marketingcontent.strategy.GuestPromptStrategy;
import com.bizket.marketing.domain.marketingcontent.strategy.MemberPromptStrategy;
import com.bizket.marketing.domain.marketingcontent.strategy.PromptStrategy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MarketingUserType {

    GUEST(new GuestPromptStrategy()),
    MEMBER(new MemberPromptStrategy()),
    BUSINESS(new BusinessPromptStrategy());

    private final PromptStrategy strategy;

    public String createPrompt(BaseMarketingContentRequest prompt) {
        return strategy.createPrompt(prompt);
    }

    public boolean isGuest() {
        return this == GUEST;
    }

    public boolean isMember() {
        return this == MEMBER;
    }

    public boolean isBusiness() {
        return this == BUSINESS;
    }
}
