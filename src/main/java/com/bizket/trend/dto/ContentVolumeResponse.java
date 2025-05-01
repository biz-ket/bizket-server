package com.bizket.trend.dto;

//특정 키워드의 N개월 전 한 달 동안 콘텐츠(블로그, 카페, 포스트 전부 합친) 발행량 응답용
public record ContentVolumeResponse(
        String keyword,    // 검색 키워드
        int monthsAgo,     // 몇 개월 전 데이터인지 (1: 지난 1개월, 2: 2개월 전 등)
        long count         // 해당 기간의 검색 결과 건수(근사치)
) {}