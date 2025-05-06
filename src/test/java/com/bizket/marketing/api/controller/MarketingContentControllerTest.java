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
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MarketingContentControllerTest extends RestDocsSupport {

    private final MarketingContentService contentService = mock(MarketingContentService.class);

    @Override
    protected Object initController() {
        return new MarketingContentController(contentService);
    }

    @DisplayName("비로그인 사용자 - 마케팅 콘텐츠 생성")
    @Test
    void createContentAsGuest() throws Exception {
        ContentResponse response = createResponse(null);

        given(contentService.createContent(any(), any()))
            .willReturn(response);

        String requestJson = """
            {
                "userType": "GUEST",
                "clientToken": "guest-token",
                "prompt": "가을 감성의 인테리어 소품 추천 문구를 생성해주세요.",
                "emphasisTags": ["TREND"],
                "rawImageUrls": ["https://storage.googleapis.com/nangpago-9d371.firebasestorage.app/dc137676-6240-4920-97d3-727c4b7d6d8d_360_F_517535712_q7f9QC9X6TQxWi6xYZZbMmw5cnLMr279.jpg"]
            }
            """;

        mockMvc.perform(post("/marketing/contents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-create-guest",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(requestFieldsForGuest()),
                responseFields(singleContentFields(false))
            ));
    }

    @DisplayName("로그인 사용자 - 마케팅 콘텐츠 생성")
    @Test
    void createContentAsMember() throws Exception {
        ContentResponse response = createResponse("instagram");

        given(contentService.createContent(any(), any()))
            .willReturn(response);

        String requestJson = """
            {
                "userType": "MEMBER",
                "memberId": 1,
                "prompt": "트렌디한 겨울 코디 추천해주세요.",
                "platform": "instagram",
                "emphasisTags": ["PRICE", "QUALITY"],
                "rawImageUrls": ["https://storage.googleapis.com/nangpago-9d371.firebasestorage.app/dc137676-6240-4920-97d3-727c4b7d6d8d_360_F_517535712_q7f9QC9X6TQxWi6xYZZbMmw5cnLMr279.jpg"]
            }
            """;

        mockMvc.perform(post("/marketing/contents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-create-member",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(requestFieldsForMember()),
                responseFields(singleContentFields(true))
            ));
    }

    @DisplayName("비즈니스 사용자 - 마케팅 콘텐츠 생성")
    @Test
    void createContentAsBusiness() throws Exception {
        ContentResponse response = createResponse("instagram");

        given(contentService.createContent(any(), any()))
            .willReturn(response);

        String requestJson = """
            {
              "userType": "BUSINESS",
              "memberId": 1,
              "brandName": "무드앤무드",
              "account": "moodandmood_official",
              "industry": "패션",
              "targetAgeGroup": "20대",
              "prompt": "봄 시즌 신상품 홍보용 문구를 생성해주세요.",
              "platform": "instagram",
              "emphasisTags": ["PRICE"],
              "rawImageUrls": [
                "https://storage.googleapis.com/nangpago-9d371.firebasestorage.app/dc137676-6240-4920-97d3-727c4b7d6d8d_360_F_517535712_q7f9QC9X6TQxWi6xYZZbMmw5cnLMr279.jpg",
                "https://storage.googleapis.com/nangpago-9d371.firebasestorage.app/dc137676-6240-4920-97d3-727c4b7d6d8d_360_F_517535712_q7f9QC9X6TQxWi6xYZZbMmw5cnLMr279.jpg"
              ]
            }
            """;

        mockMvc.perform(post("/marketing/contents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-create-business",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(requestFieldsForBusiness()),
                responseFields(singleContentFields(true))
            ));
    }

    @DisplayName("로그인 사용자 - 마케팅 콘텐츠 목록 조회")
    @Test
    void getContentsWithLogin() throws Exception {
        ContentResponse response = createResponse("instagram");

        given(contentService.getContents(any(), any()))
            .willReturn(List.of(response));

        mockMvc.perform(get("/marketing/contents")
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
                queryParameters(
                    parameterWithName("clientToken").description("클라이언트 토큰")
                ),
                responseFields(commonFields(false))
            ));
    }

    @DisplayName("로그인 사용자 - 마케팅 콘텐츠 단건 조회")
    @Test
    void getContentByIdWithLogin() throws Exception {
        ContentResponse response = createResponse("instagram");

        given(contentService.getById(1L))
            .willReturn(response);

        mockMvc.perform(get("/marketing/contents/{id}", 1L)
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

        given(contentService.getById(1L))
            .willReturn(response);

        mockMvc.perform(get("/marketing/contents/{id}", 1L)
                .param("clientToken", "bizket-test")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-get-guest",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                queryParameters(
                    parameterWithName("clientToken").description("클라이언트 토큰")
                ),
                responseFields(singleContentFields(false))
            ));
    }

    private FieldDescriptor[] requestFieldsForGuest() {
        return new FieldDescriptor[]{
            fieldWithPath("userType").type(JsonFieldType.STRING)
                .description("사용자 유형 (GUEST)"),
            fieldWithPath("clientToken").type(JsonFieldType.STRING)
                .description("클라이언트 토큰"),
            fieldWithPath("prompt").type(JsonFieldType.STRING)
                .description("마케팅 콘텐츠 생성 프롬프트"),
            fieldWithPath("emphasisTags").type(JsonFieldType.ARRAY)
                .description("강조할 키워드 태그 목록"),
            fieldWithPath("rawImageUrls").type(JsonFieldType.ARRAY)
                .optional()
                .description("원본 이미지 URL 목록")
        };
    }

    private FieldDescriptor[] requestFieldsForMember() {
        return new FieldDescriptor[]{
            fieldWithPath("userType").type(JsonFieldType.STRING)
                .description("사용자 유형 (MEMBER)"),
            fieldWithPath("memberId").type(JsonFieldType.NUMBER)
                .description("회원 ID"),
            fieldWithPath("prompt").type(JsonFieldType.STRING)
                .description("마케팅 문구 프롬프트"),
            fieldWithPath("platform").type(JsonFieldType.STRING)
                .description("콘텐츠 플랫폼"),
            fieldWithPath("emphasisTags").type(JsonFieldType.ARRAY)
                .description("강조할 키워드 목록"),
            fieldWithPath("rawImageUrls").type(JsonFieldType.ARRAY)
                .optional()
                .description("이미지 URL 목록")
        };
    }

    private FieldDescriptor[] requestFieldsForBusiness() {
        return new FieldDescriptor[]{
            fieldWithPath("userType").type(JsonFieldType.STRING)
                .description("사용자 유형 (BUSINESS)"),
            fieldWithPath("memberId").type(JsonFieldType.NUMBER)
                .description("회원 ID"),
            fieldWithPath("brandName").type(JsonFieldType.STRING)
                .description("브랜드명"),
            fieldWithPath("account").type(JsonFieldType.STRING)
                .description("운영 계정"),
            fieldWithPath("industry").type(JsonFieldType.STRING)
                .description("산업군"),
            fieldWithPath("targetAgeGroup").type(JsonFieldType.STRING)
                .description("타겟 연령층"),
            fieldWithPath("prompt")
                .type(JsonFieldType.STRING)
                .description("마케팅 문구 프롬프트"),
            fieldWithPath("platform").type(JsonFieldType.STRING)
                .description("콘텐츠 플랫폼"),
            fieldWithPath("emphasisTags").type(JsonFieldType.ARRAY)
                .description("강조할 키워드 목록"),
            fieldWithPath("rawImageUrls").type(JsonFieldType.ARRAY)
                .optional()
                .description("이미지 URL 목록")
        };
    }

    private ContentResponse createResponse(String platform) {
        return new ContentResponse(
            "누드톤으로 고급스러운 데일리 룩 ✨ #누드톤메이크업 #직장인메이크업",
            platform,
            List.of("#여름", "#이벤트"),
            List.of("https://bizket.com/image1.jpg", "https://bizket.com/image2.jpg"),
            LocalDateTime.of(2025, 5, 4, 14, 30, 20)
        );
    }

    private FieldDescriptor[] commonFields(boolean withPlatform) {
        List<FieldDescriptor> base = List.of(
            fieldWithPath("data").type(JsonFieldType.ARRAY)
                .description("콘텐츠 목록"),
            fieldWithPath("data[].generatedContent").type(JsonFieldType.STRING)
                .description("생성된 제목"),
            fieldWithPath("data[].hashtags").type(JsonFieldType.ARRAY)
                .description("연관 해시태그 목록"),
            fieldWithPath("data[].imageUrls").type(JsonFieldType.ARRAY)
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
            fieldWithPath("data.generatedContent").type(JsonFieldType.STRING)
                .description("생성된 제목"),
            fieldWithPath("data.hashtags").type(JsonFieldType.ARRAY)
                .description("연관 해시태그 목록"),
            fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY)
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
