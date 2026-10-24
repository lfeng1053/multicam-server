package com.multicam.server.dto.auth;

import com.multicam.server.entity.User;

public record SignupResponse(
        String userId,
        String email,
        String phone,
        String accountRole,
        String verifyRequired
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getValue(),
                "1"
        );
    }
}