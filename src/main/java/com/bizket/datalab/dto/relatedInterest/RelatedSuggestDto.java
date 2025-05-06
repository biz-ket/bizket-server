package com.bizket.datalab.dto.relatedInterest;

import java.util.List;

public record RelatedSuggestDto(
        String keyword,
        List<String> suggestions
) {}