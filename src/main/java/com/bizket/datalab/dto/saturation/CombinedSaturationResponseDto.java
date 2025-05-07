package com.bizket.datalab.dto.saturation;

public record CombinedSaturationResponseDto(
        String keyword,
        SaturationResponseDto blog,
        SaturationResponseDto news,
        long totalContentCount,
        long totalSearchCount,
        double overallSaturationIndex
) {
    public CombinedSaturationResponseDto(String keyword,
                                         SaturationResponseDto blog,
                                         SaturationResponseDto news) {
        this(
                keyword,
                blog,
                news,
                blog.contentCount() + news.contentCount(),
                blog.searchCount() + news.searchCount(),
                (blog.contentCount() + news.contentCount()) * 100.0
                        / (blog.searchCount() + news.searchCount())
        );
    }
}