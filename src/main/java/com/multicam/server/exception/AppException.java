package com.multicam.server.exception;

public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    /** Tên field gây lỗi (để client báo lỗi đúng ô nhập). Có thể null. */
    private final String field;
    /** Lý do chi tiết cho client phân nhánh xử lý (ví dụ not_activated, locked). Có thể null. */
    private final String reason;

    public AppException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    public AppException(ErrorCode errorCode, String field) {
        this(errorCode, field, null);
    }

    public AppException(ErrorCode errorCode, String field, String reason) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.field = field;
        this.reason = reason;
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public String getField() { return field; }
    public String getReason() { return reason; }
}