package com.bizket.marketing.builder;

import com.bizket.marketing.api.dto.request.BaseMarketingContentRequest;
import com.bizket.marketing.domain.clova.ClovaChatMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MarketingContentBuilder {

    public List<ClovaChatMessage> build(BaseMarketingContentRequest request) {
        String userPrompt = String.format("""
            아래 요청에 따라 마케팅 문구와 해시태그를 생성해줘.
            
            %s
            
            출력 형식:
            1. 마케팅 문구
            2. 해시태그
            """, request.userType().createPrompt(request));

        return List.of(
            new ClovaChatMessage("system", "당신은 마케팅 콘텐츠 전문가입니다."),
            new ClovaChatMessage("user", userPrompt)
        );
    }
}
