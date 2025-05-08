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
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
        given(contentService.createContent(any(), any())).willReturn(response);

        String json = """
            {
                "userType": "GUEST",
                "memberId": null,
                "clientToken": "bizket-test",
                "brandName": null,
                "account": null,
                "industry": null,
                "targetAgeGroup": null,
                "prompt": "20대 여성 피부관리 꿀팁",
                "platform": null,
                "emphasisTags": ["PRICE", "TREND"],
                "imageUrls": []
            }
            """;

        MockMultipartFile jsonPart = new MockMultipartFile(
            "request", "request", "application/json", json.getBytes()
        );

        mockMvc.perform(multipart("/marketing/contents")
                .file(jsonPart)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-create-guest",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestPartFields("request", requestFieldsForGuest()),
                requestParts(partWithName("request").description("게스트 생성 요청 JSON")),
                responseFields(singleContentFields(false))
            ));
    }

    @DisplayName("로그인 사용자 - 마케팅 콘텐츠 생성")
    @Test
    void createContentWithFile() throws Exception {
        ContentResponse response = createResponse("instagram");
        given(contentService.createContent(any(), any())).willReturn(response);

        String json = """
            {
                "userType": "MEMBER",
                "memberId": 1,
                "clientToken": null,
                "brandName": null,
                "account": null,
                "industry": null,
                "targetAgeGroup": null,
                "prompt": "디자인 좋은 제품 마케팅 컨텐츠 생성해줘",
                "platform": "instagram",
                "emphasisTags": ["DESIGN", "QUALITY"],
                "imageUrls": []
            }
            """;

        MockMultipartFile jsonPart = new MockMultipartFile(
            "request", "request", "application/json", json.getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
            "images", "image.jpg", "image/jpeg", "image data".getBytes()
        );

        mockMvc.perform(multipart("/marketing/contents")
                .file(jsonPart)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-create-member-with-file",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestPartFields("request", requestFieldsForMember()),
                requestParts(
                    partWithName("request").description("회원 요청 JSON"),
                    partWithName("images").optional().description("이미지 파일")
                ),
                responseFields(singleContentFields(true))
            ));
    }

    @DisplayName("비즈니스 사용자 - 마케팅 콘텐츠 생성 (멀티파트)")
    @Test
    void createContentAsBusinessMultipart() throws Exception {
        ContentResponse response = createResponse("instagram");
        given(contentService.createContent(any(), any())).willReturn(response);

        String json = """
            {
                "userType": "BUSINESS",
                "memberId": 1,
                "clientToken": null,
                "brandName": "뷰티살롱",
                "account": "beautysalon_official",
                "industry": "뷰티",
                "targetAgeGroup": "20대",
                "prompt": "20대 여성 피부관리 꿀팁",
                "platform": "instagram",
                "emphasisTags": ["PRICE", "QUALITY"],
                "imageUrls": []
            }
            """;

        MockMultipartFile jsonPart = new MockMultipartFile(
            "request", "request", "application/json", json.getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
            "images", "image.jpg", "image/jpeg", "image-data".getBytes()
        );

        mockMvc.perform(multipart("/marketing/contents")
                .file(jsonPart)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andDo(document("marketing-content-create-business",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestPartFields("request", requestFieldsForBusiness()),
                requestParts(
                    partWithName("request").description("비즈니스 콘텐츠 생성 파라미터(JSON)"),
                    partWithName("images").optional().description("업로드 이미지 파일")
                ),
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
            fieldWithPath("userType").type(JsonFieldType.STRING).description("사용자 유형 (GUEST)"),
            fieldWithPath("memberId").type(JsonFieldType.NULL).optional().description("회원 ID (게스트는 null)"),
            fieldWithPath("clientToken").type(JsonFieldType.STRING).description("클라이언트 토큰"),
            fieldWithPath("brandName").type(JsonFieldType.NULL).optional().description("브랜드명 (게스트는 null)"),
            fieldWithPath("account").type(JsonFieldType.NULL).optional().description("운영 계정 (게스트는 null)"),
            fieldWithPath("industry").type(JsonFieldType.NULL).optional().description("산업군 (게스트는 null)"),
            fieldWithPath("targetAgeGroup").type(JsonFieldType.NULL).optional().description("타겟 연령층 (게스트는 null)"),
            fieldWithPath("prompt").type(JsonFieldType.STRING).description("마케팅 문구 프롬프트"),
            fieldWithPath("platform").type(JsonFieldType.NULL).description("콘텐츠 플랫폼"),
            fieldWithPath("emphasisTags").type(JsonFieldType.ARRAY).description("강조할 키워드 목록"),
            fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).optional().description("이미지 URL 목록")
        };
    }

    private FieldDescriptor[] requestFieldsForMember() {
        return new FieldDescriptor[]{
            fieldWithPath("userType").type(JsonFieldType.STRING).description("사용자 유형 (MEMBER)"),
            fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("회원 ID"),
            fieldWithPath("clientToken").type(JsonFieldType.NULL).optional().description("클라이언트 토큰"),
            fieldWithPath("brandName").type(JsonFieldType.NULL).optional().description("브랜드명"),
            fieldWithPath("account").type(JsonFieldType.NULL).optional().description("운영 계정"),
            fieldWithPath("industry").type(JsonFieldType.NULL).optional().description("산업군"),
            fieldWithPath("targetAgeGroup").type(JsonFieldType.NULL).optional().description("타겟 연령층"),
            fieldWithPath("prompt").type(JsonFieldType.STRING).optional().description("마케팅 문구 프롬프트"),
            fieldWithPath("platform").type(JsonFieldType.STRING).description("콘텐츠 플랫폼"),
            fieldWithPath("emphasisTags").type(JsonFieldType.ARRAY).description("강조할 키워드 목록"),
            fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).optional().description("이미지 URL 목록")
        };
    }

    private FieldDescriptor[] requestFieldsForBusiness() {
        return new FieldDescriptor[]{
            fieldWithPath("userType").type(JsonFieldType.STRING).description("사용자 유형 (BUSINESS)"),
            fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("회원 ID"),
            fieldWithPath("clientToken").type(JsonFieldType.NULL).optional().description("클라이언트 토큰"),
            fieldWithPath("brandName").type(JsonFieldType.STRING).description("브랜드명"),
            fieldWithPath("account").type(JsonFieldType.STRING).description("운영 계정"),
            fieldWithPath("industry").type(JsonFieldType.STRING).description("산업군"),
            fieldWithPath("targetAgeGroup").type(JsonFieldType.STRING).description("타겟 연령층"),
            fieldWithPath("prompt").type(JsonFieldType.STRING).description("마케팅 문구 프롬프트"),
            fieldWithPath("platform").type(JsonFieldType.STRING).description("콘텐츠 플랫폼"),
            fieldWithPath("emphasisTags").type(JsonFieldType.ARRAY).description("강조할 키워드 목록"),
            fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).optional().description("이미지 URL 목록")
        };
    }

    private ContentResponse createResponse(String platform) {
        return new ContentResponse(
            1L,
            "누드톤으로 고급스러운 데일리 룩 ✨ #누드톤메이크업 #직장인메이크업",
            "부드러운 모카 무스로 완성하는 올해의 컬러! 합리적인 가격에 눈부신 디자인을 경험하세요. 지금 바로 만나보세요!",  // generatedContent
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
            fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
                .description("콘텐츠 ID"),
            fieldWithPath("data[].prompt").type(JsonFieldType.STRING)
                .description("요청 프롬프트"),
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
            fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                .description("콘텐츠 ID"),
            fieldWithPath("data.prompt").type(JsonFieldType.STRING)
                .description("요청 프롬프트"),
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
