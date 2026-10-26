package com.multicam.server.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "verification_codes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_verification_target_purpose", columnNames = {"target", "purpose"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email (đã chuẩn hóa chữ thường) hoặc số điện thoại. */
    @Column(nullable = false, length = 100)
    private String target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationPurpose purpose;

    @Column(nullable = false, length = 6)
    private String code;

    /** Thời điểm gửi gần nhất, dùng để giới hạn 120 giây giữa 2 lần gọi. */
    @Column(nullable = false)
    private Instant sentAt;

    @Column(nullable = false)
    private Instant expiresAt;

    /** Số lần nhập sai mã; chạm giới hạn thì phải xin mã mới. */
    @Builder.Default
    @Column(nullable = false)
    private int failedAttempts = 0;
}