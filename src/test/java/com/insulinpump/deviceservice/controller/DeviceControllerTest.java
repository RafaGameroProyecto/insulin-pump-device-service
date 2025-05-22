package com.insulinpump.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insulinpump.deviceservice.dto.DeviceCreateDto;
import com.insulinpump.deviceservice.dto.DeviceDetailsDto;
import com.insulinpump.deviceservice.exception.DeviceNotFoundException;
import com.insulinpump.deviceservice.model.DeviceStatus;
import com.insulinpump.deviceservice.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private DeviceService deviceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_get_all_devices() throws Exception {
        // Given
        List<DeviceDetailsDto> devices = Arrays.asList(createTestDeviceDetailsDto());
        when(deviceService.getAllDevices()).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].serialNo").value("ABC123"))
                .andExpect(jsonPath("$[0].model").value("Model X"));
    }

    @Test
    void should_get_device_by_id() throws Exception {
        // Given
        DeviceDetailsDto device = createTestDeviceDetailsDto();
        when(deviceService.getDeviceById(1L)).thenReturn(device);

        // When & Then
        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNo").value("ABC123"));
    }

    @Test
    void should_return_404_when_device_not_found() throws Exception {
        // Given
        when(deviceService.getDeviceById(999L))
                .thenThrow(new DeviceNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/devices/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Dispositivo no encontrado"));
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serialNo").value("ABC123"))
                .andExpect(jsonPath("$.model").value("Model X"));
    }

    @Test
    void should_return_400_for_invalid_device_data() throws Exception {
        // Given
        DeviceCreateDto invalidDto = new DeviceCreateDto();
        // Dejar campos obligatorios vacíos

        // When & Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"));
    }

    @Test
    void should_update_device() throws Exception {
        // Given
        DeviceCreateDto updateDto = createTestDeviceCreateDto();
        DeviceDetailsDto updatedDevice = createTestDeviceDetailsDto();
        when(deviceService.updateDevice(anyLong(), any(DeviceCreateDto.class)))
                .thenReturn(updatedDevice);

        // When & Then
        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serialNo").value("ABC123"));
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
    }

    @Test
    void should_delete_device() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_get_devices_by_status() throws Exception {
        // Given
        List<DeviceDetailsDto> devices = Arrays.asList(createTestDeviceDetailsDto());
        when(deviceService.getDevicesByStatus(DeviceStatus.ACTIVE)).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/devices/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
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
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].model").value("Model X"));
    }

    private DeviceDetailsDto createTestDeviceDetailsDto() {
        DeviceDetailsDto dto = new DeviceDetailsDto();
        dto.setId(1L);
        dto.setSerialNo("ABC123");
        dto.setModel("Model X");
        dto.setManufacturer("Manufacturer A");
        dto.setStatus("ACTIVE");
        dto.setManufactureDate(LocalDate.now().minusMonths(6));
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
        dto.setMaxBasalRate(2.0f);
        dto.setMaxBolusAmount(10.0f);
        dto.setReservoirCapacity(300);
        dto.setFirmwareVersion("1.0.0");
        dto.setBatteryType("Lithium");
        return dto;
    }
}
