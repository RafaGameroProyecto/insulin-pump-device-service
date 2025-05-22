package com.insulinpump.deviceservice.exception;

public class DeviceAlreadyExistsException extends RuntimeException {

    public DeviceAlreadyExistsException(String serialNo) {
        super("Ya existe un dispositivo con el número de serie: " + serialNo);
    }
}