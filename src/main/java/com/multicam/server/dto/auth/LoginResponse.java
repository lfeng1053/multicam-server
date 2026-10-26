package com.multicam.server.dto.auth;

public record LoginResponse(
        UserInfo user,
        String accountRole,
        String accessToken,
        String refreshToken,
        long expiresIn,
        DeviceInfo device
) {
    public record UserInfo(String id, String firstName, String lastName, String email, String phone) {
    }

    public record DeviceInfo(String deviceId, String cameraName) {
    }
}