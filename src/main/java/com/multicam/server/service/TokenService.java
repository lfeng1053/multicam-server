package com.multicam.server.service;

import com.multicam.server.entity.RefreshToken;
import com.multicam.server.entity.User;
import com.multicam.server.repository.RefreshTokenRepository;
import com.multicam.server.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    public record IssuedTokens(String accessToken, String refreshToken, long expiresIn) {
    }

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final long refreshTtlDays;
    private final SecureRandom random = new SecureRandom();

    public TokenService(RefreshTokenRepository refreshTokenRepository,
                        JwtService jwtService,
                        @Value("${app.jwt.refresh-ttl-days:30}") long refreshTtlDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshTtlDays = refreshTtlDays;
    }

    /** Tạo một phiên mới cho user và trả về cặp token. */
    @Transactional
    public IssuedTokens issue(User user) {
        String refreshToken = newRefreshToken();

        RefreshToken session = refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(sha256Hex(refreshToken))
                .expiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS))
                .build());

        String accessToken = jwtService.generateAccessToken(user, session.getId());
        return new IssuedTokens(accessToken, refreshToken, jwtService.getAccessTtlSeconds());
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "rt_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}