package com.bizket.marketing.api.controller;

import com.bizket.docs.RestDocsSupport;
import com.bizket.marketing.api.dto.response.ContentResponse;
import com.bizket.marketing.application.service.MarketingContentService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MarketingContentControllerTest extends RestDocsSupport {

    private final MarketingContentService contentService = mock(MarketingContentService.class);

    @Override
    protected Object initController() {
        return new MarketingContentController(contentService);
    }

    @DisplayName("로그인 사용자 - 마케팅 콘텐츠 목록 조회")
    @Test
    void getContentsWithLogin() throws Exception {
        ContentResponse response = createResponse("instagram");

        given(contentService.getContents(any(), any()))
            .willReturn(List.of(response));

        mockMvc.perform(get("/marketing/contents")
                .param("memberId", "2")
                .param("clientToken", "bizket-test")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("marketing-contents-list-login",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(commonFields(true))
            ));
    }

    @DisplayName("비로그인 사용자 - 마케팅 콘텐츠 목록 조회")
    @Test
    void getContentsWithoutLogin() throws Exception {
        ContentResponse response = createResponse(null);

        given(contentService.getContents(null, "bizket-test"))
            .willReturn(List.of(response));

        mockMvc.perform(get("/marketing/contents")
                .param("clientToken", "bizket-test")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("marketing-contents-list-guest",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(commonFields(false))
            ));
    }

    @DisplayName("로그인 사용자 - 마케팅 콘텐츠 단건 조회")
    @Test
    void getContentByIdWithLogin() throws Exception {
        ContentResponse response = createResponse("instagram");

        given(contentService.getById(2L))
            .willReturn(response);

        mockMvc.perform(get("/marketing/contents/{id}", 2L)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-get-login",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(singleContentFields(true))
            ));
    }

    @DisplayName("비로그인 사용자 - 마케팅 콘텐츠 단건 조회")
    @Test
    void getContentByIdWithoutLogin() throws Exception {
        ContentResponse response = createResponse(null);

        given(contentService.getById(2L))
            .willReturn(response);

        mockMvc.perform(get("/marketing/contents/{id}", 2L)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-get-guest",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(singleContentFields(false))
            ));
    }

    private ContentResponse createResponse(String platform) {
        return new ContentResponse(
            "누드톤으로 고급스러운 데일리 룩 ✨ #누드톤메이크업 #직장인메이크업",
            platform,
            List.of("#여름", "#이벤트"),
            "https://example.com/image.jpg",
            LocalDateTime.of(2025, 5, 4, 14, 30, 20)
        );
    }

    private FieldDescriptor[] commonFields(boolean withPlatform) {
        List<FieldDescriptor> base = List.of(
            fieldWithPath("data").type(JsonFieldType.ARRAY)
                .description("콘텐츠 목록"),
            fieldWithPath("data[].generatedText").type(JsonFieldType.STRING)
                .description("생성된 제목"),
            fieldWithPath("data[].hashtags").type(JsonFieldType.ARRAY)
                .description("연관 해시태그 목록"),
            fieldWithPath("data[].imageUrl").type(JsonFieldType.STRING)
                .optional()
                .description("콘텐츠 이미지 URL"),
            fieldWithPath("data[].createdAt").type(JsonFieldType.STRING)
                .description("생성 일시"),
            fieldWithPath("message").type(JsonFieldType.STRING)
                .description("응답 메시지")
        );

        List<FieldDescriptor> platformFields = List.of(
            fieldWithPath("data[].platform").type(JsonFieldType.NULL)
                .optional()
                .description("콘텐츠 플랫폼 +\n (비로그인 시 null)"),
            fieldWithPath("data[].platform").type(JsonFieldType.STRING)
                .optional()
                .description("콘텐츠 플랫폼")
        );

        FieldDescriptor platformField = platformFields.get(Boolean.compare(withPlatform, false));

        List<FieldDescriptor> result = new ArrayList<>();
        result.addAll(base);
        result.add(3, platformField);

        return result.toArray(new FieldDescriptor[0]);
    }

    private FieldDescriptor[] singleContentFields(boolean withPlatform) {
        List<FieldDescriptor> base = List.of(
            fieldWithPath("data").type(JsonFieldType.OBJECT)
                .description("콘텐츠 데이터"),
            fieldWithPath("data.generatedText").type(JsonFieldType.STRING)
                .description("생성된 제목"),
            fieldWithPath("data.hashtags").type(JsonFieldType.ARRAY)
                .description("연관 해시태그 목록"),
            fieldWithPath("data.imageUrl").type(JsonFieldType.STRING)
                .optional()
                .description("콘텐츠 이미지 URL"),
            fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                .description("생성 일시"),
            fieldWithPath("message").type(JsonFieldType.STRING)
                .description("응답 메시지")
        );

        List<FieldDescriptor> platformFields = List.of(
            fieldWithPath("data.platform").type(JsonFieldType.NULL)
                .optional()
                .description("콘텐츠 플랫폼 +\n (비로그인 시 null)"),
            fieldWithPath("data.platform").type(JsonFieldType.STRING)
                .optional()
                .description("콘텐츠 플랫폼")
        );

        FieldDescriptor platformField = platformFields.get(Boolean.compare(withPlatform, false));

        List<FieldDescriptor> result = new ArrayList<>();
        result.addAll(base);
        result.add(3, platformField);

        return result.toArray(new FieldDescriptor[0]);
    }

}
