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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final PatientClient patientClient;

    public List<DeviceDetailsDto> getAllDevices() {
        log.info("Obteniendo todos los dispositivos");
        return deviceRepository.findAll().stream()
                .map(this::convertToDeviceDetailsDto)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDto getDeviceById(Long id) {
        log.info("Obteniendo dispositivo con ID: {}", id);
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        return convertToDeviceDetailsDto(device);
    }

    public DeviceDetailsDto getDeviceBySerialNo(String serialNo) {
        log.info("Obteniendo dispositivo con número de serie: {}", serialNo);
        Device device = deviceRepository.findBySerialNo(serialNo)
                .orElseThrow(() -> new DeviceNotFoundException("número de serie", serialNo));
        return convertToDeviceDetailsDto(device);
    }

    public List<DeviceDetailsDto> getDevicesByPatientId(Long patientId) {
        log.info("Obteniendo dispositivos del paciente con ID: {}", patientId);
        return deviceRepository.findByPatientId(patientId).stream()
                .map(this::convertToDeviceDetailsDto)
                .collect(Collectors.toList());
    }

    public List<DeviceDetailsDto> getDevicesByStatus(DeviceStatus status) {
        log.info("Obteniendo dispositivos con estado: {}", status);
        return deviceRepository.findByStatus(status).stream()
                .map(this::convertToDeviceDetailsDto)
                .collect(Collectors.toList());
    }

    public List<DeviceDetailsDto> searchDevicesByModel(String model) {
        log.info("Buscando dispositivos por modelo: {}", model);
        return deviceRepository.findByModelContaining(model).stream()
                .map(this::convertToDeviceDetailsDto)
                .collect(Collectors.toList());
    }

    public List<DeviceDetailsDto> searchDevicesByManufacturer(String manufacturer) {
        log.info("Buscando dispositivos por fabricante: {}", manufacturer);
        return deviceRepository.findByManufacturerContaining(manufacturer).stream()
                .map(this::convertToDeviceDetailsDto)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDto createDevice(DeviceCreateDto deviceCreateDto) {
        log.info("Creando nuevo dispositivo con número de serie: {}", deviceCreateDto.getSerialNo());

        if (deviceRepository.existsBySerialNo(deviceCreateDto.getSerialNo())) {
            throw new DeviceAlreadyExistsException(deviceCreateDto.getSerialNo());
        }

        Device device = new Device();
        BeanUtils.copyProperties(deviceCreateDto, device);

        Device savedDevice = deviceRepository.save(device);
        log.info("Dispositivo creado exitosamente con ID: {}", savedDevice.getId());

        return convertToDeviceDetailsDto(savedDevice);
    }

    public DeviceDetailsDto updateDevice(Long id, DeviceCreateDto deviceUpdateDto) {
        log.info("Actualizando dispositivo con ID: {}", id);

        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        // Verificar si el número de serie ya existe (excepto para el dispositivo actual)
        if (!existingDevice.getSerialNo().equals(deviceUpdateDto.getSerialNo()) &&
                deviceRepository.existsBySerialNo(deviceUpdateDto.getSerialNo())) {
            throw new DeviceAlreadyExistsException(deviceUpdateDto.getSerialNo());
        }

        BeanUtils.copyProperties(deviceUpdateDto, existingDevice, "id");

        Device updatedDevice = deviceRepository.save(existingDevice);
        log.info("Dispositivo actualizado exitosamente con ID: {}", updatedDevice.getId());

        return convertToDeviceDetailsDto(updatedDevice);
    }

    public DeviceDetailsDto updateDeviceStatus(Long id, DeviceStatus status) {
        log.info("Actualizando estado del dispositivo con ID: {} a {}", id, status);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        device.setStatus(status);
        if (status == DeviceStatus.MAINTENANCE) {
            device.setLastMaintenanceDate(LocalDate.now());
        }

        Device updatedDevice = deviceRepository.save(device);
        log.info("Estado del dispositivo actualizado exitosamente");

        return convertToDeviceDetailsDto(updatedDevice);
    }

    public DeviceDetailsDto assignDeviceToPatient(Long deviceId, Long patientId) {
        log.info("Asignando dispositivo {} al paciente {}", deviceId, patientId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // Verificar que el paciente existe
        PatientDto patient = patientClient.getPatientById(patientId);

        device.setPatientId(patientId);
        Device updatedDevice = deviceRepository.save(device);

        // Actualizar la asignación en el servicio de pacientes
        patientClient.assignDeviceToPatient(patientId, deviceId);

        log.info("Dispositivo asignado exitosamente al paciente");
        return convertToDeviceDetailsDto(updatedDevice);
    }

    public void deleteDevice(Long id) {
        log.info("Eliminando dispositivo con ID: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        deviceRepository.delete(device);
        log.info("Dispositivo eliminado exitosamente");
    }

    private DeviceDetailsDto convertToDeviceDetailsDto(Device device) {
        DeviceDetailsDto dto = new DeviceDetailsDto(device);

        // Obtener información del paciente si está asignado
        if (device.getPatientId() != null) {
            try {
                PatientDto patient = patientClient.getPatientById(device.getPatientId());
                dto.setPatient(patient);
            } catch (Exception e) {
                log.warn("No se pudo obtener información del paciente con ID: {}", device.getPatientId());
            }
        }

        return dto;
    }
}
