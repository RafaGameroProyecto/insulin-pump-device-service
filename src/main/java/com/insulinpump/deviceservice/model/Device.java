package com.insulinpump.deviceservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "dispositivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de serie es obligatorio")
    @Column(unique = true)
    private String serialNo;

    @NotBlank(message = "El modelo es obligatorio")
    private String model;

    @NotBlank(message = "El fabricante es obligatorio")
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado del dispositivo es obligatorio")
    private DeviceStatus status;

    private Long patientId;

    private LocalDate manufactureDate;

    private LocalDate lastMaintenanceDate;

    // Configuración específica de la bomba de insulina
    private Float maxBasalRate;
    private Float maxBolusAmount;
    private Integer reservoirCapacity;
    private String firmwareVersion;
    private String batteryType;
}