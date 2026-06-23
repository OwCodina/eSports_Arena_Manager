# eSports Arena Manager

Plataforma backend distribuida para la organización y gestión integral de torneos de videojuegos competitivos. Desarrollada como proyecto semestral de la asignatura **Desarrollo FullStack I (Backend) — DSY1103**, DuocUC.

---

## Integrantes

| Nombre | Rol |
|---|---|
| Sebastián Cabrera | Desarrollador Backend |
| Owen Sias | Desarrollador Backend |
| Felipe Álvarez | Desarrollador Backend |

---

## Descripción del proyecto

eSports Arena Manager permite gestionar el ciclo completo de un torneo de videojuegos: desde la creación del juego y el torneo, la inscripción de equipos o jugadores individuales, la programación de partidas, el registro y validación de resultados, el cálculo del ranking de posiciones, hasta la asignación automática de premios a los ganadores.

El sistema está construido sobre una arquitectura de **12 microservicios independientes**, cada uno con su propia base de datos H2, expuestos a través de un **API Gateway** centralizado y descubiertos mediante **Eureka**.

---

## Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 4.0.6 | Framework base |
| Spring Cloud OpenFeign | 2025.1.1 | Comunicación entre microservicios |
| Spring Cloud Gateway | 2025.1.1 | Punto de entrada único |
| Spring Cloud Netflix Eureka | 2025.1.1 | Descubrimiento de servicios |
| Spring Security + JWT | - | Autenticación y autorización |
| Spring Data JPA | - | Persistencia de datos |
| H2 Database | - | BD embebida por servicio |
| springdoc-openapi | 3.0.2 | Documentación Swagger/OpenAPI |
| Lombok | - | Reducción de boilerplate |
| Bean Validation | - | Validación de DTOs |
| Datafaker | 2.5.4 | Generación de datos en tests |
| JUnit 5 + Mockito | - | Pruebas unitarias |
| Maven | 3.x | Build multi-módulo |

---

## Microservicios

| Servicio | Puerto | Descripción |
|---|---|---|
| **msvc-eureka** | 8761 | Servidor de descubrimiento. Todos los servicios se registran aquí. |
| **msvc-gateway** | 8080 | Punto de entrada único. Enruta, autentica con JWT y expone Swagger agregado. |
| **msvc-auth** | 8021 | Autenticación JWT: registro, login y generación de tokens. |
| **msvc-game** | 8010 | Catálogo de videojuegos habilitados para torneos. |
| **msvc-user** | 8011 | Gestión de jugadores, organizadores y administradores. |
| **msvc-tournament** | 8012 | Ciclo de vida completo de torneos (BORRADOR → ABIERTO → FINALIZADO). |
| **msvc-sanction** | 8013 | Sanciones a jugadores y equipos. |
| **msvc-team** | 8014 | Equipos y miembros. Valida capitán y juego via Feign. |
| **msvc-registration** | 8015 | Inscripciones al torneo con 5 validaciones en cadena via Feign. |
| **msvc-match** | 8016 | Programación de partidas verificando participantes inscritos. |
| **msvc-result** | 8017 | Registro y validación de resultados de partidas. |
| **msvc-ranking** | 8018 | Tabla de posiciones actualizada automáticamente tras cada resultado. |
| **msvc-prize** | 8019 | Gestión y asignación automática de premios según ranking final. |
| **msvc-notification** | 8020 | Notificaciones internas a usuarios y equipos. |

---

## Rutas principales del Gateway

Todas las peticiones entran por `http://localhost:8080`. El gateway valida el JWT antes de enrutar.

| Prefijo | Microservicio destino |
|---|---|
| `/api/v1/auth/**` | msvc-auth (público: login y registro) |
| `/api/v1/juegos/**` | msvc-game |
| `/api/v1/usuarios/**` | msvc-user |
| `/api/v1/torneos/**` | msvc-tournament |
| `/api/v1/sanciones/**` | msvc-sanction |
| `/api/v1/equipos/**` | msvc-team |
| `/api/v1/inscripciones/**` | msvc-registration |
| `/api/v1/partidas/**` | msvc-match |
| `/api/v1/resultados/**` | msvc-result |
| `/api/v1/rankings/**` | msvc-ranking |
| `/api/v1/premios/**` | msvc-prize |
| `/api/v1/notificaciones/**` | msvc-notification |

---

## Documentación Swagger

Cada microservicio expone su propia documentación en `/docs/swagger-ui.html`.

El **gateway agrega todos los docs en una sola página** con un desplegable para elegir el servicio:

```
http://localhost:8080/docs/swagger-ui.html
```

También se puede acceder directamente a cada servicio:

| Servicio | URL Swagger |
|---|---|
| msvc-auth | http://localhost:8021/docs/swagger-ui.html |
| msvc-game | http://localhost:8010/docs/swagger-ui.html |
| msvc-user | http://localhost:8011/docs/swagger-ui.html |
| msvc-tournament | http://localhost:8012/docs/swagger-ui.html |
| msvc-sanction | http://localhost:8013/docs/swagger-ui.html |
| msvc-team | http://localhost:8014/docs/swagger-ui.html |
| msvc-registration | http://localhost:8015/docs/swagger-ui.html |
| msvc-match | http://localhost:8016/docs/swagger-ui.html |
| msvc-result | http://localhost:8017/docs/swagger-ui.html |
| msvc-ranking | http://localhost:8018/docs/swagger-ui.html |
| msvc-prize | http://localhost:8019/docs/swagger-ui.html |
| msvc-notification | http://localhost:8020/docs/swagger-ui.html |

