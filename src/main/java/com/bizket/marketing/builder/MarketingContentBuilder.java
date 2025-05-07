package com.bizket.marketing.builder;

import com.bizket.marketing.api.dto.request.MarketingContentRequest;
import com.bizket.marketing.domain.clova.ClovaContent;
import com.bizket.marketing.domain.clova.ClovaContent.ImageUrl;
import com.bizket.marketing.domain.clova.ClovaMessage;
import com.bizket.marketing.domain.marketingcontent.model.MarketingUserType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MarketingContentBuilder {

    private static final String SYSTEM_MESSAGE = "당신은 마케팅 콘텐츠 전문가입니다.";

    public List<ClovaMessage> build(MarketingContentRequest request) {
        List<ClovaMessage> messages = new ArrayList<>();
        messages.add(createSystemMessage());
        messages.addAll(createImageMessages(request.imageUrls()));
        messages.add(createPromptMessage(request));
        return messages;
    }

    private ClovaMessage createSystemMessage() {
        return new ClovaMessage("system", List.of(
            new ClovaContent("text", SYSTEM_MESSAGE, null)
        ));
    }

    private List<ClovaMessage> createImageMessages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }

        return imageUrls.stream()
            .filter(url -> url != null && !url.isBlank())
            .map(url -> new ClovaMessage("user", List.of(
                new ClovaContent("image_url", null, new ImageUrl(url))
            )))
            .toList();
    }

    private ClovaMessage createPromptMessage(MarketingContentRequest request) {
        String formattedPrompt = formatPrompt(request);
        return new ClovaMessage("user", List.of(
            new ClovaContent("text", formattedPrompt, null)
        ));
    }

    private String formatPrompt(MarketingContentRequest request) {
        MarketingUserType type = MarketingUserType.valueOf(request.userType().toUpperCase());
        String userPrompt = type.createPrompt(request);
        return String.format("""
            아래 내용을 기반으로 마케팅 문구와 해시태그를 각각 생성해주세요.
            
            ✏️ 마케팅 문구 조건:
            - 200자 이내로 간결하게 작성
            - 이미지와 어울리게 자연스럽고 세련된 문구로 구성
            
            ✏️ 강조할 키워드:
            %s
            
            📌 출력 형식 예시:
            1. 마케팅 문구: 여리여리 분위기 가득 Mocha mousse 올해의 팬톤 컬러 #모카무스 메이크업 메이크업 예약 및 문의 프로필링크 카카오채널
            2. 해시태그: 감성, 디자인, 트렌디
            
            위의 출력 예시 형식에 맞춰서 꼭 두 항목을 모두 출력해 주세요.
            """, userPrompt);
    }
}
