package com.multicam.server.controller;

import com.multicam.server.common.ApiResponse;
import com.multicam.server.dto.auth.LoginRequest;
import com.multicam.server.dto.auth.LoginResponse;
import com.multicam.server.dto.auth.SignupRequest;
import com.multicam.server.dto.auth.SignupResponse;
import com.multicam.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 1. signup — POST /auth/signup */
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    /** 4. login — POST /auth/login */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}