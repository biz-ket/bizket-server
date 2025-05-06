package com.bizket.datalab.dto.totalSearch;

public record TotalSearchResponseDto(
        String keyword,
        long totalCount
) {}