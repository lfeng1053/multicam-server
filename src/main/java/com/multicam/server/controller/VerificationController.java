package com.multicam.server.controller;

import com.multicam.server.common.ApiResponse;
import com.multicam.server.dto.auth.GetVerifyCodeRequest;
import com.multicam.server.dto.auth.GetVerifyCodeResponse;
import com.multicam.server.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.multicam.server.dto.auth.CheckVerifyCodeRequest;
import com.multicam.server.dto.auth.CheckVerifyCodeResponse;


@RestController
@RequestMapping("/auth/verify-code")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    /** 2. get_verify_code — POST /auth/verify-code */
    @PostMapping
    public ApiResponse<GetVerifyCodeResponse> getVerifyCode(@Valid @RequestBody GetVerifyCodeRequest request) {
        return ApiResponse.success(verificationService.getVerifyCode(request));
    }

    /** 3. check_verify_code — POST /auth/verify-code/check */
    @PostMapping("/check")
    public ApiResponse<CheckVerifyCodeResponse> check(@Valid @RequestBody CheckVerifyCodeRequest request) {
        return ApiResponse.success(verificationService.checkVerifyCode(request));
    }
}