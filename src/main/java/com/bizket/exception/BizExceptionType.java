package com.bizket.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BizExceptionType {

    // Success(200)
    OK(HttpStatus.OK, "성공"),

    // BAD_REQUEST(400)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // UNAUTHORIZED(401)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 실패했습니다."),

    // Forbidden(403)
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // NOT_FOUND(404)
    NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다."),

    // Conflict(409)
    DUPLICATE_INGREDIENT(HttpStatus.CONFLICT, "이미 등록되어있는 정보입니다."),

    // Unprocessable Entity(422)
    UNPROCESSABLE_JSON(HttpStatus.UNPROCESSABLE_ENTITY, "JSON 객체 변환에 실패했습니다."),

    // Internal Server Error(500)
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    public BizException of() {
        return new BizException(this);
    }

    public BizException of(String message) {
        return new BizException(this, message);
    }
}
