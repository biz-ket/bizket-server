package com.bizket.datalab.dto.relatedInterest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverACResponse(
        List<List<Object>> items
) {}