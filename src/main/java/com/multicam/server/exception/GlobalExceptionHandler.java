package com.multicam.server.exception;

import com.multicam.server.common.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ApiResponse<Map<String, String>> handleApp(AppException ex) {
        return build(ex.getErrorCode(), ex.getField());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();

        // Thiếu tham số bắt buộc -> 1002 (ưu tiên hơn sai định dạng)
        for (FieldError e : errors) {
            if ("NotBlank".equals(e.getCode()) || "NotNull".equals(e.getCode())) {
                return build(ErrorCode.PARAMETER_NOT_ENOUGH, e.getField());
            }
        }
        // Còn lại là giá trị không hợp lệ -> 1004
        String field = errors.isEmpty() ? null : errors.get(0).getField();
        return build(ErrorCode.PARAMETER_VALUE_INVALID, field);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(ErrorCode.PARAMETER_TYPE_INVALID, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Map<String, String>> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return build(ErrorCode.METHOD_INVALID, null);
    }

    @ExceptionHandler(DataAccessException.class)
    public ApiResponse<Map<String, String>> handleDb(DataAccessException ex) {
        log.error("Database error", ex);
        return build(ErrorCode.CANNOT_CONNECT_DB, null);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Map<String, String>> handleOther(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(ErrorCode.EXCEPTION_ERROR, null);
    }

    private ApiResponse<Map<String, String>> build(ErrorCode code, String field) {
        if (field == null) {
            return ApiResponse.error(code);
        }
        return ApiResponse.error(code, Map.of("field", toSnake(field)));
    }

    private String toSnake(String s) {
        return s.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}