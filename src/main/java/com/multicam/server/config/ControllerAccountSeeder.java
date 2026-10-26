package com.multicam.server.config;

import com.multicam.server.entity.AccountRole;
import com.multicam.server.entity.User;
import com.multicam.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Đảm bảo hệ thống luôn có đúng một tài khoản Controller (cấp sẵn, không đăng ký được). */
@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerAccountSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.controller.email:controller@example.com}")
    private String email;

    @Value("${app.controller.phone:0900000000}")
    private String phone;

    @Value("${app.controller.password:Controller123}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(AccountRole.CONTROLLER)) {
            return;
        }
        if (userRepository.existsByEmail(email) || userRepository.existsByPhone(phone)) {
            log.error("Khong the tao tai khoan Controller: email {} hoac phone {} da duoc dung", email, phone);
            return;
        }
        userRepository.save(User.builder()
                .firstName("Controller")
                .lastName("Admin")
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(AccountRole.CONTROLLER)
                .active(true)
                .build());
        log.info("Da tao tai khoan Controller: {}", email);
    }
}