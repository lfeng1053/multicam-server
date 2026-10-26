package com.multicam.server.entity;

import com.multicam.server.exception.AppException;
import com.multicam.server.exception.ErrorCode;

/** device_type theo đề: 1 = android, 2 = ios, 4 = web. */
public enum DeviceType {
    ANDROID(1),
    IOS(2),
    WEB(4);

    private final int code;

    DeviceType(int code) {
        this.code = code;
    }

    public int getCode() { return code; }

    public static DeviceType fromCode(Integer code) {
        if (code != null) {
            for (DeviceType t : values()) {
                if (t.code == code) {
                    return t;
                }
            }
        }
        throw new AppException(ErrorCode.PARAMETER_VALUE_INVALID, "deviceType");
    }
}