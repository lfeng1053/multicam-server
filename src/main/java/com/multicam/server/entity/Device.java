package com.multicam.server.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "devices", uniqueConstraints = {
        @UniqueConstraint(name = "uk_devices_user_client", columnNames = {"user_id", "client_device_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** UUID do client sinh ở lần chạy đầu tiên. */
    @Column(nullable = false, length = 64)
    private String clientDeviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceType deviceType;

    @Column(nullable = false, length = 100)
    private String deviceName;

    /** Tên hiển thị trên máy Controller. Mặc định = deviceName, không bị login ghi đè. */
    @Column(nullable = false, length = 100)
    private String cameraName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastLoginAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    /** ID công khai dạng "dv_12" như trong đề. */
    public String getPublicId() {
        return "dv_" + id;
    }
}