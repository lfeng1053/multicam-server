package com.multicam.server.repository;

import com.multicam.server.entity.Device;
import com.multicam.server.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByUserAndClientDeviceId(User user, String clientDeviceId);
}