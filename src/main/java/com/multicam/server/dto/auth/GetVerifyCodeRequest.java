package com.multicam.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Gửi email HOẶC phone (không cần cả hai), kèm purpose. */
public record GetVerifyCodeRequest(
        @Email @Size(max = 100)
        String email,

        // cho phép chuỗi rỗng để client gửi kèm "phone": "" khi dùng email
        @Pattern(regexp = "^(0\\d{9})?$")
        String phone,

        @NotBlank @Pattern(regexp = "^(signup|forgot_password)$")
        String purpose
) {
}