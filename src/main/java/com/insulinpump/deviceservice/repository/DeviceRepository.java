package com.insulinpump.deviceservice.repository;

import com.insulinpump.deviceservice.model.Device;
import com.insulinpump.deviceservice.model.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findBySerialNo(String serialNo);
    List<Device> findByPatientId(Long patientId);
    List<Device> findByStatus(DeviceStatus status);
    List<Device> findByModelContaining(String model);
    List<Device> findByManufacturerContaining(String manufacturer);
    boolean existsBySerialNo(String serialNo);
}