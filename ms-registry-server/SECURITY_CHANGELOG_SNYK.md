# Evidencia de remediacion Snyk - ms-registry-server

Fecha: 2026-04-23
Servicio: `ms-registry-server`
Archivo modificado: `ms-registry-server/pom.xml`
Responsable del cambio: Codex
Objetivo: eliminar vulnerabilidades criticas reportadas por Snyk en dependencias transitivas de `spring-cloud-starter-netflix-eureka-server`

## Vulnerabilidades objetivo

- `CVE-2025-12383` en `org.glassfish.jersey.core:jersey-client@3.1.9`
- `CVE-2024-50379` en `org.apache.tomcat.embed:tomcat-embed-core@10.1.33`
- `CVE-2024-56337` en `org.apache.tomcat.embed:tomcat-embed-core@10.1.33`
- `CVE-2025-66614` en `org.apache.tomcat.embed:tomcat-embed-core@10.1.33`
- `CVE-2025-22228` en `org.springframework.security:spring-security-crypto@6.2.8`

## Cambios aplicados

### 1. Parent de Spring Boot

Ubicacion: `ms-registry-server/pom.xml`

Antes:
```xml
<version>3.2.12</version>
```

Despues:
```xml
<version>3.3.7</version>
```

Motivo:
Actualizar la base de dependencias de Spring Boot para dejar atras el stack que arrastra `tomcat-embed-core@10.1.33`.

### 2. BOM de Spring Cloud

Ubicacion: `ms-registry-server/pom.xml`

Antes:
```xml
<spring-cloud.version>2023.0.4</spring-cloud.version>
```

Despues:
```xml
<spring-cloud.version>2023.0.6</spring-cloud.version>
```

Motivo:
Mover `spring-cloud-starter-netflix-eureka-server` desde la linea `4.1.4` a una linea que incorpora correcciones asociadas a Snyk para `Jersey`.

### 3. Override manual de Tomcat

Ubicacion: `ms-registry-server/pom.xml`

Antes:
```xml
<tomcat.version>10.1.33</tomcat.version>
```

Despues:
```xml
<tomcat.version>10.1.50</tomcat.version>
```

Motivo:
Forzar una version corregida de Tomcat para retirar los criticos asociados a `10.1.33`, incluyendo el fix reportado por Snyk para `CVE-2025-66614`.

### 4. Override manual de Spring Security

Ubicacion: `ms-registry-server/pom.xml`

Antes:
```xml
<spring-security.version>6.2.8</spring-security.version>
```

Despues:
```xml
<spring-security.version>6.3.8</spring-security.version>
```

Motivo:
Forzar una version corregida de Spring Security para retirar el critico reportado por Snyk en `spring-security-crypto@6.2.8`.

### 5. Version fija de spring-cloud-starter-config

Ubicacion: `ms-registry-server/pom.xml`

Antes:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
    <version>4.2.3</version>
</dependency>
```

Despues:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

Motivo:
Eliminar mezcla de versiones de Spring Cloud y permitir que el BOM `2023.0.6` resuelva una version compatible.

## Impacto esperado

- `spring-cloud-starter-netflix-eureka-server` deja de resolverse en la linea vulnerable `4.1.4`
- `jersey-client@3.1.9` debe dejar de aparecer en el arbol de dependencias
- `tomcat-embed-core@10.1.33` debe dejar de aparecer en el arbol de dependencias y ser reemplazado por `10.1.50`
- `spring-security-crypto@6.2.8` debe dejar de aparecer en el arbol de dependencias y ser reemplazado por `6.3.8`
- Snyk deberia retirar los criticos asociados a esas versiones

## Verificacion recomendada

Ejecutar en `ms-registry-server`:

```powershell
.\mvnw.cmd dependency:tree
.\mvnw.cmd test
```

Luego reejecutar el analisis de Snyk sobre `ms-registry-server`.
