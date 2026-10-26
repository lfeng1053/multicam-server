package com.multicam.server.dto.auth;

public record GetVerifyCodeResponse(
        long expiresIn,
        String maskedTarget,
        String codeVerify
) {
}