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
            해시태그는 별도 항목에서 생성하겠습니다. 콘텐츠에는 해시태그를 포함하지 마세요.
            
            ✏️ 마케팅 문구 조건:
            - 150자~200자 이내로 간결하게 작성
            - 이미지와 어울리게 자연스럽고 세련된 문구로 구성
            - 문장부호([.?!]) 뒤에 한 번 줄바꿈 + 빈 줄(\\n\\n) 추가
            - 각 문장마다 1~2개의 이모지(예: 🌟, 🎉)를 포함하세요.
            - 이모지 뒤에 역슬래시(\\\\)가 붙지 않도록 주의하세요!
            - 적절한 특수문자(예: 이모지) 사용으로 위트 있게!
            - 위트 있는 특수문자 사용 권장
            
            ✏️ 강조할 키워드:
            %s
            
            📌 출력 형식 예시:
            1. 마케팅 문구: 여리여리 분위기 가득 Mocha mousse 올해의 팬톤 컬러 #모카무스\\\\n메이크업 메이크업 예약 및 문의 프로필링크 카카오채널
            2. 해시태그: 감성, 디자인, 트렌디
            
            —————————————————————
            위 예시와 동일한 형식으로,
            1) 마케팅 문구(멀티라인, 150~200자, 각 문장마다 1~2개 이모지)
            2) 해시태그(별도 리스트)
            두 항목을 모두 출력해주세요.
            
            위의 출력 예시 형식에 맞춰서 꼭 두 항목을 모두 출력해 주세요.
            """, userPrompt);
    }
}
