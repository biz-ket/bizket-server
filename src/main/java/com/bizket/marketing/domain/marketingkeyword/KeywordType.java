package com.bizket.marketing.domain.marketingkeyword;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KeywordType {

    QUALITY("퀄리티"),
    PRICE("가격"),
    DESIGN("디자인"),
    TREND("트렌드");

    private final String description;

}
