package com.multicam.server.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.multicam.server.exception.ErrorCode;

/** Wrapper chung: { "code": "1000", "message": "OK", "data": {...} } */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), data);
    }
}