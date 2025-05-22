package com.insulinpump.deviceservice.dto;

import com.insulinpump.deviceservice.model.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCreateDto {

    @NotBlank(message = "El número de serie es obligatorio")
    private String serialNo;

    @NotBlank(message = "El modelo es obligatorio")
    private String model;

    @NotBlank(message = "El fabricante es obligatorio")
    private String manufacturer;

    @NotNull(message = "El estado del dispositivo es obligatorio")
    private DeviceStatus status;

    private Long patientId;

    private LocalDate manufactureDate;

    private LocalDate lastMaintenanceDate;

    @Positive(message = "La tasa basal máxima debe ser positiva")
    private Float maxBasalRate;

    @Positive(message = "La cantidad máxima de bolo debe ser positiva")
    private Float maxBolusAmount;

    @Positive(message = "La capacidad del reservorio debe ser positiva")
    private Integer reservoirCapacity;

    private String firmwareVersion;

    private String batteryType;
}
