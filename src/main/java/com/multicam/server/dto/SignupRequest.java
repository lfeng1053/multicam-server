package com.multicam.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 50)
        String firstName,

        @NotBlank @Size(max = 50)
        String lastName,

        @NotBlank @Email @Size(max = 100)
        String email,

        // 10 số, bắt đầu bằng 0
        @NotBlank @Pattern(regexp = "^0\\d{9}$")
        String phone,

        // 6-20 ký tự, chỉ chữ và số (không ký tự đặc biệt)
        @NotBlank @Size(min = 6, max = 20) @Pattern(regexp = "^[A-Za-z0-9]*$")
        String password
) {
}