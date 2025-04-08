package com.bizket.common.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Response<T> {

    private final T data;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private Response(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public static <T> Response<T> of(T data, String message) {
        return Response.<T>builder()
            .data(data)
            .message(message)
            .build();
    }

    public static <T> Response<T> of(T data) {
        return Response.of(data, "");
    }
}

