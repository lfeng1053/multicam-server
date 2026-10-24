package com.multicam.server.exception;

public enum ErrorCode {
    OK("1000", "OK"),
    CANNOT_CONNECT_DB("1001", "Can not connect to DB"),
    PARAMETER_NOT_ENOUGH("1002", "Parameter is not enough"),
    PARAMETER_TYPE_INVALID("1003", "Parameter type is invalid"),
    PARAMETER_VALUE_INVALID("1004", "Parameter value is invalid"),
    UNKNOWN_ERROR("1005", "Unknown error"),
    FILE_SIZE_TOO_BIG("1006", "File size is too big"),
    UPLOAD_FILE_FAILED("1007", "Upload file failed"),
    NOT_ACCESS("1009", "Not access"),
    ACTION_DONE_PREVIOUSLY("1010", "Action has been done previously by this user"),
    LIMITED_ACCESS("1012", "Limited access"),

    CODE_VERIFY_INCORRECT("9993", "Code verify is incorrect"),
    NO_DATA("9994", "No data or end of list data"),
    USER_NOT_VALIDATED("9995", "User is not validated"),
    USER_EXISTED("9996", "User existed"),
    METHOD_INVALID("9997", "Method is invalid"),
    TOKEN_INVALID("9998", "Token is invalid"),
    EXCEPTION_ERROR("9999", "Exception error"),

    INVITE_CODE_NOT_FOUND("2001", "Invite code not found or expired"),
    ROOM_FULL("2002", "Room is full"),
    DEVICE_IN_ANOTHER_ROOM("2003", "Device already in another room"),
    NOT_ROOM_OWNER("2004", "Not room owner"),
    ROOM_CLOSED("2005", "Room closed"),
    INVALID_SESSION_STATE("2006", "Invalid session state"),

    UPLOAD_SESSION_NOT_FOUND("3001", "Upload session not found or expired"),
    MISSING_CHUNKS("3002", "Missing chunks"),
    CHECKSUM_MISMATCH("3003", "Checksum mismatch"),
    STORAGE_QUOTA_EXCEEDED("3004", "Storage quota exceeded"),
    VIDEO_ALREADY_UPLOADED("3005", "Video already uploaded"),

    VIDEO_NOT_FOUND("4001", "Video not found"),
    ANOTHER_DOWNLOAD_IN_PROGRESS("4002", "Another download in progress");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}