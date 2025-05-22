package com.insulinpump.deviceservice.client;

import com.insulinpump.deviceservice.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/patients/{id}")
    PatientDto getPatientById(@PathVariable("id") Long id);

    @GetMapping("/api/patients/device/{deviceId}")
    PatientDto getPatientByDeviceId(@PathVariable("deviceId") Long deviceId);

    @PutMapping("/api/patients/{patientId}/device/{deviceId}")
    PatientDto assignDeviceToPatient(@PathVariable("patientId") Long patientId, @PathVariable("deviceId") Long deviceId);
}
