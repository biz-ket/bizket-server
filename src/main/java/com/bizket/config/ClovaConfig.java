package com.bizket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova.api")
public record ClovaConfig(
    String apiKeyId,
    String apiKey,
    String model
) {

}
