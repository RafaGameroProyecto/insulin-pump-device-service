package com.insulinpump.deviceservice.dto;

import com.insulinpump.deviceservice.model.Device;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class DeviceDetailsDto {
    private Long id;
    private String serialNo;
    private String model;
    private String manufacturer;
    private String status;
    private LocalDate manufactureDate;
    private LocalDate lastMaintenanceDate;
    private Float maxBasalRate;
    private Float maxBolusAmount;
    private Integer reservoirCapacity;
    private PatientDto patient;

    public DeviceDetailsDto(Device device) {
        this.id = device.getId();
        this.serialNo = device.getSerialNo();
        this.model = device.getModel();
        this.manufacturer = device.getManufacturer();
        this.status = device.getStatus().toString();
        this.manufactureDate = device.getManufactureDate();
        this.lastMaintenanceDate = device.getLastMaintenanceDate();
        this.maxBasalRate = device.getMaxBasalRate();
        this.maxBolusAmount = device.getMaxBolusAmount();
        this.reservoirCapacity = device.getReservoirCapacity();
    }
}