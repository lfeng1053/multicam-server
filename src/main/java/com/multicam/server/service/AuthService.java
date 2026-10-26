package com.multicam.server.service;

import com.multicam.server.dto.auth.LoginRequest;
import com.multicam.server.dto.auth.LoginResponse;
import com.multicam.server.dto.auth.SignupRequest;
import com.multicam.server.dto.auth.SignupResponse;
import com.multicam.server.entity.AccountRole;
import com.multicam.server.entity.Device;
import com.multicam.server.entity.DeviceType;
import com.multicam.server.entity.User;
import com.multicam.server.exception.AppException;
import com.multicam.server.exception.ErrorCode;
import com.multicam.server.repository.DeviceRepository;
import com.multicam.server.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // ===================== 1. signup =====================

    @Transactional
    public SignupResponse signup(SignupRequest req) {
        String email = req.email().trim().toLowerCase();
        String phone = req.phone().trim();

        // Mật khẩu không được trùng số điện thoại
        if (req.password().equals(phone)) {
            throw new AppException(ErrorCode.PARAMETER_VALUE_INVALID, "password");
        }
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.USER_EXISTED, "email");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new AppException(ErrorCode.USER_EXISTED, "phone");
        }

        User user = User.builder()
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(AccountRole.REMOTE) // đăng ký qua app LUÔN là Remote, không đọc từ request
                .build();

        try {
            // saveAndFlush để bắt lỗi unique ngay tại đây (2 request đăng ký cùng lúc)
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return SignupResponse.from(user);
    }

    // ===================== 4. login =====================

    @Transactional
    public LoginResponse login(LoginRequest req) {
        boolean byEmail = hasText(req.email());
        boolean byPhone = hasText(req.phone());
        if (!byEmail && !byPhone) {
            throw new AppException(ErrorCode.PARAMETER_NOT_ENOUGH, "email");
        }
        // Nếu gửi cả hai thì ưu tiên email
        String identifier = byEmail ? req.email().trim().toLowerCase() : req.phone().trim();
        String field = byEmail ? "email" : "phone";

        // Kiểm tra giá trị tham số trước khi chạm DB
        DeviceType deviceType = DeviceType.fromCode(req.deviceType());

        User user = (byEmail ? userRepository.findByEmail(identifier) : userRepository.findByPhone(identifier))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_VALIDATED, field));

        // Kiểm tra mật khẩu TRƯỚC trạng thái tài khoản, để không lộ tài khoản nào đang bị khóa/chưa kích hoạt
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.PARAMETER_VALUE_INVALID, "password");
        }
        if (user.isLocked()) {
            throw new AppException(ErrorCode.NOT_ACCESS, null, "locked");
        }
        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_NOT_VALIDATED, field, "not_activated");
        }

        Device device = registerDevice(user, req.deviceId().trim(), deviceType, req.deviceName().trim());

        // Mỗi lần đăng nhập là một phiên mới; KHÔNG hủy phiên trên máy khác
        TokenService.IssuedTokens tokens = tokenService.issue(user);

        return new LoginResponse(
                new LoginResponse.UserInfo(
                        user.getPublicId(), user.getFirstName(), user.getLastName(),
                        user.getEmail(), user.getPhone()),
                user.getRole().getValue(),
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                new LoginResponse.DeviceInfo(device.getPublicId(), device.getCameraName())
        );
    }

    /** Tạo thiết bị lần đầu, hoặc cập nhật thiết bị đã có của user này. */
    private Device registerDevice(User user, String clientDeviceId, DeviceType type, String deviceName) {
        Instant now = Instant.now();
        Device device = deviceRepository.findByUserAndClientDeviceId(user, clientDeviceId).orElse(null);

        if (device == null) {
            device = Device.builder()
                    .user(user)
                    .clientDeviceId(clientDeviceId)
                    .deviceType(type)
                    .deviceName(deviceName)
                    .cameraName(deviceName)
                    .lastLoginAt(now)
                    .build();
        } else {
            device.setDeviceType(type);
            device.setDeviceName(deviceName);
            device.setLastLoginAt(now);
            // cameraName giữ nguyên: người dùng có thể đã đổi tên camera
        }

        try {
            return deviceRepository.saveAndFlush(device);
        } catch (DataIntegrityViolationException e) {
            // 2 lần đăng nhập đầu tiên của cùng thiết bị đến cùng lúc: bảo client thử lại
            throw new AppException(ErrorCode.LIMITED_ACCESS);
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}