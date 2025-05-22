package com.insulinpump.deviceservice.service;

import com.insulinpump.deviceservice.client.PatientClient;
import com.insulinpump.deviceservice.dto.DeviceCreateDto;
import com.insulinpump.deviceservice.dto.DeviceDetailsDto;
import com.insulinpump.deviceservice.dto.PatientDto;
import com.insulinpump.deviceservice.exception.DeviceAlreadyExistsException;
import com.insulinpump.deviceservice.exception.DeviceNotFoundException;
import com.insulinpump.deviceservice.model.Device;
import com.insulinpump.deviceservice.model.DeviceStatus;
import com.insulinpump.deviceservice.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private PatientClient patientClient;

    @InjectMocks
    private DeviceService deviceService;

    private Device testDevice;
    private DeviceCreateDto testDeviceCreateDto;
    private PatientDto testPatient;

    @BeforeEach
    void setUp() {
        testDevice = createTestDevice();
        testDeviceCreateDto = createTestDeviceCreateDto();
        testPatient = createTestPatient();
    }

    @Test
    void should_get_all_devices() {
        // Given
        List<Device> devices = Arrays.asList(testDevice);
        when(deviceRepository.findAll()).thenReturn(devices);

        // When
        List<DeviceDetailsDto> result = deviceService.getAllDevices();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSerialNo()).isEqualTo("ABC123");
        verify(deviceRepository).findAll();
    }

    @Test
    void should_get_device_by_id() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

        // When
        DeviceDetailsDto result = deviceService.getDeviceById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSerialNo()).isEqualTo("ABC123");
        verify(deviceRepository).findById(1L);
    }

    @Test
    void should_throw_exception_when_device_not_found_by_id() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.getDeviceById(1L))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("Dispositivo no encontrado con ID: 1");
    }

    @Test
    void should_create_device_successfully() {
        // Given
        when(deviceRepository.existsBySerialNo("ABC123")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        DeviceDetailsDto result = deviceService.createDevice(testDeviceCreateDto);

        // Then
        assertThat(result.getSerialNo()).isEqualTo("ABC123");
        assertThat(result.getModel()).isEqualTo("Model X");
        verify(deviceRepository).existsBySerialNo("ABC123");
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void should_throw_exception_when_creating_device_with_existing_serial() {
        // Given
        when(deviceRepository.existsBySerialNo("ABC123")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> deviceService.createDevice(testDeviceCreateDto))
                .isInstanceOf(DeviceAlreadyExistsException.class)
                .hasMessageContaining("Ya existe un dispositivo con el número de serie: ABC123");

        verify(deviceRepository).existsBySerialNo("ABC123");
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void should_update_device_status() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        DeviceDetailsDto result = deviceService.updateDeviceStatus(1L, DeviceStatus.MAINTENANCE);

        // Then
        assertThat(result.getStatus()).isEqualTo("MAINTENANCE");
        verify(deviceRepository).findById(1L);
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void should_assign_device_to_patient() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(patientClient.getPatientById(100L)).thenReturn(testPatient);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);
        when(patientClient.assignDeviceToPatient(100L, 1L)).thenReturn(testPatient);

        // When
        DeviceDetailsDto result = deviceService.assignDeviceToPatient(1L, 100L);

        // Then
        assertThat(result).isNotNull();
        verify(deviceRepository).findById(1L);
        verify(patientClient).getPatientById(100L);
        verify(patientClient).assignDeviceToPatient(100L, 1L);
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void should_delete_device() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

        // When
        deviceService.deleteDevice(1L);

        // Then
        verify(deviceRepository).findById(1L);
        verify(deviceRepository).delete(testDevice);
    }

    @Test
    void should_get_devices_by_status() {
        // Given
        List<Device> devices = Arrays.asList(testDevice);
        when(deviceRepository.findByStatus(DeviceStatus.ACTIVE)).thenReturn(devices);

        // When
        List<DeviceDetailsDto> result = deviceService.getDevicesByStatus(DeviceStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        verify(deviceRepository).findByStatus(DeviceStatus.ACTIVE);
    }

    private Device createTestDevice() {
        Device device = new Device();
        device.setId(1L);
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

    private DeviceCreateDto createTestDeviceCreateDto() {
        DeviceCreateDto dto = new DeviceCreateDto();
        dto.setSerialNo("ABC123");
        dto.setModel("Model X");
        dto.setManufacturer("Manufacturer A");
        dto.setStatus(DeviceStatus.ACTIVE);
        dto.setManufactureDate(LocalDate.now().minusMonths(6));
        dto.setMaxBasalRate(2.0f);
        dto.setMaxBolusAmount(10.0f);
        dto.setReservoirCapacity(300);
        dto.setFirmwareVersion("1.0.0");
        dto.setBatteryType("Lithium");
        return dto;
    }

    private PatientDto createTestPatient() {
        PatientDto patient = new PatientDto();
        patient.setId(100L);
        patient.setName("Juan Pérez");
        patient.setAge(35);
        patient.setMedicalId("MED123");
        patient.setDiabetesType("Tipo 1");
        return patient;
    }
}
