//package com.bizket.marketing.infrastructure.clova;
//
//import com.bizket.config.ClovaConfig;
//import com.bizket.marketing.domain.clova.ClovaChatMessage;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//@RequiredArgsConstructor
//@Component
//public class ClovaApiClient {
//
//    private final ClovaConfig clovaConfig;
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public String send(List<ClovaChatMessage> messages) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(clovaConfig.apiKey());
//
//
//    }
//}
