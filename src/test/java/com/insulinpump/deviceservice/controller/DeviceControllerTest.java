package com.insulinpump.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.insulinpump.deviceservice.dto.DeviceCreateDto;
import com.insulinpump.deviceservice.dto.DeviceDetailsDto;
import com.insulinpump.deviceservice.exception.DeviceNotFoundException;
import com.insulinpump.deviceservice.exception.GlobalExceptionHandler;
import com.insulinpump.deviceservice.model.DeviceStatus;
import com.insulinpump.deviceservice.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void should_get_all_devices() throws Exception {
        // Given
        List<DeviceDetailsDto> devices = Arrays.asList(createTestDeviceDetailsDto());
        when(deviceService.getAllDevices()).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].serialNo").value("ABC123"))
                .andExpect(jsonPath("$[0].model").value("Model X"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(deviceService, times(1)).getAllDevices();
    }

    @Test
    void should_get_device_by_id() throws Exception {
        // Given
        DeviceDetailsDto device = createTestDeviceDetailsDto();
        when(deviceService.getDeviceById(1L)).thenReturn(device);

        // When & Then
        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNo").value("ABC123"))
                .andExpect(jsonPath("$.manufacturer").value("Manufacturer A"));

        verify(deviceService, times(1)).getDeviceById(1L);
    }

    @Test
    void should_return_404_when_device_not_found() throws Exception {
        // Given
        when(deviceService.getDeviceById(999L))
                .thenThrow(new DeviceNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/devices/999"))
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).getDeviceById(999L);
    }

    @Test
    void should_get_device_by_serial_number() throws Exception {
        // Given
        DeviceDetailsDto device = createTestDeviceDetailsDto();
        when(deviceService.getDeviceBySerialNo("ABC123")).thenReturn(device);

        // When & Then
        mockMvc.perform(get("/api/devices/serial/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNo").value("ABC123"));

        verify(deviceService, times(1)).getDeviceBySerialNo("ABC123");
    }

    @Test
    void should_get_devices_by_patient_id() throws Exception {
        // Given
        List<DeviceDetailsDto> devices = Arrays.asList(createTestDeviceDetailsDto());
        when(deviceService.getDevicesByPatientId(100L)).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/patient/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serialNo").value("ABC123"));

        verify(deviceService, times(1)).getDevicesByPatientId(100L);
    }

    @Test
    void should_get_devices_by_status() throws Exception {
        // Given
        List<DeviceDetailsDto> devices = Arrays.asList(createTestDeviceDetailsDto());
        when(deviceService.getDevicesByStatus(DeviceStatus.ACTIVE)).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(deviceService, times(1)).getDevicesByStatus(DeviceStatus.ACTIVE);
    }

    @Test
    void should_search_devices_by_model() throws Exception {
        // Given
        List<DeviceDetailsDto> devices = Arrays.asList(createTestDeviceDetailsDto());
        when(deviceService.searchDevicesByModel("Model")).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/search/model")
                        .param("model", "Model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("Model X"));

        verify(deviceService, times(1)).searchDevicesByModel("Model");
    }

    @Test
    void should_search_devices_by_manufacturer() throws Exception {
        // Given
        List<DeviceDetailsDto> devices = Arrays.asList(createTestDeviceDetailsDto());
        when(deviceService.searchDevicesByManufacturer("Manufacturer")).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/search/manufacturer")
                        .param("manufacturer", "Manufacturer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].manufacturer").value("Manufacturer A"));

        verify(deviceService, times(1)).searchDevicesByManufacturer("Manufacturer");
    }

    @Test
    void should_create_device() throws Exception {
        // Given
        DeviceCreateDto createDto = createTestDeviceCreateDto();
        DeviceDetailsDto createdDevice = createTestDeviceDetailsDto();
        when(deviceService.createDevice(any(DeviceCreateDto.class))).thenReturn(createdDevice);

        // When & Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNo").value("ABC123"))
                .andExpect(jsonPath("$.model").value("Model X"));

        verify(deviceService, times(1)).createDevice(any(DeviceCreateDto.class));
    }

    @Test
    void should_return_400_for_invalid_device_data() throws Exception {
        // Given - Datos inválidos
        DeviceCreateDto invalidDto = new DeviceCreateDto();
        invalidDto.setSerialNo(""); // Vacío (inválido)
        invalidDto.setModel(""); // Vacío (inválido)
        invalidDto.setManufacturer(""); // Vacío (inválido)

        // When & Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(deviceService, never()).createDevice(any(DeviceCreateDto.class));
    }

    @Test
    void should_update_device() throws Exception {
        // Given
        DeviceCreateDto updateDto = createTestDeviceCreateDto();
        DeviceDetailsDto updatedDevice = createTestDeviceDetailsDto();
        when(deviceService.updateDevice(eq(1L), any(DeviceCreateDto.class)))
                .thenReturn(updatedDevice);

        // When & Then
        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNo").value("ABC123"));

        verify(deviceService, times(1)).updateDevice(eq(1L), any(DeviceCreateDto.class));
    }

    @Test
    void should_update_device_status() throws Exception {
        // Given
        DeviceDetailsDto updatedDevice = createTestDeviceDetailsDto();
        updatedDevice.setStatus("MAINTENANCE");
        when(deviceService.updateDeviceStatus(1L, DeviceStatus.MAINTENANCE))
                .thenReturn(updatedDevice);

        // When & Then
        mockMvc.perform(patch("/api/devices/1/status")
                        .param("status", "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        verify(deviceService, times(1)).updateDeviceStatus(1L, DeviceStatus.MAINTENANCE);
    }

    @Test
    void should_assign_device_to_patient() throws Exception {
        // Given
        DeviceDetailsDto updatedDevice = createTestDeviceDetailsDto();
        when(deviceService.assignDeviceToPatient(1L, 100L)).thenReturn(updatedDevice);

        // When & Then
        mockMvc.perform(put("/api/devices/1/assign/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNo").value("ABC123"));

        verify(deviceService, times(1)).assignDeviceToPatient(1L, 100L);
    }

    @Test
    void should_delete_device() throws Exception {
        // Given
        doNothing().when(deviceService).deleteDevice(1L);

        // When & Then
        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).deleteDevice(1L);
    }

    // Métodos helper
    private DeviceDetailsDto createTestDeviceDetailsDto() {
        DeviceDetailsDto dto = new DeviceDetailsDto();
        dto.setId(1L);
        dto.setSerialNo("ABC123");
        dto.setModel("Model X");
        dto.setManufacturer("Manufacturer A");
        dto.setStatus("ACTIVE");
        dto.setManufactureDate(LocalDate.now().minusMonths(6));
        dto.setLastMaintenanceDate(LocalDate.now().minusMonths(1));
        dto.setMaxBasalRate(2.0f);
        dto.setMaxBolusAmount(10.0f);
        dto.setReservoirCapacity(300);
        return dto;
    }

    private DeviceCreateDto createTestDeviceCreateDto() {
        DeviceCreateDto dto = new DeviceCreateDto();
        dto.setSerialNo("ABC123");
        dto.setModel("Model X");
        dto.setManufacturer("Manufacturer A");
        dto.setStatus(DeviceStatus.ACTIVE);
        dto.setManufactureDate(LocalDate.now().minusMonths(6));
        dto.setLastMaintenanceDate(LocalDate.now().minusMonths(1));
        dto.setMaxBasalRate(2.0f);
        dto.setMaxBolusAmount(10.0f);
        dto.setReservoirCapacity(300);
        dto.setFirmwareVersion("1.0.0");
        dto.setBatteryType("Lithium");
        return dto;
    }
}