package com.multicam.server.entity;

import com.multicam.server.exception.AppException;
import com.multicam.server.exception.ErrorCode;

public enum VerificationPurpose {
    SIGNUP("signup"),
    FORGOT_PASSWORD("forgot_password");

    private final String value;

    VerificationPurpose(String value) {
        this.value = value;
    }

    public String getValue() { return value; }

    public static VerificationPurpose fromValue(String value) {
        for (VerificationPurpose p : values()) {
            if (p.value.equals(value)) {
                return p;
            }
        }
        throw new AppException(ErrorCode.PARAMETER_VALUE_INVALID, "purpose");
    }
}