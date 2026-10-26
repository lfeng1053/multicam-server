package com.multicam.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Gửi email HOẶC phone, kèm mã 6 số. */
public record CheckVerifyCodeRequest(
        @Email @Size(max = 100)
        String email,

        @Pattern(regexp = "^(0\\d{9})?$")
        String phone,

        @NotBlank @Pattern(regexp = "^\\d{6}$")
        String codeVerify
) {
}