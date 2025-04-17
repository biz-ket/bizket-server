package com.bizket.auth.jwt;

import com.bizket.exception.BizException;
import com.bizket.exception.BizExceptionType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JwtExceptionHandler {
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredToken(ExpiredJwtException ex) {
        BizException bizEx = BizExceptionType.UNAUTHORIZED.of("토큰이 만료되었습니다.");
        return ResponseEntity
            .status(bizEx.getBizExceptionType().getHttpStatus())
            .body(bizEx.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<String> handleJwtError(JwtException ex) {
        BizException bizEx = BizExceptionType.UNAUTHORIZED.of("유효하지 않은 토큰입니다.");
        return ResponseEntity
            .status(bizEx.getBizExceptionType().getHttpStatus())
            .body(bizEx.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOtherErrors(Exception ex) {
        BizException bizEx = BizExceptionType.SERVER_ERROR.of("토큰 처리 중 오류가 발생했습니다.");
        return ResponseEntity
            .status(bizEx.getBizExceptionType().getHttpStatus())
            .body(bizEx.getMessage());
    }
}
