//package com.bizket.marketing.domain.clova;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class ClovaMessageTest {
//
//    private final ObjectMapper mapper = new ObjectMapper()
//        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
//
//    @DisplayName("null 필드는 직렬화 결과에서 제외된다")
//    @Test
//    void shouldExcludeNullFieldFromJson() throws Exception {
//        // given
//        ClovaContent content = new ClovaContent("text", "문구 생성해주세요.", null);
//        ClovaMessage message = new ClovaMessage("user", List.of(content));
//
//        // when
//        String json = mapper.writeValueAsString(message);
//
//        // then
//        System.out.println(json);
//        assertThat(json).contains("\"role\":\"user\"");
//        assertThat(json).contains("\"type\":\"text\"");
//        assertThat(json).contains("\"text\":\"문구 생성해주세요.\"");
//        assertThat(json).doesNotContain("imageUrl");
//    }
//}
