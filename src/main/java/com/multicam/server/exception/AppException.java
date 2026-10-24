package com.multicam.server.exception;

public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    /** Tên field gây lỗi (để client báo lỗi đúng ô nhập). Có thể null. */
    private final String field;

    public AppException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public AppException(ErrorCode errorCode, String field) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.field = field;
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public String getField() { return field; }
}