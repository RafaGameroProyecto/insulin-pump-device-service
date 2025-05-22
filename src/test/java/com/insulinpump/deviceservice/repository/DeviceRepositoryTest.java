package com.insulinpump.deviceservice.repository;

import com.insulinpump.deviceservice.model.Device;
import com.insulinpump.deviceservice.model.DeviceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DeviceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void should_find_device_by_serial_number() {
        // Given
        Device device = createTestDevice("ABC123", "Model X", "Manufacturer A");
        entityManager.persistAndFlush(device);

        // When
        Optional<Device> found = deviceRepository.findBySerialNo("ABC123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSerialNo()).isEqualTo("ABC123");
        assertThat(found.get().getModel()).isEqualTo("Model X");
    }

    @Test
    void should_return_empty_when_serial_not_found() {
        // When
        Optional<Device> found = deviceRepository.findBySerialNo("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void should_find_devices_by_status() {
        // Given
        Device activeDevice1 = createTestDevice("ABC123", "Model X", "Manufacturer A");
        activeDevice1.setStatus(DeviceStatus.ACTIVE);

        Device activeDevice2 = createTestDevice("ABC124", "Model Y", "Manufacturer B");
        activeDevice2.setStatus(DeviceStatus.ACTIVE);

        Device inactiveDevice = createTestDevice("ABC125", "Model Z", "Manufacturer C");
        inactiveDevice.setStatus(DeviceStatus.INACTIVE);

        entityManager.persist(activeDevice1);
        entityManager.persist(activeDevice2);
        entityManager.persist(inactiveDevice);
        entityManager.flush();

        // When
        List<Device> activeDevices = deviceRepository.findByStatus(DeviceStatus.ACTIVE);

        // Then
        assertThat(activeDevices).hasSize(2);
        assertThat(activeDevices).extracting(Device::getStatus)
                .containsOnly(DeviceStatus.ACTIVE);
    }

    @Test
    void should_find_devices_by_patient_id() {
        // Given
        Device device1 = createTestDevice("ABC123", "Model X", "Manufacturer A");
        device1.setPatientId(1L);

        Device device2 = createTestDevice("ABC124", "Model Y", "Manufacturer B");
        device2.setPatientId(1L);

        Device device3 = createTestDevice("ABC125", "Model Z", "Manufacturer C");
        device3.setPatientId(2L);

        entityManager.persist(device1);
        entityManager.persist(device2);
        entityManager.persist(device3);
        entityManager.flush();

        // When
        List<Device> patient1Devices = deviceRepository.findByPatientId(1L);

        // Then
        assertThat(patient1Devices).hasSize(2);
        assertThat(patient1Devices).extracting(Device::getPatientId)
                .containsOnly(1L);
    }

    @Test
    void should_check_if_device_exists_by_serial() {
        // Given
        Device device = createTestDevice("ABC123", "Model X", "Manufacturer A");
        entityManager.persistAndFlush(device);

        // When & Then
        assertThat(deviceRepository.existsBySerialNo("ABC123")).isTrue();
        assertThat(deviceRepository.existsBySerialNo("NONEXISTENT")).isFalse();
    }

    @Test
    void should_find_devices_by_model_containing() {
        // Given
        Device device1 = createTestDevice("ABC123", "iPhone Model", "Apple");
        Device device2 = createTestDevice("ABC124", "Samsung Model", "Samsung");
        Device device3 = createTestDevice("ABC125", "Huawei Phone", "Huawei");

        entityManager.persist(device1);
        entityManager.persist(device2);
        entityManager.persist(device3);
        entityManager.flush();

        // When
        List<Device> modelDevices = deviceRepository.findByModelContaining("Model");

        // Then
        assertThat(modelDevices).hasSize(2);
        assertThat(modelDevices).extracting(Device::getModel)
                .containsExactlyInAnyOrder("iPhone Model", "Samsung Model");
    }

    private Device createTestDevice(String serialNo, String model, String manufacturer) {
        Device device = new Device();
        device.setSerialNo(serialNo);
        device.setModel(model);
        device.setManufacturer(manufacturer);
        device.setStatus(DeviceStatus.ACTIVE);
        device.setManufactureDate(LocalDate.now().minusMonths(6));
        device.setMaxBasalRate(2.0f);
        device.setMaxBolusAmount(10.0f);
        device.setReservoirCapacity(300);
        device.setFirmwareVersion("1.0.0");
        device.setBatteryType("Lithium");
        return device;
    }
}