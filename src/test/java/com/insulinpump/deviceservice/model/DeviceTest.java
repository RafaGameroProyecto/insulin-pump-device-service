package com.insulinpump.deviceservice.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_validate_device_successfully() {
        // Given
        Device device = createValidDevice();

        // When
        Set<ConstraintViolation<Device>> violations = validator.validate(device);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void should_fail_validation_when_serial_number_is_blank() {
        // Given
        Device device = createValidDevice();
        device.setSerialNo("");

        // When
        Set<ConstraintViolation<Device>> violations = validator.validate(device);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El número de serie es obligatorio");
    }

    @Test
    void should_fail_validation_when_model_is_blank() {
        // Given
        Device device = createValidDevice();
        device.setModel("");

        // When
        Set<ConstraintViolation<Device>> violations = validator.validate(device);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El modelo es obligatorio");
    }

    @Test
    void should_fail_validation_when_manufacturer_is_blank() {
        // Given
        Device device = createValidDevice();
        device.setManufacturer("");

        // When
        Set<ConstraintViolation<Device>> violations = validator.validate(device);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El fabricante es obligatorio");
    }

    @Test
    void should_fail_validation_when_status_is_null() {
        // Given
        Device device = createValidDevice();
        device.setStatus(null);

        // When
        Set<ConstraintViolation<Device>> violations = validator.validate(device);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El estado del dispositivo es obligatorio");
    }

    @Test
    void should_create_device_with_lombok_annotations() {
        // Given
        Device device1 = new Device();
        device1.setSerialNo("ABC123");
        device1.setModel("Model X");

        Device device2 = new Device();
        device2.setSerialNo("ABC123");
        device2.setModel("Model X");

        // When & Then
        assertThat(device1).isEqualTo(device2);
        assertThat(device1.hashCode()).isEqualTo(device2.hashCode());
        assertThat(device1.toString()).contains("ABC123", "Model X");
    }

    private Device createValidDevice() {
        Device device = new Device();
        device.setSerialNo("ABC123");
        device.setModel("Model X");
        device.setManufacturer("Manufacturer A");
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
