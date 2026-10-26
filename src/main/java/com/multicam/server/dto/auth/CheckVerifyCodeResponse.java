package com.multicam.server.dto.auth;

public record CheckVerifyCodeResponse(
        String userId,
        String accessToken,
        String refreshToken,
        String accountRole,
        String active
) {
}