---

## Instrucciones de ejecución local

### Requisitos previos

- Java 21
- Maven 3.x
- Postman (para pruebas)

### 1. Compilar todos los módulos

```bash
mvn clean install -DskipTests
```

### 2. Iniciar los servicios en orden

Cada servicio se inicia en una terminal separada. **El orden importa** porque los servicios de capas superiores dependen de los de capas inferiores.

```bash
# 1. Infraestructura — siempre primero
cd msvc-eureka  && mvn spring-boot:run
cd msvc-gateway && mvn spring-boot:run

# 2. Autenticación
cd msvc-auth && mvn spring-boot:run

# 3. Capa base (sin dependencias Feign)
cd msvc-game         && mvn spring-boot:run
cd msvc-user         && mvn spring-boot:run
cd msvc-notification && mvn spring-boot:run

# 4. Dominio
cd msvc-tournament && mvn spring-boot:run
cd msvc-sanction   && mvn spring-boot:run

# 5. Lógica
cd msvc-team         && mvn spring-boot:run
cd msvc-registration && mvn spring-boot:run

# 6. Resultados
cd msvc-match  && mvn spring-boot:run
cd msvc-result && mvn spring-boot:run

# 7. Cierre
cd msvc-ranking && mvn spring-boot:run
cd msvc-prize   && mvn spring-boot:run
```

### 3. Verificar que todos los servicios están registrados

Panel Eureka: `http://localhost:8761`

Deben aparecer los 12 servicios registrados.

### 4. Autenticarse antes de usar la API

```bash
# Registrar usuario (público, sin token)
POST http://localhost:8080/api/v1/auth/register

# Login (retorna el JWT)
POST http://localhost:8080/api/v1/auth/login

# Usar el token en el header de todas las demás peticiones:
Authorization: Bearer <token>
```

### 5. Consola H2

Cada servicio expone su BD en `/h2-console`:

```
URL:      http://localhost:<puerto>/h2-console
JDBC URL: jdbc:h2:file:./data/<nombre>
Usuario:  sa
Password: sa
```

---

## Ejecutar pruebas unitarias

```bash
# Ejecutar todos los tests del proyecto
mvn test

# Ejecutar solo los tests de un servicio
cd msvc-game && mvn test
```

Los tests están implementados en los 11 microservicios de negocio usando Given-When-Then con Mockito, cubriendo happy path y casos de error de las reglas de negocio críticas.

---

## Estructura de cada microservicio

```
msvc-<nombre>/
└── src/
    ├── main/java/com/esports/msvc_<nombre>/
    │   ├── config/
    │   │   ├── SwaggerConfig.java
    │   │   └── SecurityConfig.java
    │   ├── controllers/
    │   │   ├── <Entidad>Controller.java
    │   │   └── GlobalExceptionHandler.java
    │   ├── services/
    │   │   ├── <Entidad>Service.java
    │   │   └── <Entidad>ServiceImpl.java
    │   ├── repositories/
    │   │   └── <Entidad>Repository.java
    │   ├── models/
    │   │   ├── <Entidad>.java
    │   │   └── dtos/
    │   ├── clients/
    │   └── exceptions/
    └── test/java/services/
        └── <Entidad>ServiceTest.java
```

---

*Asignatura: Desarrollo FullStack I (Backend) — DSY1103 · DuocUC · 2025*

---

## Despliegue con Docker

### Requisitos

- Docker Desktop instalado y corriendo
- 8 GB de RAM disponibles
- Puertos 8010–8021, 8080 y 8761 libres

### Ejecución

```bash
# Primera vez — compila los 14 servicios y levanta los contenedores (~10 min)
docker-compose up --build

# Siguientes veces — usa imágenes ya compiladas
docker-compose up

# Apagar todo
docker-compose down
```

### Verificación

| URL | Descripción |
|-----|-------------|
| http://localhost:8761 | Panel Eureka — 13 servicios registrados |
| http://localhost:8080/api/v1/auth/register | Registrar usuario (público) |
| http://localhost:8080/api/v1/auth/login | Login → obtener JWT |
| http://localhost:8080/docs/swagger-ui.html | Swagger UI agregado |

### Comandos útiles

```bash
docker-compose ps                    # ver estado de todos los contenedores
docker-compose logs -f msvc-game     # ver logs de un servicio
docker-compose restart msvc-game     # reiniciar un servicio
docker-compose up --build msvc-game  # rebuild de un servicio específico
docker-compose down --rmi all        # eliminar todo y empezar desde cero
```

> **Nota:** los datos son efímeros. Al hacer `docker-compose down` se pierden
> porque la BD H2 corre en memoria dentro del contenedor.
