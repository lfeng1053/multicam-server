package com.multicam.server.service;

import com.multicam.server.dto.auth.CheckVerifyCodeRequest;
import com.multicam.server.dto.auth.CheckVerifyCodeResponse;
import com.multicam.server.dto.auth.GetVerifyCodeRequest;
import com.multicam.server.dto.auth.GetVerifyCodeResponse;
import com.multicam.server.entity.User;
import com.multicam.server.entity.VerificationCode;
import com.multicam.server.entity.VerificationPurpose;
import com.multicam.server.exception.AppException;
import com.multicam.server.exception.ErrorCode;
import com.multicam.server.repository.UserRepository;
import com.multicam.server.repository.VerificationCodeRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    /** Mã có hiệu lực 5 phút. */
    static final long CODE_TTL_SECONDS = 300;
    /** Giới hạn 120 giây giữa 2 lần gọi. */
    static final long RESEND_INTERVAL_SECONDS = 120;
    /** Nhập sai quá số lần này thì mã bị vô hiệu, phải xin mã mới. */
    static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final TokenService tokenService;
    private final SecureRandom random = new SecureRandom();

    /** Đối tượng nhận mã: email (đã chuẩn hóa chữ thường) hoặc số điện thoại. */
    private record Target(String value, String field, boolean byEmail) {
    }

    // ===================== 2. get_verify_code =====================

    @Transactional
    public GetVerifyCodeResponse getVerifyCode(GetVerifyCodeRequest req) {
        VerificationPurpose purpose = VerificationPurpose.fromValue(req.purpose());
        Target target = resolveTarget(req.email(), req.phone());
        User user = findUser(target);

        if (purpose == VerificationPurpose.SIGNUP && user.isActive()) {
            // Tài khoản đã kích hoạt rồi, không cần mã đăng ký nữa
            throw new AppException(ErrorCode.USER_EXISTED, target.field());
        }
        if (purpose == VerificationPurpose.FORGOT_PASSWORD && !user.isActive()) {
            throw new AppException(ErrorCode.USER_NOT_VALIDATED, target.field());
        }

        Instant now = Instant.now();
        VerificationCode vc = verificationCodeRepository.findByTargetAndPurpose(target.value(), purpose)
                .orElseGet(() -> VerificationCode.builder().target(target.value()).purpose(purpose).build());

        // Gọi lại trong vòng 120 giây -> 1012, không đổi gì trong DB
        if (vc.getSentAt() != null && vc.getSentAt().plusSeconds(RESEND_INTERVAL_SECONDS).isAfter(now)) {
            throw new AppException(ErrorCode.LIMITED_ACCESS);
        }

        vc.setCode(generateCode());
        vc.setSentAt(now);
        vc.setExpiresAt(now.plusSeconds(CODE_TTL_SECONDS));
        vc.setFailedAttempts(0);

        try {
            verificationCodeRepository.saveAndFlush(vc);
        } catch (DataIntegrityViolationException e) {
            // 2 request đầu tiên đến cùng lúc: request đến sau coi như gọi quá nhanh
            throw new AppException(ErrorCode.LIMITED_ACCESS);
        }

        // TODO: gửi email/SMS thật. Theo đề, tạm trả mã trong response cho đơn giản.
        String masked = target.byEmail() ? maskEmail(target.value()) : maskPhone(target.value());
        return new GetVerifyCodeResponse(CODE_TTL_SECONDS, masked, vc.getCode());
    }

    // ===================== 3. check_verify_code =====================

    /**
     * noRollbackFor: khi nhập sai ta ném AppException nhưng vẫn phải lưu failedAttempts,
     * nếu không transaction rollback và bộ đếm không bao giờ tăng.
     * Vì vậy mọi lệnh ném lỗi phải nằm TRƯỚC các thao tác ghi thành công.
     */
    @Transactional(noRollbackFor = AppException.class)
    public CheckVerifyCodeResponse checkVerifyCode(CheckVerifyCodeRequest req) {
        Target target = resolveTarget(req.email(), req.phone());
        User user = findUser(target);

        if (user.isActive()) {
            throw new AppException(ErrorCode.USER_EXISTED, target.field());
        }

        VerificationCode vc = verificationCodeRepository
                .findByTargetAndPurpose(target.value(), VerificationPurpose.SIGNUP)
                .orElseThrow(() -> new AppException(ErrorCode.CODE_VERIFY_INCORRECT, "code_verify"));

        boolean expired = vc.getExpiresAt().isBefore(Instant.now());
        boolean locked = vc.getFailedAttempts() >= MAX_FAILED_ATTEMPTS;
        if (expired || locked) {
            throw new AppException(ErrorCode.CODE_VERIFY_INCORRECT, "code_verify");
        }

        boolean match = MessageDigest.isEqual(
                vc.getCode().getBytes(StandardCharsets.UTF_8),
                req.codeVerify().getBytes(StandardCharsets.UTF_8));
        if (!match) {
            vc.setFailedAttempts(vc.getFailedAttempts() + 1);
            verificationCodeRepository.save(vc);
            throw new AppException(ErrorCode.CODE_VERIFY_INCORRECT, "code_verify");
        }

        // Mã đúng: kích hoạt, xóa mã đã dùng, cấp token để vào thẳng trang chính
        user.setActive(true);
        userRepository.save(user);
        verificationCodeRepository.delete(vc);

        TokenService.IssuedTokens tokens = tokenService.issue(user);
        return new CheckVerifyCodeResponse(
                user.getPublicId(),
                tokens.accessToken(),
                tokens.refreshToken(),
                user.getRole().getValue(),
                "1"
        );
    }

    // ===================== dùng chung =====================

    private Target resolveTarget(String email, String phone) {
        boolean byEmail = hasText(email);
        boolean byPhone = hasText(phone);
        if (!byEmail && !byPhone) {
            throw new AppException(ErrorCode.PARAMETER_NOT_ENOUGH, "email");
        }
        // Nếu gửi cả hai thì ưu tiên email
        return byEmail
                ? new Target(email.trim().toLowerCase(), "email", true)
                : new Target(phone.trim(), "phone", false);
    }

    private User findUser(Target target) {
        return (target.byEmail()
                ? userRepository.findByEmail(target.value())
                : userRepository.findByPhone(target.value()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_VALIDATED, target.field()));
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    /** an.nguyen@gmail.com -> a***@gmail.com */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }

    /** 0912345678 -> 091****678 */
    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}