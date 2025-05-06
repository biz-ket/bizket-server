package com.bizket.datalab.dto.monthlyInterest;

import java.time.YearMonth;

public record MonthlyInterestResponseDto(
        String keyword,
        YearMonth yearMonth,
        double searchVolume
) {}
