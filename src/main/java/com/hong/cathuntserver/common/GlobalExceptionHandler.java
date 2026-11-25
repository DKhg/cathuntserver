package com.hong.cathuntserver.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// 모든 컨트롤러의 예외를 한곳에서 잡아 {code, message} 형식으로 통일해 내려준다(PRD 9 — 에러 포맷 표준화).
// 상세 원인은 로그로만 남기고, 클라이언트에는 내부 정보를 노출하지 않는다(출시 심사·보안).
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스에서 던지는 ResponseStatusException(400 발바닥 부족, 401 로그인 필요, 404 등).
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String code = codeOf(status);
        String message = ex.getReason() != null ? ex.getReason() : code;
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }

    // @Valid 검증 실패(예: 빈 기기 키, caughtCount 음수) → 400.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null && fieldError.getDefaultMessage() != null
            ? fieldError.getDefaultMessage()
            : "입력값이 올바르지 않습니다";
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION", message));
    }

    // 그 밖의 처리되지 않은 오류 → 500. 상세는 로그로만, 클라에는 일반 메시지.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("처리되지 않은 서버 오류", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError("INTERNAL", "서버 오류가 발생했어요. 잠시 후 다시 시도해 주세요."));
    }

    // HTTP 상태 → 코드 문자열(예: 400 → "BAD_REQUEST"). 못 찾으면 숫자.
    private static String codeOf(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.name() : String.valueOf(status.value());
    }
}
