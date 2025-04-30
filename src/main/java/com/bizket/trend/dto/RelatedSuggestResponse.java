package com.bizket.trend.dto;

import java.util.List;

public record RelatedSuggestResponse(
        List<String> top
) {}