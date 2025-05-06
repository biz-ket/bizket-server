package com.bizket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova.api")
public record ClovaConfig(
    String baseUrl,
    String apiKeyId,
    String clovaApiKey,
    String model
) {

}

