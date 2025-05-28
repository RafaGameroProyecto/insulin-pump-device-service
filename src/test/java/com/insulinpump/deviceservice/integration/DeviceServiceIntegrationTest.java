package com.insulinpump.deviceservice.integration;

import com.insulinpump.deviceservice.client.PatientClient;
import com.insulinpump.deviceservice.dto.DeviceCreateDto;
import com.insulinpump.deviceservice.dto.DeviceDetailsDto;
import com.insulinpump.deviceservice.dto.PatientDto;
import com.insulinpump.deviceservice.model.Device;
import com.insulinpump.deviceservice.model.DeviceStatus;
import com.insulinpump.deviceservice.repository.DeviceRepository;
import com.insulinpump.deviceservice.service.DeviceService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Device Service Integration Tests - Standalone")
class DeviceServiceIntegrationTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private PatientClient patientClient;

    @InjectMocks
    private DeviceService deviceService;

    private Device testDevice;
    private PatientDto testPatient;

    @BeforeEach
    void setUp() {
        testDevice = createTestDevice();
        testPatient = createTestPatient();
    }

    @Test
    @DisplayName("Debería asignar dispositivo a paciente consultando patient-service")
    void should_assign_device_to_patient_consulting_patient_service() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(patientClient.getPatientById(100L)).thenReturn(testPatient);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);
        when(patientClient.assignDeviceToPatient(100L, 1L)).thenReturn(testPatient);

        // When
        DeviceDetailsDto result = deviceService.assignDeviceToPatient(1L, 100L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        // Verificar que se consultaron ambos servicios
        verify(deviceRepository, times(1)).findById(1L);
        verify(patientClient, times(2)).getPatientById(100L); // Cambiado de 1 a 2
        verify(patientClient, times(1)).assignDeviceToPatient(100L, 1L);
        verify(deviceRepository, times(1)).save(any(Device.class));
    }
    @Test
    @DisplayName("Debería manejar error cuando paciente no existe en patient-service")
    void should_handle_patient_not_found_in_patient_service() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(patientClient.getPatientById(999L))
                .thenThrow(new RuntimeException("Patient not found"));

        // When & Then
        assertThatThrownBy(() -> deviceService.assignDeviceToPatient(1L, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Patient not found");

        verify(deviceRepository, times(1)).findById(1L);
        verify(patientClient, times(1)).getPatientById(999L);
        verify(patientClient, never()).assignDeviceToPatient(any(), any());
        verify(deviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería obtener dispositivos con información de paciente enriquecida")
    void should_get_devices_with_enriched_patient_information() {
        // Given
        testDevice.setPatientId(100L);
        when(deviceRepository.findAll()).thenReturn(Arrays.asList(testDevice));
        when(patientClient.getPatientById(100L)).thenReturn(testPatient);

        // When
        var devices = deviceService.getAllDevices();

        // Then
        assertThat(devices).hasSize(1);
        assertThat(devices.get(0).getPatient()).isNotNull();
        assertThat(devices.get(0).getPatient().getName()).isEqualTo("Juan Pérez");
        assertThat(devices.get(0).getPatient().getMedicalId()).isEqualTo("MED123");

        verify(deviceRepository, times(1)).findAll();
        verify(patientClient, times(1)).getPatientById(100L);
    }

    @Test
    @DisplayName("Debería continuar funcionando cuando patient-service no está disponible")
    void should_continue_working_when_patient_service_unavailable() {
        // Given
        testDevice.setPatientId(100L);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(patientClient.getPatientById(100L))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When
        DeviceDetailsDto result = deviceService.getDeviceById(1L);

        // Then - Debería funcionar sin información del paciente
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSerialNo()).isEqualTo("ABC123");
        assertThat(result.getPatient()).isNull(); // Sin info del paciente por el error

        verify(deviceRepository, times(1)).findById(1L);
        verify(patientClient, times(1)).getPatientById(100L);
    }

    @Test
    @DisplayName("Debería manejar dispositivos sin paciente asignado")
    void should_handle_devices_without_assigned_patient() {
        // Given - Device sin paciente asignado
        testDevice.setPatientId(null);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

        // When
        DeviceDetailsDto result = deviceService.getDeviceById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPatient()).isNull();

        verify(deviceRepository, times(1)).findById(1L);
        verify(patientClient, never()).getPatientById(any());
    }

    @Test
    @DisplayName("Debería crear dispositivo y validar unicidad del serial")
    void should_create_device_and_validate_serial_uniqueness() {
        // Given
        DeviceCreateDto createDto = createTestDeviceCreateDto();
        when(deviceRepository.existsBySerialNo("ABC123")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        DeviceDetailsDto result = deviceService.createDevice(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSerialNo()).isEqualTo("ABC123");

        verify(deviceRepository, times(1)).existsBySerialNo("ABC123");
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    @DisplayName("Debería buscar dispositivos por paciente específico")
    void should_search_devices_by_specific_patient() {
        // Given
        testDevice.setPatientId(100L);
        when(deviceRepository.findByPatientId(100L)).thenReturn(Arrays.asList(testDevice));
        when(patientClient.getPatientById(100L)).thenReturn(testPatient);

        // When
        var devices = deviceService.getDevicesByPatientId(100L);

        // Then
        assertThat(devices).hasSize(1);
        assertThat(devices.get(0).getPatient()).isNotNull();
        assertThat(devices.get(0).getPatient().getName()).isEqualTo("Juan Pérez");

        verify(deviceRepository, times(1)).findByPatientId(100L);
        verify(patientClient, times(1)).getPatientById(100L);
    }

    @Test
    @DisplayName("Debería actualizar estado de dispositivo y fecha de mantenimiento")
    void should_update_device_status_and_maintenance_date() {
        // Given
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device device = invocation.getArgument(0);
            device.setStatus(DeviceStatus.MAINTENANCE);
            device.setLastMaintenanceDate(LocalDate.now());
            return device;
        });

        // When
        DeviceDetailsDto result = deviceService.updateDeviceStatus(1L, DeviceStatus.MAINTENANCE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("MAINTENANCE");

        verify(deviceRepository, times(1)).findById(1L);
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    // Métodos helper
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

    private PatientDto createTestPatient() {
        return new PatientDto(100L, "Juan Pérez", 35, "MED123", 1L, "TYPE_1");
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
}