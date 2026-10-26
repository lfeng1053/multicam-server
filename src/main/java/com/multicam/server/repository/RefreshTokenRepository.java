package com.multicam.server.repository;

import com.multicam.server.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // dùng ở API 5 (refresh_token)
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}