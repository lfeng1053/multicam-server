package com.multicam.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Gửi email HOẶC phone, kèm mật khẩu và thông tin thiết bị. */
public record LoginRequest(
        @Email @Size(max = 100)
        String email,

        @Pattern(regexp = "^(0\\d{9})?$")
        String phone,

        // Không kiểm tra độ phức tạp ở login; chỉ chặn chuỗi quá dài
        @NotBlank @Size(max = 64)
        String password,

        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{8,64}$")
        String deviceId,

        // 1 = android, 2 = ios, 4 = web (kiểm tra trong service)
        @NotNull
        Integer deviceType,

        @NotBlank @Size(max = 100)
        String deviceName
) {
}