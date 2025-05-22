package com.insulinpump.deviceservice.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String message) {
        super(message);
    }

    public DeviceNotFoundException(Long id) {
        super("Dispositivo no encontrado con ID: " + id);
    }

    public DeviceNotFoundException(String field, String value) {
        super("Dispositivo no encontrado con " + field + ": " + value);
    }
}
