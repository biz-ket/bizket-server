package com.bizket.datalab.dto.monthlyInterest;

public record MonthlyInterestDataPointDto(
        String period,    // "2024-05" 같은 문자열
        double ratio      // 0~100 사이 상대값
) {}
