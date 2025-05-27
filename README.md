# 📦 Device Service

**Microservicio para la gestión de dispositivos de bombas de insulina en el sistema médico.**

## 🚀 Descripción
El Device Service se encarga de gestionar todas las bombas de insulina del sistema, incluyendo su configuración técnica, estado operativo, mantenimiento y asignación a pacientes.

## 🛠️ Tecnologías

- Java 21
- Spring Boot 3.4.5
- Spring Data JPA
- MySQL 8.0
- Spring Cloud Netflix Eureka
- OpenFeign
- Lombok
- Bean Validation

## 📋 Funcionalidades

### ✅ Gestión de Dispositivos
- Registro de nuevas bombas de insulina
- Actualización de configuraciones técnicas
- Control de estados operativos
- Historial de mantenimiento

### ✅ Estados del Dispositivo
- `ACTIVE`: Dispositivo operativo
- `INACTIVE`: Dispositivo desactivado
- `MAINTENANCE`: En mantenimiento
- `DEFECTIVE`: Dispositivo defectuoso

### ✅ Configuración Técnica
- Tasa basal máxima
- Cantidad máxima de bolo
- Capacidad del reservorio
- Versión de firmware
- Tipo de batería

## 🌐 Endpoints Principales

### Acceso Directo (Puerto 8082)

| Método | Endpoint                           | Descripción                      |
|--------|------------------------------------|----------------------------------|
| GET    | /api/devices                       | Obtener todos los dispositivos   |
| GET    | /api/devices/{id}                  | Obtener dispositivo por ID       |
| GET    | /api/devices/serial/{serialNo}     | Buscar por número de serie       |
| POST   | /api/devices                       | Crear nuevo dispositivo          |
| PUT    | /api/devices/{id}                  | Actualizar dispositivo           |
| PATCH  | /api/devices/{id}/status           | Cambiar estado                   |
| PUT    | /api/devices/{deviceId}/assign/{patientId} | Asignar a paciente     |
| DELETE | /api/devices/{id}                  | Eliminar dispositivo             |

### Acceso a través de Gateway (Puerto 8087) - RECOMENDADO

| Método | Endpoint Gateway                                | Descripción                    |
|--------|--------------------------------------------------|--------------------------------|
| GET    | http://localhost:8087/api/devices                | Obtener todos los dispositivos |
| GET    | http://localhost:8087/api/devices/{id}           | Obtener dispositivo por ID     |
| GET    | http://localhost:8087/api/devices/serial/{serialNo} | Buscar por número de serie |
| POST   | http://localhost:8087/api/devices                | Crear nuevo dispositivo        |

## 🔍 Consultas Especializadas (Gateway)

| Método | Endpoint Gateway                                           | Descripción                  |
|--------|------------------------------------------------------------|------------------------------|
| GET    | http://localhost:8087/api/devices/patient/{patientId}     | Dispositivos de un paciente  |
| GET    | http://localhost:8087/api/devices/status/{status}         | Filtrar por estado           |
| GET    | http://localhost:8087/api/devices/search/model?model=X    | Buscar por modelo            |
| GET    | http://localhost:8087/api/devices/search/manufacturer?manufacturer=X | Buscar por fabricante |

## 🗄️ Modelo de Datos

```java
@Entity
public class Device {
    private Long id;
    private String serialNo;
    private String model;
    private String manufacturer;
    private DeviceStatus status;
    private Long patientId;
    private LocalDate manufactureDate;
    private LocalDate lastMaintenanceDate;
    private Float maxBasalRate;
    private Float maxBolusAmount;
    private Integer reservoirCapacity;
    private String firmwareVersion;
    private String batteryType;
}
```

## ⚙️ Configuración

### Puerto
```properties
server.port=8082
```

### Base de Datos
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dispositivos
spring.datasource.username=root
spring.datasource.password=***
```

### Eureka
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## 🚀 Ejecución

1. Iniciar MySQL en puerto 3306
2. Iniciar Eureka Server en puerto 8761
3. Iniciar Gateway Service en puerto 8087
4. Iniciar Patient Service en puerto 8081
5. Ejecutar la aplicación:



## 🔗 Comunicación con Otros Servicios

### Patient Service
- Valida existencia de pacientes
- Sincroniza asignaciones de dispositivos
- Obtiene información de pacientes

### Reading Service
- Proporciona información de dispositivos
- Valida dispositivos para lecturas

### Gateway Service
- Enrutamiento automático de peticiones
- Balanceador de carga

## 💡 Características Especiales

### Validaciones de Negocio
- Números de serie únicos
- Validación de estados válidos
- Control de asignaciones

### Auditoría
- Registro de cambios de estado
- Historial de mantenimiento
- Logging detallado de operaciones

## 👨‍💻 Autor 

**Rafael Gamero Arrabal**  
[🔗 LinkedIn](https://www.linkedin.com/in/rafael-gamero-arrabal-619200186/)
⭐ Parte del Sistema de Microservicios para Bombas de Insulina
