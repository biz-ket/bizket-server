package com.bizket.datalab.dto.monthlyInterest;

import java.util.List;

public record MonthlyInterestResultDto(
        String title,
        List<String> keywords,
        List<MonthlyInterestDataPointDto> data
) {}
