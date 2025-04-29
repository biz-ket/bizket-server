package com.bizket.insight;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .connectTimeout(Duration.ofSeconds(5))  // 이전 setConnectTimeout → connectTimeout
            .readTimeout(Duration.ofSeconds(5))     // 이전 setReadTimeout    → readTimeout
            .build();
    }
}