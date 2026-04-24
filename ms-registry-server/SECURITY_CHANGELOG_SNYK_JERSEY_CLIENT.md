# Evidencia de remediacion Snyk - ultimo critico Jersey

Fecha: 2026-04-23
Servicio: `ms-registry-server`
Archivo modificado: `ms-registry-server/pom.xml`
Responsable del cambio: Codex
Objetivo: retirar el ultimo critico reportado por Snyk en `org.glassfish.jersey.core:jersey-client@3.1.9`

## Vulnerabilidad objetivo

- `CVE-2025-12383` en `org.glassfish.jersey.core:jersey-client@3.1.9`

## Contexto

Despues de actualizar `spring-cloud-starter-netflix-eureka-server` a la linea `4.1.6`, Snyk sigue reportando que `Eureka` arrastra `jersey-client@3.1.9` a traves de `com.netflix.eureka:eureka-client-jersey3@2.0.5` y `com.netflix.eureka:eureka-core-jersey3@2.0.5`.

Como Snyk ya no ofrece una ruta de remediacion soportada desde el starter, se aplica un override puntual en Maven para forzar la version corregida del modulo vulnerable.

## Cambio aplicado

### Override de jersey-client

Ubicacion: `ms-registry-server/pom.xml`

Antes:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Despues:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-client</artifactId>
            <version>3.1.10</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Motivo:
Forzar que Maven resuelva `jersey-client` en una version corregida por encima de la transitiva `3.1.9` que sigue llegando desde Eureka.

## Impacto esperado

- `jersey-client@3.1.9` debe dejar de aparecer en el arbol de dependencias
- `jersey-client@3.1.10` debe pasar a ser la version efectiva
- Snyk deberia retirar el critico `CVE-2025-12383`

## Verificacion recomendada

Ejecutar en `ms-registry-server`:

```powershell
.\mvnw.cmd dependency:tree -Dincludes=org.glassfish.jersey.core:jersey-client
.\mvnw.cmd test
```

Luego reejecutar el analisis de Snyk sobre `ms-registry-server`.
