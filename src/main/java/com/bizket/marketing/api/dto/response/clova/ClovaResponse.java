package com.bizket.marketing.api.dto.response.clova;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClovaResponse(
    Status status,
    Result result
) {

    public record Status(String code, String message) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
        long created,
        Usage usage,
        Message message
    ) {

        public record Usage(int promptTokens, int completionTokens, int totalTokens) {

        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Message(
            String role,
            String content
        ) {

        }
    }
}
