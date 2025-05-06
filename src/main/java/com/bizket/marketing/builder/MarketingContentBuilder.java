package com.bizket.marketing.builder;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.domain.clova.ClovaContent;
import com.bizket.marketing.domain.clova.ClovaImageUrl;
import com.bizket.marketing.domain.clova.ClovaMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MarketingContentBuilder {

    private static final String SYSTEM_MESSAGE = "당신은 마케팅 콘텐츠 전문가입니다.";

    public List<ClovaMessage> build(BaseMarketingContentRequest request) {
        return List.of(
            buildSystemMessage(),
            buildUserMessage(request)
        );
    }

    private ClovaMessage buildSystemMessage() {
        return new ClovaMessage("system", List.of(
            new ClovaContent("text", SYSTEM_MESSAGE, null)
        ));
    }

    private ClovaMessage buildUserMessage(BaseMarketingContentRequest request) {
        List<ClovaContent> contents = new ArrayList<>();

        contents.add(buildPromptContent(request));
        contents.addAll(buildImageContents(request.imageUrls()));

        return new ClovaMessage("user", contents);
    }

    private ClovaContent buildPromptContent(BaseMarketingContentRequest request) {
        String prompt = String.format("""
            아래 요청에 따라 마케팅 문구와 해시태그를 생성해줘.
            
            %s
            
            출력 형식:
            1. 마케팅 문구
            2. 해시태그
            """, request.userType().createPrompt(request));

        return new ClovaContent("text", prompt, null);
    }

    private List<ClovaContent> buildImageContents(List<String> imageUrls) {
        return imageUrls.stream()
            .filter(Objects::nonNull)
            .map(url -> new ClovaContent("image_url", null, new ClovaImageUrl(url)))
            .toList();
    }
}
