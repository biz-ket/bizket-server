package com.bizket.marketing.infrastructure.clova;

import com.bizket.config.ClovaConfig;
import com.bizket.marketing.domain.clova.ClovaContent;
import com.bizket.marketing.domain.clova.ClovaContent.ImageUrl;
import com.bizket.marketing.domain.clova.ClovaMessage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClovaApiClientTest {

    private final ClovaConfig config = new ClovaConfig(
        "https://clovastudio.stream.ntruss.com",
        "key-id",
        "api-key",
        "HCX-005"
    );

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final ClovaApiClient client = new ClovaApiClient(config, restTemplate);

    @Test
    @DisplayName("Clova 메시지를 전송하면 응답 문자열을 반환")
    void shouldSendRequestAndReturnResponse() {
        // given
        List<ClovaMessage> messages = List.of(
            new ClovaMessage("system", List.of(new ClovaContent("text", "너는 마케터야", null))),
            new ClovaMessage("user", List.of(
                new ClovaContent("text", "문구 생성해줘", null),
                new ClovaContent("image_url", null, new ImageUrl("image.jpg"))
            ))
        );

        String fakeResponse = "{\"result\":\"some text\"}";
        ResponseEntity<String> response = new ResponseEntity<>(fakeResponse, HttpStatus.OK);

        String expectedUrl = "https://clovastudio.stream.ntruss.com/v3/chat-completions/HCX-005";

        given(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.POST), any(), eq(String.class)))
            .willReturn(response);

        // when
        String result = String.valueOf(client.send(messages));

        // then
        assertThat(result).contains("some text");
        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.POST), any(), eq(String.class));
    }
}
