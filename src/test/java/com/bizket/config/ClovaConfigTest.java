package com.bizket.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ClovaConfigTest {

    private static final String BASE_URL = "https://clovastudio.apigw.ntruss.com/testapp/v1/chat-completions";
    private static final String API_KEY_ID = "test-id";
    private static final String API_KEY = "test-key";
    private static final String MODEL = "test-model";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class)
        .withPropertyValues(
            "clova.api.base-url=" + BASE_URL,
            "clova.api.api-key-id=" + API_KEY_ID,
            "clova.api.api-key=" + API_KEY,
            "clova.api.model=" + MODEL
        );

    @Test
    void shouldBindClovaConfigPropertiesCorrectly() {
        contextRunner.run(context -> {
            ClovaConfig config = context.getBean(ClovaConfig.class);

            assertThat(config).isNotNull();
            assertThat(config.baseUrl()).isEqualTo(BASE_URL);
            assertThat(config.apiKeyId()).isEqualTo(API_KEY_ID);
            assertThat(config.apiKey()).isEqualTo(API_KEY);
            assertThat(config.model()).isEqualTo(MODEL);
        });
    }

    @EnableConfigurationProperties(ClovaConfig.class)
    static class TestConfig {

    }
}
