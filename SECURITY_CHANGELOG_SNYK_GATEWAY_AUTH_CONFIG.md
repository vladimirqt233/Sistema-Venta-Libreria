# Evidencia de remediacion Snyk - errores criticos de Gateway, Auth y Config Server

Fecha: 2026-04-24
Servicios: `ms-gateway-service`, `ms-auth`, `ms-config-server`
Responsable del cambio: Codex
Objetivo: retirar los criticos reportados por Snyk con el menor impacto posible sobre el codigo de aplicacion

## Estrategia aplicada

- `ms-gateway-service`: migracion de la base de dependencias a una combinacion oficialmente compatible que incluye Spring Cloud Gateway `4.2.x`
- `ms-auth` y `ms-config-server`: override puntual de Spring Security a `6.5.9` manteniendo la misma linea general del proyecto

## Cambios aplicados

### 1. Gateway - Spring Boot

Archivo: `ms-gateway-service/pom.xml`

Antes:
```xml
<version>3.3.7</version>
```

Despues:
```xml
<version>3.4.12</version>
```

Motivo:
Spring Cloud `2024.0.3` esta basado en Spring Boot `3.4.12`, y esa linea incorpora Spring Cloud Gateway `4.2.7`, superior a la version corregida minima `4.2.5` reportada por Snyk para `CVE-2025-41243`.

### 2. Gateway - Spring Cloud BOM

Archivo: `ms-gateway-service/pom.xml`

Antes:
```xml
<spring-cloud.version>2023.0.4</spring-cloud.version>
```

Despues:
```xml
<spring-cloud.version>2024.0.3</spring-cloud.version>
```

Motivo:
La rama `2024.0.3` gestiona Spring Cloud Gateway `4.2.7`, que supera la version fija `4.2.5` indicada por Snyk.

### 3. Auth - Spring Security

Archivo: `ms-auth/pom.xml`

Antes:
```xml
<spring-security.version>6.3.8</spring-security.version>
```

Despues:
```xml
<spring-security.version>6.5.9</spring-security.version>
```

Motivo:
Forzar una version corregida de `spring-security-web` por encima de `6.3.8` para retirar `CVE-2026-22732` sin migrar todo el servicio a una nueva generacion de Spring Boot.

### 4. Config Server - Spring Security

Archivo: `ms-config-server/pom.xml`

Antes:
```xml
<spring-security.version>6.3.8</spring-security.version>
```

Despues:
```xml
<spring-security.version>6.5.9</spring-security.version>
```

Motivo:
Aplicar la misma correccion puntual que en `ms-auth` para retirar `CVE-2026-22732` con el menor impacto en el servicio.

## Impacto esperado

- `ms-gateway-service` debe dejar de resolver `spring-cloud-gateway-server@4.1.6`
- `ms-gateway-service` debe pasar a una version `4.2.x` corregida frente a `CVE-2025-41243`
- `ms-auth` debe dejar de resolver `spring-security-web@6.3.8`
- `ms-config-server` debe dejar de resolver `spring-security-web@6.3.8`
- Snyk deberia retirar los criticos de esos tres microservicios

## Referencias tecnicas

- Spring Cloud `2023.0.x` soporta Spring Boot `3.3.x` y `3.2.x`
- Spring Cloud `2024.0.x` soporta Spring Boot `3.4.x`
- Spring Cloud `2024.0.3` usa Spring Boot `3.4.12` y Spring Cloud Gateway `4.2.7`
- Spring Security `6.5.9` es una version estable publicada y superior a la version fija minima requerida por Snyk para `CVE-2026-22732`
