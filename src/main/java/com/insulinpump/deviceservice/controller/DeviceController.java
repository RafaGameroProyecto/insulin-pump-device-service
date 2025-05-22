package com.insulinpump.deviceservice.controller;

import com.insulinpump.deviceservice.dto.DeviceCreateDto;
import com.insulinpump.deviceservice.dto.DeviceDetailsDto;
import com.insulinpump.deviceservice.model.DeviceStatus;
import com.insulinpump.deviceservice.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceDetailsDto>> getAllDevices() {
        log.info("GET /api/devices - Obteniendo todos los dispositivos");
        List<DeviceDetailsDto> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDto> getDeviceById(@PathVariable Long id) {
        log.info("GET /api/devices/{} - Obteniendo dispositivo por ID", id);
        DeviceDetailsDto device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/serial/{serialNo}")
    public ResponseEntity<DeviceDetailsDto> getDeviceBySerialNo(@PathVariable String serialNo) {
        log.info("GET /api/devices/serial/{} - Obteniendo dispositivo por número de serie", serialNo);
        DeviceDetailsDto device = deviceService.getDeviceBySerialNo(serialNo);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DeviceDetailsDto>> getDevicesByPatientId(@PathVariable Long patientId) {
        log.info("GET /api/devices/patient/{} - Obteniendo dispositivos por paciente", patientId);
        List<DeviceDetailsDto> devices = deviceService.getDevicesByPatientId(patientId);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeviceDetailsDto>> getDevicesByStatus(@PathVariable DeviceStatus status) {
        log.info("GET /api/devices/status/{} - Obteniendo dispositivos por estado", status);
        List<DeviceDetailsDto> devices = deviceService.getDevicesByStatus(status);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/search/model")
    public ResponseEntity<List<DeviceDetailsDto>> searchDevicesByModel(@RequestParam String model) {
        log.info("GET /api/devices/search/model?model={} - Buscando dispositivos por modelo", model);
        List<DeviceDetailsDto> devices = deviceService.searchDevicesByModel(model);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/search/manufacturer")
    public ResponseEntity<List<DeviceDetailsDto>> searchDevicesByManufacturer(@RequestParam String manufacturer) {
        log.info("GET /api/devices/search/manufacturer?manufacturer={} - Buscando dispositivos por fabricante", manufacturer);
        List<DeviceDetailsDto> devices = deviceService.searchDevicesByManufacturer(manufacturer);
        return ResponseEntity.ok(devices);
    }

    @PostMapping
    public ResponseEntity<DeviceDetailsDto> createDevice(@Valid @RequestBody DeviceCreateDto deviceCreateDto) {
        log.info("POST /api/devices - Creando nuevo dispositivo");
        DeviceDetailsDto createdDevice = deviceService.createDevice(deviceCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDetailsDto> updateDevice(@PathVariable Long id,
                                                         @Valid @RequestBody DeviceCreateDto deviceUpdateDto) {
        log.info("PUT /api/devices/{} - Actualizando dispositivo", id);
        DeviceDetailsDto updatedDevice = deviceService.updateDevice(id, deviceUpdateDto);
        return ResponseEntity.ok(updatedDevice);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeviceDetailsDto> updateDeviceStatus(@PathVariable Long id,
                                                               @RequestParam DeviceStatus status) {
        log.info("PATCH /api/devices/{}/status - Actualizando estado del dispositivo a {}", id, status);
        DeviceDetailsDto updatedDevice = deviceService.updateDeviceStatus(id, status);
        return ResponseEntity.ok(updatedDevice);
    }

    @PutMapping("/{deviceId}/assign/{patientId}")
    public ResponseEntity<DeviceDetailsDto> assignDeviceToPatient(@PathVariable Long deviceId,
                                                                  @PathVariable Long patientId) {
        log.info("PUT /api/devices/{}/assign/{} - Asignando dispositivo a paciente", deviceId, patientId);
        DeviceDetailsDto updatedDevice = deviceService.assignDeviceToPatient(deviceId, patientId);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.info("DELETE /api/devices/{} - Eliminando dispositivo", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
