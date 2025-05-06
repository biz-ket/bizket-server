package com.bizket.marketing.domain.marketingkeyword.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KeywordType {

    QUALITY("퀄리티"),
    PRICE("가격"),
    DESIGN("디자인"),
    TREND("트렌드");

    private final String description;

    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static KeywordType from(String value) {
        return Arrays.stream(values())
            .filter(type ->
                type.name().equalsIgnoreCase(value) ||
                    type.description.equalsIgnoreCase(value)
            )
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid keyword type: " + value));
    }

    @Override
    public String toString() {
        return description;
    }
}
