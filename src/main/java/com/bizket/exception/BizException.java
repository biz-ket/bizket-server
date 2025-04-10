package com.bizket.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BizException extends RuntimeException {

    private final BizExceptionType bizExceptionType;
    private final String message;

    public BizException(BizExceptionType bizExceptionType) {
        this.bizExceptionType = bizExceptionType;
        this.message = bizExceptionType.getDefaultMessage();
    }

    public BizException(BizExceptionType bizExceptionType, String message) {
        this.bizExceptionType = bizExceptionType;
        this.message = message;
    }

    public boolean isMessageNotEmpty() {
        return this.message != null && !this.message.isEmpty();
    }

    public boolean isInternalServerError() {
        return this.bizExceptionType.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
