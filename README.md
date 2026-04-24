# Sistema Venta Libreria

Proyecto compuesto por microservicios Spring Boot y un frontend Angular.

## Estructura

- `ms-config-server`: servidor de configuracion centralizada.
- `ms-registry-server`: Eureka Server.
- `ms-gateway-service`: API Gateway.
- `ms-auth`: autenticacion y usuarios.
- `ms-book`: libros, categorias y provedores.
- `ms-cliente`: clientes y vendedores.
- `ms-venta`: carrito y ventas.
- `venta-libros`: frontend Angular.
- `config-data`: archivos de configuracion externa consumidos por Config Server.

## Requisitos reales para levantarlo

- Java 17 instalado y configurado en `JAVA_HOME`.
- Maven Wrapper de cada microservicio (`mvnw.cmd`), no hace falta Maven global.
- Node.js 20 o superior.
- npm 10 o superior.
- MySQL corriendo en `localhost:3306`.

## Bases de datos necesarias

Crear estas bases vacias en MySQL:

- `ms_auth`
- `ms_book`
- `ms_cliente`
- `ms_venta`

Por defecto el proyecto quedo alineado para usar:

- Usuario MySQL: `root`
- Password MySQL: vacio
- Usuario Config Server: `root`
- Password Config Server: `123456`

Si tu entorno usa otras credenciales, puedes sobreescribirlas con variables:

- `DB_USERNAME`
- `DB_PASSWORD`
- `CONFIG_SERVER_USER`
- `CONFIG_SERVER_PASSWORD`

## Orden de arranque

Ejecutar cada servicio en una terminal distinta desde su carpeta:

1. `ms-config-server`
2. `ms-registry-server`
3. `ms-gateway-service`
4. `ms-auth`
5. `ms-book`
6. `ms-cliente`
7. `ms-venta`
8. `venta-libros`

## Comandos

### Backend

Desde cada carpeta de microservicio:

```powershell
.\mvnw.cmd spring-boot:run
```

### Frontend

Desde `venta-libros`:

```powershell
npm install
npm start
```

## Puertos esperados

- Config Server: `7071`
- Eureka: `8090`
- Gateway: `8095`
- Frontend Angular: `4200`

Los demas microservicios usan puerto dinamico y se registran en Eureka.

## Flujo de configuracion

- El `config-server` lee configuracion local desde `config-data`.
- Los microservicios consumen esa configuracion via `spring.config.import`.
- El frontend llama al gateway en `http://localhost:8095`.

## Problemas detectados en este estado del proyecto

- El equipo actual tiene solo Java 8, pero los microservicios usan Spring Boot 3 y requieren Java 17.
- `node`, `npm` y `mvn` no estan disponibles en `PATH`.
- La configuracion original del Config Server dependia de GitHub; ahora quedo preparada para usar `config-data` local.
- El puerto del Config Server se movio a `7071` porque `7070` estaba ocupado por otro proceso local.
