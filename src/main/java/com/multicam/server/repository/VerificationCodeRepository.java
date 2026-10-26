package com.multicam.server.repository;

import com.multicam.server.entity.VerificationCode;
import com.multicam.server.entity.VerificationPurpose;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByTargetAndPurpose(String target, VerificationPurpose purpose);
}