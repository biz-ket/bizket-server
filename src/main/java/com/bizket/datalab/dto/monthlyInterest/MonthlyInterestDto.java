package com.bizket.datalab.dto.monthlyInterest;

import java.util.List;

public record MonthlyInterestDto(
        List<MonthlyInterestResultDto> results
) {}
