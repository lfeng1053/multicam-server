package com.multicam.server.service;

import com.multicam.server.dto.auth.SignupRequest;
import com.multicam.server.dto.auth.SignupResponse;
import com.multicam.server.entity.AccountRole;
import com.multicam.server.entity.User;
import com.multicam.server.exception.AppException;
import com.multicam.server.exception.ErrorCode;
import com.multicam.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}