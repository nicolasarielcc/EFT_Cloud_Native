# Informe de Evaluación Final Transversal (EFT) — Semana 9
## Desarrollo Cloud Native (CDY2204)
### Plataforma de Gestión de Transporte — Cursos Online

---

## Índice

1. [Introducción](#1-introducción)
2. [Criterio 1 — Microservicios Backend en Spring Boot (20 pts)](#2-criterio-1--microservicios-backend-en-spring-boot)
3. [Criterio 2 — Colas de Mensajes en RabbitMQ (15 pts)](#3-criterio-2--colas-de-mensajes-en-rabbitmq)
4. [Criterio 3 — Identity as a Service — Azure AD B2C (15 pts)](#4-criterio-3--identity-as-a-service--azure-ad-b2c)
5. [Criterio 4 — API Manager (10 pts)](#5-criterio-4--api-manager)
6. [Criterio 5 — Almacenamiento en la Nube — AWS S3 (10 pts)](#6-criterio-5--almacenamiento-en-la-nube--aws-s3)
7. [Criterio 6 — Despliegue Pipeline CI/CD (10 pts)](#7-criterio-6--despliegue-pipeline-cicd)
8. [Criterio 7 — Documentación (10 pts)](#8-criterio-7--documentación)
9. [Criterio 8 — Video Explicativo (10 pts)](#9-criterio-8--video-explicativo)
10. [Anexo: Estructura Completa del Proyecto](#10-anexo-estructura-completa-del-proyecto)

---

## 1. Introducción

### Descripción del sistema

El proyecto `cursos_online` implementa una plataforma Cloud Native de gestión de transporte compuesta por **dos microservicios Spring Boot** que se comunican de forma asíncrona mediante **RabbitMQ** (patrón productor-consumidor). El sistema permite la gestión completa de guías de despacho y transportistas, integrando autenticación con **Azure AD B2C**, almacenamiento cloud en **AWS S3**, y despliegue automatizado mediante **Docker + GitHub Actions** sobre una instancia **EC2**.

### Arquitectura general

```
                    ┌─────────────────────────────────────────┐
                    │       Docker Network: cloud-network       │
                    │                                          │
  Cliente ──(JWT)──▶   Producer (:8080)                        │
  (Postman)          │  ├─ SecurityConfig (Azure AD B2C)        │
                     │  ├─ RabbitMQProducer ──▶ RabbitMQ        │
                     │  └─ RestTemplate ──(HTTP GET)──┐         │
                     │                                │         │
                     │                    RabbitMQ    │         │
                     │                   (host=rabbitmq)│        │
                     │                                │         │
                     │  Consumer (:8081) ◀──listen─────┘         │
                     │  ├─ RabbitMQListener ◀──────────┘         │
                     │  ├─ H2 Database (JPA)                      │
                     │  ├─ GuiaService + TransportistaService     │
                     │  └─ AWS S3 (subida/descarga/eliminación)  │
                     │                                          │
                     └─────────────────────────────────────────┘
```

---

## 2. Criterio 1 — Microservicios Backend en Spring Boot

### 2.1 Estructura del proyecto Maven multi-módulo

**Archivo raíz:** `pom.xml`

```xml
<groupId>com.duoc</groupId>
<artifactId>transportmanagement</artifactId>
<version>0.0.1-SNAPSHOT</version>
<modules>
    <module>transportmanagement-producer</module>
    <module>transportmanagement-consumer</module>
</modules>
```

El proyecto está organizado como un **Maven multi-módulo** con dos microservicios independientes:

| Módulo | Puerto | Descripción | Spring Security |
|--------|:------:|-------------|:---------------:|
| `transportmanagement-producer` | 8080 | BFF — Orquesta llamadas, expone API REST securitizada, envía mensajes a RabbitMQ | Sí (Azure AD B2C) |
| `transportmanagement-consumer` | 8081 | Servicio interno — Procesa mensajes de RabbitMQ, lógica de negocio, acceso a BD y S3 | No |

### 2.2 Dependencias clave (pom.xml de cada módulo)

**Producer** (`transportmanagement-producer/pom.xml`):
- `spring-boot-starter-web` — API REST
- `spring-boot-starter-data-jpa` — Persistencia JPA
- `spring-boot-starter-amqp` — RabbitMQ
- `spring-boot-starter-security` — Seguridad
- `spring-boot-starter-oauth2-resource-server` — JWT Azure AD B2C
- `spring-security-oauth2-jose` — Validación JWT
- `spring-boot-starter-validation` — Validaciones Jakarta
- `spring-boot-starter-actuator` — Health checks
- `h2` — Base de datos en memoria
- `lombok` — Reducción de boilerplate

**Consumer** (`transportmanagement-consumer/pom.xml`):
- Mismas dependencias que el producer **excepto** las de Spring Security/OAuth2
- Adicional: `spring-cloud-aws-dependencies` (BOM v2.3.0) y `spring-cloud-aws-messaging` para integración S3

**Versiones:**
- Java 21
- Spring Boot 3.5.14
- Spring Cloud AWS 2.3.0
- Lombok 1.18.46

### 2.3 Microservicio Producer (BFF — Backend for Frontend)

**Paquete:** `com.duoc.transportmanagement`  
**Archivo principal:** `transportmanagement-producer/src/main/java/com/duoc/transportmanagement/TransportmanagementApplication.java`

#### 2.3.1 Controladores REST

##### GuiaController (`/api/guias`) — 12 endpoints

| Método | Endpoint | Descripción | Comunicación |
|--------|----------|-------------|--------------|
| `GET` | `/api/guias` | Listar todas las guías | HTTP → Consumer |
| `GET` | `/api/guias/{id}` | Obtener guía por ID | HTTP → Consumer |
| `POST` | `/api/guias` | Crear guía | RabbitMQ (async) |
| `PUT` | `/api/guias/{id}` | Modificar guía | RabbitMQ (async) |
| `DELETE` | `/api/guias/{id}` | Eliminar guía | RabbitMQ (async) |
| `GET` | `/api/guias/transportista/{id}` | Buscar por transportista | HTTP → Consumer |
| `GET` | `/api/guias/fecha/{fecha}` | Buscar por fecha | HTTP → Consumer |
| `POST` | `/api/guias/s3/{id}` | Subir archivo a S3 | RabbitMQ (async) |
| `PUT` | `/api/guias/s3/{id}` | Actualizar archivo en S3 | RabbitMQ (async) |
| `GET` | `/api/guias/s3/{id}` | Descargar archivo de S3 | HTTP → Consumer |
| `DELETE` | `/api/guias/s3/{id}` | Eliminar archivo de S3 | RabbitMQ (async) |

##### TransportistaController (`/api/transportistas`) — 5 endpoints

| Método | Endpoint | Descripción | Comunicación |
|--------|----------|-------------|--------------|
| `GET` | `/api/transportistas` | Listar transportistas | HTTP → Consumer |
| `GET` | `/api/transportistas/{id}` | Obtener transportista | HTTP → Consumer |
| `POST` | `/api/transportistas` | Crear transportista | RabbitMQ (async) |
| `PUT` | `/api/transportistas/{id}` | Modificar transportista | RabbitMQ (async) |
| `DELETE` | `/api/transportistas/{id}` | Eliminar transportista | RabbitMQ (async) |

**Total: 17 endpoints REST** que devuelven datos en formato JSON.

#### 2.3.2 Patrón de comunicación

El Producer actúa como **BFF (Backend for Frontend)** con dos modos de comunicación:

1. **Lecturas (GET):** Síncronas vía `RestTemplate` → `ConsumerClient` → Consumer (:8081)
2. **Escrituras (POST/PUT/DELETE):** Asíncronas vía RabbitMQ → colas → Consumer procesa

**ConsumerClient.java** (`transportmanagement-producer/src/main/java/com/duoc/transportmanagement/service/ConsumerClient.java`):
```java
@Component
public class ConsumerClient {
    @Autowired private RestTemplate restTemplate;
    @Value("${aws.consumer.url}") private String consumerUrl;

    public GuiaDTO findGuiaById(Long id) {
        return restTemplate.getForObject(consumerUrl + "/api/guias/" + id, GuiaDTO.class);
    }
    public List<GuiaResumenDTO> findAllGuias() {
        return restTemplate.exchange(consumerUrl + "/api/guias", ...);
    }
    // ... métodos similares para transportistas, búsqueda por fecha, descarga S3
}
```

### 2.4 Microservicio Consumer (Procesador de negocio)

**Archivo principal:** `transportmanagement-consumer/src/main/java/com/duoc/transportmanagement/TransportmanagementApplication.java`

#### 2.4.1 Controladores REST (internos)

##### GuiaController (`/api/guias`) — 6 endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/guias` | Listar todas las guías desde BD |
| `GET` | `/api/guias/{id}` | Obtener guía por ID |
| `GET` | `/api/guias/transportista/{id}` | Filtrar por transportista |
| `GET` | `/api/guias/fecha/{fecha}` | Filtrar por fecha |
| `GET` | `/api/guias/s3/{id}` | Descargar archivo de S3 |

##### TransportistaController (`/api/transportistas`) — 2 endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/transportistas` | Listar transportistas |
| `GET` | `/api/transportistas/{id}` | Obtener transportista |

##### RabbitMQController (`/api/rabbit`) — 2 endpoints de prueba

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/rabbit/guia/procesar` | Procesar manualmente mensaje de guía |
| `POST` | `/api/rabbit/transportista/procesar` | Procesar manualmente mensaje de transportista |

##### S3Controller (`/s3`) — 6 endpoints de administración S3

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/s3/listS3Files` | Listar archivos en bucket |
| `GET` | `/s3/getS3FileContent` | Ver contenido de archivo |
| `GET` | `/s3/downloadS3File` | Descargar archivo |
| `POST` | `/s3/uploadFile` | Subir archivo al bucket |
| `DELETE` | `/s3/deleteObject` | Eliminar objeto del bucket |
| `GET` | `/s3/moveFile` | Mover archivo dentro del bucket |

#### 2.4.2 Modelos JPA

**GuiaDespacho.java** (`transportmanagement-consumer/src/main/java/com/duoc/transportmanagement/model/GuiaDespacho.java`):

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long (PK) | Autogenerado |
| `numeroGuia` | Integer (unique) | Número único de guía |
| `fechaGeneracion` | LocalDate | Fecha de emisión |
| `cliente` | String | Nombre del cliente |
| `direccionEntrega` | String | Dirección de entrega |
| `descripcionCarga` | String | Descripción de la carga |
| `estado` | String | Estado de la guía |
| `rutaEfs` | String | Ruta en almacenamiento temporal |
| `rutaS3` | String | Ruta en bucket S3 |
| `transportista` | @ManyToOne | Relación con Transportista |

**Transportista.java** (`transportmanagement-consumer/src/main/java/com/duoc/transportmanagement/model/Transportista.java`):

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long (PK) | Autogenerado |
| `nombre` | String (@NotBlank) | Nombre del transportista |
| `rut` | String (@NotBlank) | RUT del transportista |
| `telefono` | String (@NotBlank) | Teléfono de contacto |
| `correo` | String (@NotBlank) | Correo electrónico |

#### 2.4.3 DTOs

El proyecto define 10 DTOs compartidos entre módulos para el intercambio de datos:

- `GuiaCreateDTO`, `GuiaUpdateDTO`, `GuiaDTO`, `GuiaResumenDTO`, `GuiaMessageDTO`
- `TransportistaDTO`, `TransportistaResumenDTO`, `TransportistaMessageDTO`
- `ArchivoDTO`, `AssetDTO`

### 2.5 Evidencia de cumplimiento

| Requisito | Estado | Evidencia |
|-----------|:------:|-----------|
| Microservicios en Spring Boot | ✅ | 2 módulos Spring Boot 3.5.14 con Java 21 |
| API REST con JSON | ✅ | 17 endpoints en Producer + 14 en Consumer, todos retornan JSON |
| BFF que orquesta llamadas a colas | ✅ | Producer actúa como BFF: lecturas HTTP a Consumer, escrituras vía RabbitMQ |
| 2 endpoints que llamen a cola (producir + consumir) | ✅ | POST /api/guias (produce), POST /api/rabbit/guia/procesar (consume) |
| CRUD completo | ✅ | Create, Read, Update, Delete para guías y transportistas |

---

## 3. Criterio 2 — Colas de Mensajes en RabbitMQ

### 3.1 Configuración de RabbitMQ

**Host:** `rabbitmq` (nombre del contenedor Docker dentro de la red `cloud-network`)  
**Puerto:** `5672`  
**Credenciales:** `guest/guest`  
**Imagen:** `rabbitmq:3-management`

### 3.2 Colas, Exchanges y Bindings

El sistema define **4 colas principales + 2 DLQ** para guías y transportistas:

#### Colas para Guías

| Componente | Nombre | Tipo | Propiedades |
|------------|--------|------|-------------|
| Exchange | `guia.exchange` | Direct | — |
| Cola principal | `guia.queue` | Durable | DLX → `guia.dlx.exchange` |
| DLX Exchange | `guia.dlx.exchange` | Direct | — |
| Dead Letter Queue | `guia.dlq` | Durable | — |
| Binding | `guia.queue` → `guia.exchange` | Routing key: `guia.queue` | — |
| DLQ Binding | `guia.dlq` → `guia.dlx.exchange` | Routing key: `guia.dlq` | — |

#### Colas para Transportistas

| Componente | Nombre | Tipo | Propiedades |
|------------|--------|------|-------------|
| Exchange | `transportista.exchange` | Direct | — |
| Cola principal | `transportista.queue` | Durable | DLX → `transportista.dlx.exchange` |
| DLX Exchange | `transportista.dlx.exchange` | Direct | — |
| Dead Letter Queue | `transportista.dlq` | Durable | — |
| Binding | `transportista.queue` → `transportista.exchange` | Routing key: `transportista.queue` | — |
| DLQ Binding | `transportista.dlq` → `transportista.dlx.exchange` | Routing key: `transportista.dlq` | — |

**Configuración en código** (`RabbitMQConfig.java` en ambos módulos):

```java
@Configuration
public class RabbitMQConfig {
    // Guia Exchange + Queue + DLX
    @Bean public DirectExchange guiaExchange() { return new DirectExchange("guia.exchange"); }
    @Bean public Queue guiaQueue() {
        return QueueBuilder.durable("guia.queue")
            .withArgument("x-dead-letter-exchange", "guia.dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "guia.dlq")
            .build();
    }
    @Bean public DirectExchange guiaDlxExchange() { return new DirectExchange("guia.dlx.exchange"); }
    @Bean public Queue guiaDlq() { return QueueBuilder.durable("guia.dlq").build(); }

    // Transportista Exchange + Queue + DLX (misma estructura)
    // ...

    // JSON Message Converter (serialización portable, no binario Java)
    @Bean public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTrustedPackages("*");
        converter.setTypePrecedence(Jackson2JsonMessageConverter.TypePrecedence.INFERRED);
        return converter;
    }
}
```

**Archivo de constantes** (`util/RabbitConstants.java` en ambos módulos):
```java
public class RabbitConstants {
    public static final String GUIA_QUEUE = "guia.queue";
    public static final String GUIA_DLQ = "guia.dlq";
    public static final String GUIA_EXCHANGE = "guia.exchange";
    public static final String GUIA_DLX_EXCHANGE = "guia.dlx.exchange";
    public static final String TRANSPORTISTA_QUEUE = "transportista.queue";
    public static final String TRANSPORTISTA_DLQ = "transportista.dlq";
    public static final String TRANSPORTISTA_EXCHANGE = "transportista.exchange";
    public static final String TRANSPORTISTA_DLX_EXCHANGE = "transportista.dlx.exchange";
}
```

### 3.3 Productores (Producer module)

**RabbitMQGuiaProducer.java** (`transportmanagement-producer/src/main/java/com/duoc/transportmanagement/service/RabbitMQGuiaProducer.java`):
```java
@Service
public class RabbitMQGuiaProducer {
    @Autowired private RabbitTemplate rabbitTemplate;

    public void sendMessage(GuiaMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitConstants.GUIA_QUEUE, message);
    }
}
```

**RabbitMQTransportistaProducer.java** (`transportmanagement-producer/.../ RabbitMQTransportistaProducer.java`):
```java
@Service
public class RabbitMQTransportistaProducer {
    @Autowired private RabbitTemplate rabbitTemplate;

    public void sendMessage(TransportistaMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitConstants.TRANSPORTISTA_QUEUE, message);
    }
}
```

**Flujo de envío (GuiaService en Producer):**
```java
@Service
public class GuiaService {
    private final RabbitMQGuiaProducer producer;

    public String createGuia(GuiaCreateDTO dto) {
        GuiaMessageDTO msg = new GuiaMessageDTO();
        msg.setOperacion("CREATE");
        msg.setGuiaCreate(dto);
        producer.sendMessage(msg);
        return "Solicitud para crear la guia enviada a RabbitMQ";
    }
    // Operaciones: CREATE, UPDATE, DELETE, UPLOAD_S3, UPDATE_S3, DELETE_S3
}
```

### 3.4 Consumidores (Consumer module)

**GuiaConsumer.java** (`transportmanagement-consumer/src/main/java/com/duoc/transportmanagement/listener/GuiaConsumer.java`):
```java
@Service
public class GuiaConsumer {
    @Autowired private GuiaService guiaService;

    @RabbitListener(queues = RabbitConstants.GUIA_QUEUE,
                    containerFactory = "rabbitListenerContainerFactory")
    public void receive(GuiaMessageDTO dto) {
        switch (dto.getOperacion()) {
            case "CREATE":     guiaService.createGuia(dto.getGuiaCreate()); break;
            case "UPDATE":     guiaService.updateGuia(dto.getId(), dto.getGuiaUpdate()); break;
            case "DELETE":     guiaService.deleteGuia(dto.getId()); break;
            case "UPLOAD_S3":  guiaService.subirArchivoS3(dto.getId()); break;
            case "UPDATE_S3":  guiaService.actualizarArchivoS3(dto.getId(), dto.getGuiaUpdate()); break;
            case "DELETE_S3":  guiaService.eliminarArchivoS3(dto.getId()); break;
            default: throw new IllegalArgumentException("Operación no válida: " + dto.getOperacion());
        }
    }
}
```

**TransportistaConsumer.java** (`transportmanagement-consumer/.../ TransportistaConsumer.java`):
```java
@Service
public class TransportistaConsumer {
    @Autowired private TransportistaService transportistaService;

    @RabbitListener(queues = RabbitConstants.TRANSPORTISTA_QUEUE,
                    containerFactory = "rabbitListenerContainerFactory")
    public void receive(TransportistaMessageDTO dto) {
        switch (dto.getOperacion()) {
            case "CREATE": transportistaService.saveTransportista(dto.getTransportistaDTO()); break;
            case "UPDATE": transportistaService.updateTransportista(dto.getId(), dto.getTransportistaDTO()); break;
            case "DELETE": transportistaService.deleteTransportista(dto.getId()); break;
            default: throw new IllegalArgumentException("Operación no soportada: " + dto.getOperacion());
        }
    }
}
```

### 3.5 Mecanismo de Dead Letter Queue (DLQ)

El Consumer configura un **RetryInterceptor** que limita los reintentos y envía mensajes fallidos a la DLQ:

```java
@Bean
public RetryOperationsInterceptor retryInterceptor(RabbitTemplate rabbitTemplate) {
    return RetryInterceptorBuilder.stateless()
        .maxAttempts(1)
        .recoverer(new RejectAndDontRequeueRecoverer())
        .build();
}
```

**Flujo de mensajes:**
```
Producer → guia.exchange → guia.queue → Consumer
                                    ↓ (error)
                              guia.dlx.exchange → guia.dlq
```

### 3.6 Despliegue de RabbitMQ en Docker

El pipeline CI/CD despliega RabbitMQ como contenedor Docker:

```yaml
docker run -d \
  --name rabbitmq \
  --network cloud-network \
  --restart unless-stopped \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

### 3.7 Evidencia de cumplimiento

| Requisito | Estado | Evidencia |
|-----------|:------:|-----------|
| Colas en RabbitMQ desplegadas en Docker | ✅ | Imagen `rabbitmq:3-management` en deploy.yml |
| Productores en Java | ✅ | `RabbitMQGuiaProducer.java` + `RabbitMQTransportistaProducer.java` |
| Consumidores en Java | ✅ | `GuiaConsumer.java` + `TransportistaConsumer.java` con `@RabbitListener` |
| Cola de guías | ✅ | `guia.queue` (durable, con DLX) |
| Cola de errores/DLQ | ✅ | `guia.dlq` + `transportista.dlq` |
| Serialización JSON | ✅ | `Jackson2JsonMessageConverter` con `INFERRED` |
| Retry + DLX configurado | ✅ | `RetryOperationsInterceptor` con maxAttempts=1 |

---

## 4. Criterio 3 — Identity as a Service — Azure AD B2C

### 4.1 Configuración de Azure AD B2C

La autenticación del backend se gestiona mediante **Azure AD B2C** con tokens JWT. Esta configuración aplica solo al microservicio Producer (punto de entrada securitizado).

**Archivo:** `transportmanagement-producer/src/main/resources/application.properties`

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://despachoservice2.b2clogin.com/despachoservice2.onmicrosoft.com/discovery/v2.0/keys?p=B2C_1_despacho_signin
```

**Datos de conexión a Azure AD B2C:**

| Elemento | Valor |
|----------|-------|
| Tenant | `despachoservice2.onmicrosoft.com` |
| Tenant ID | `5199d2b5-40ed-44c1-a8e5-f4a83132a743` |
| User Flow | `B2C_1_despacho_signin` (Sign up and sign in) |
| JWKS URI | `.../discovery/v2.0/keys?p=B2C_1_despacho_signin` |

### 4.2 Implementación de Spring Security

**SecurityConfig.java** (`transportmanagement-producer/src/main/java/com/duoc/transportmanagement/config/SecurityConfig.java`):

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(
            "https://despachoservice2.b2clogin.com/5199d2b5-40ed-44c1-a8e5-f4a83132a743/v2.0/"
        ));
        return decoder;
    }
}
```

**Características de la seguridad implementada:**

| Característica | Implementación |
|----------------|---------------|
| Autenticación JWT | `.oauth2ResourceServer().jwt()` |
| Validación de firma | `NimbusJwtDecoder` con JWK Set URI de Azure B2C |
| Validación de issuer | `JwtValidators.createDefaultWithIssuer(...)` |
| CSRF deshabilitado | `csrf.ignoringRequestMatchers("/h2-console/**")` |
| H2 Console permitido | `.requestMatchers("/h2-console/**").permitAll()` |
| Stateless | Por defecto en OAuth2 Resource Server |

### 4.3 Dependencias de seguridad

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

### 4.4 Flujo de autenticación

```
Cliente (Postman)
    │
    │  Authorization: Bearer <JWT-Azure>
    ▼
Producer (:8080)
    │
    ├── SecurityFilterChain
    │   ├── ¿JWT presente?         → NO  → 401 Unauthorized
    │   ├── ¿Firma válida (JWKS)? → NO  → 401 Unauthorized
    │   ├── ¿Issuer correcto?     → NO  → 401 Unauthorized
    │   └── ¿Autenticado?         → SÍ  → Endpoint procesa
    ▼
Controller (@RestController)
```

### 4.5 Evidencia de cumplimiento

| Requisito | Estado | Evidencia |
|-----------|:------:|-----------|
| IDaaS configurado (Azure AD B2C) | ✅ | Tenant `despachoservice2`, JWK Set URI configurado |
| Backend securitizado con Spring Security | ✅ | `SecurityConfig.java` con `.oauth2ResourceServer().jwt()` |
| Validación JWT (firma + issuer) | ✅ | `NimbusJwtDecoder` + `JwtValidators` |
| Protección de endpoints | ✅ | `.anyRequest().authenticated()` en Producer |
| Consumer interno sin autenticación | ✅ | Consumer no expone seguridad (uso interno) |

---

## 5. Criterio 4 — API Manager

### 5.1 Preparación para API Manager

El código está preparado para integrarse con un **AWS API Gateway** como API Manager:

1. **Todos los endpoints REST están definidos** en el Producer (17 endpoints) — listos para ser registrados
2. **El Producer acepta JWT en header Authorization** — compatible con autorizadores JWT de API Gateway
3. **Health check endpoint** disponible via Actuator (`/actuator/health`) para monitoreo

### 5.2 Configuración de Actuator (monitoreo)

```properties
management.endpoints.web.exposure.include=*
```

El endpoint `/actuator/health` está expuesto para que el API Gateway pueda verificar el estado del microservicio.

### 5.3 Endpoints a registrar en API Manager

**Producer (securitizados):**

| Método | Ruta | Propósito |
|--------|------|-----------|
| `POST` | `/api/guias` | Crear guía |
| `GET` | `/api/guias` | Listar guías |
| `GET` | `/api/guias/{id}` | Obtener guía |
| `PUT` | `/api/guias/{id}` | Modificar guía |
| `DELETE` | `/api/guias/{id}` | Eliminar guía |
| `GET` | `/api/guias/transportista/{id}` | Buscar por transportista |
| `GET` | `/api/guias/fecha/{fecha}` | Buscar por fecha |
| `POST` | `/api/guias/s3/{id}` | Subir a S3 |
| `PUT` | `/api/guias/s3/{id}` | Actualizar S3 |
| `GET` | `/api/guias/s3/{id}` | Descargar de S3 |
| `DELETE` | `/api/guias/s3/{id}` | Eliminar de S3 |
| `GET` | `/api/transportistas` | Listar transportistas |
| `GET` | `/api/transportistas/{id}` | Obtener transportista |
| `POST` | `/api/transportistas` | Crear transportista |
| `PUT` | `/api/transportistas/{id}` | Modificar transportista |
| `DELETE` | `/api/transportistas/{id}` | Eliminar transportista |

### 5.4 Arquitectura para API Gateway

```
Cliente (Postman/App)
    │
    │  Authorization: Bearer <JWT>
    ▼
┌───────────────────────────┐
│  AWS API Gateway          │
│  ┌─────────────────────┐  │
│  │ JWT Authorizer       │  │  ← Validación inicial JWT (Azure AD B2C)
│  │ (Issuer: Azure B2C)  │  │
│  └─────────────────────┘  │
│            │               │
│  ┌─────────────────────┐  │
│  │ 16 rutas registradas │  │  ← Proxy HTTP → EC2:8080
│  └─────────────────────┘  │
└───────────────────────────┘
    │
    │  JWT reenviado al backend
    ▼
┌───────────────────────────┐
│  EC2 — Producer (:8080)   │
│  Spring Security           │  ← Segunda validación JWT
│  (OAuth2 Resource Server)  │
└───────────────────────────┘
```

### 5.5 Evidencia de cumplimiento

| Requisito | Estado | Evidencia |
|-----------|:------:|-----------|
| Endpoints listos para registro | ✅ | 17 endpoints REST definidos en Producer |
| Compatible con autorizador JWT | ✅ | SecurityConfig acepta JWT Bearer |
| Health check disponible | ✅ | Actuator `/actuator/health` expuesto |
| Configuración API Gateway | ⚠️ | Requiere configuración en AWS Console (paso manual) |

---

## 6. Criterio 5 — Almacenamiento en la Nube — AWS S3

### 6.1 Configuración de AWS S3

**Archivo:** `transportmanagement-consumer/src/main/resources/application.properties`

```properties
aws.access-key=${AWS_ACCESS_KEY_ID}
aws.secret-key=${AWS_SECRET_ACCESS_KEY}
aws.session-token=${AWS_SESSION_TOKEN}
aws.region=us-east-1
aws.bucket.name=${AWS_S3_BUCKET:despacho-grupo3-bucket}
```

### 6.2 Configuración del cliente S3

**StorageConfig.java** (`transportmanagement-consumer/src/main/java/com/duoc/transportmanagement/config/StorageConfig.java`):
```java
@Configuration
public class StorageConfig {
    @Value("${aws.access-key}") private String accessKey;
    @Value("${aws.secret-key}") private String accessSecret;
    @Value("${aws.session-token}") private String sessionToken;
    @Value("${aws.region}") private String region;

    @Bean
    public AmazonS3 s3Client() {
        AWSCredentials credentials = new BasicSessionCredentials(accessKey, accessSecret, sessionToken);
        return AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(region).build();
    }
}
```

Se utiliza **AWS SDK v1** con credenciales de sesión (`BasicSessionCredentials`) compatibles con AWS Academy Learner Lab.

### 6.3 Repositorio S3

**S3Repository.java** (interfaz):
```java
public interface S3Repository {
    List<Asset> listObjectsInBucket(String bucket);
    S3ObjectInputStream getObject(String bucketName, String fileName) throws IOException;
    byte[] downloadFile(String bucketName, String fileName) throws IOException;
    void moveObject(String bucketName, String fileKey, String destinationFileKey);
    void deleteObject(String bucketName, String fileKey);
    String uploadFile(String bucketName, String fileName, File fileObj);
}
```

**S3RepositoryImpl.java** (implementación con `AmazonS3`):

Implementa todas las operaciones con el SDK de AWS S3:
- `listObjectsV2()` para listar archivos
- `getObject()` para obtener contenido
- `putObject()` para subir archivos
- `copyObject()` + `deleteObject()` para mover
- `deleteObject()` para eliminar

### 6.4 Integración con la lógica de negocio

**GuiaService.java** (Consumer) — Integración con S3:

```java
@Service
public class GuiaService {
    @Value("${aws.bucket.name}") private String bucketName;

    // Subir archivo a S3 con estructura organizada
    public void subirArchivoS3(Long id) {
        GuiaDespacho guia = guiaRepository.findById(id).orElseThrow(...);
        generarArchivo(id);  // Genera archivo temporal .txt
        String key = String.format("%d/%02d/%02d/%s/guia_%d.txt",
            LocalDate.now().getYear(), LocalDate.now().getMonthValue(),
            LocalDate.now().getDayOfMonth(),
            guia.getTransportista().getNombre(), guia.getNumeroGuia());
        File file = new File(guia.getRutaEfs());
        s3Repository.uploadFile(bucketName, key, file);
        guia.setRutaS3(key);
        guiaRepository.save(guia);
    }

    // Descargar archivo desde S3
    public byte[] descargarArchivo(Long id) {
        GuiaDespacho guia = guiaRepository.findById(id).orElseThrow(...);
        return s3Repository.downloadFile(bucketName, guia.getRutaS3());
    }

    // Eliminar archivo de S3
    public void eliminarArchivoS3(Long id) {
        GuiaDespacho guia = guiaRepository.findById(id).orElseThrow(...);
        if (guia.getRutaS3() != null) {
            s3Repository.deleteObject(bucketName, guia.getRutaS3());
            guia.setRutaS3(null);
            guiaRepository.save(guia);
        }
    }
}
```

### 6.5 Estructura de carpetas en S3

```
despacho-grupo3-bucket/
└── {YYYY}/
    └── {MM}/
        └── {DD}/
            └── {transportista-nombre}/
                └── guia_{numero}.txt
```

Ejemplo: `2025/07/15/Transportes del Sur/guia_1001.txt`

### 6.6 Controladores S3 expuestos (Consumer)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/s3/listS3Files?bucketName=X` | Listar archivos del bucket |
| `GET` | `/s3/getS3FileContent?bucketName=X&fileName=Y` | Ver contenido de archivo |
| `GET` | `/s3/downloadS3File?bucketName=X&filePath=Y&fileName=Z` | Descargar archivo |
| `POST` | `/s3/uploadFile?bucketName=X&filePath=Y` | Subir archivo (multipart) |
| `DELETE` | `/s3/deleteObject?bucketName=X&fileKey=Y` | Eliminar objeto |
| `GET` | `/s3/moveFile?bucketName=X&fileName=Y&fileNameDest=Z` | Mover archivo |

### 6.7 Evidencia de cumplimiento

| Requisito | Estado | Evidencia |
|-----------|:------:|-----------|
| Almacenamiento Cloud implementado | ✅ | AWS S3 con `AmazonS3` SDK v1 |
| Subida automática de archivos | ✅ | `GuiaService.subirArchivoS3()` → `s3Repository.uploadFile()` |
| Descarga de archivos | ✅ | `GuiaService.descargarArchivo()` → `s3Repository.downloadFile()` |
| Estructura de carpetas organizada | ✅ | Por año/mes/día/transportista |
| Eliminación de archivos en S3 | ✅ | `GuiaService.eliminarArchivoS3()` → `s3Repository.deleteObject()` |
| Credenciales AWS Academy | ✅ | `BasicSessionCredentials` con access key + secret + session token |

---

## 7. Criterio 6 — Despliegue Pipeline CI/CD

### 7.1 Pipeline de GitHub Actions

**Archivo:** `.github/workflows/deploy.yml`

El pipeline se ejecuta en cada push y pull request a la rama `main`.

### 7.2 Flujo del pipeline

```
Push a main
    │
    ▼
Checkout repository
    │
    ▼
Login DockerHub
    │
    ▼
Build Docker images
    ├── cursosonline-producer:latest
    └── cursosonline-consumer:latest
    │
    ▼
Push images to DockerHub
    │
    ▼
Configure AWS credentials
    │
    ▼
SSH into EC2
    ├── docker pull images
    ├── docker network create cloud-network
    ├── Deploy RabbitMQ (rabbitmq:3-management)
    ├── Deploy Producer (puerto 8080)
    │   └── Env vars: AWS creds, S3 bucket, consumer URL
    └── Deploy Consumer (puerto 8081)
        └── Env vars: AWS creds, S3 bucket, producer URL
    │
    ▼
Health check (curl a EC2:8080/actuator/health)
```

### 7.3 Dockerfiles

**Producer** (`transportmanagement-producer/Dockerfile`):
```dockerfile
FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Consumer** (`transportmanagement-consumer/Dockerfile`):
```dockerfile
# Misma estructura multi-stage, expone puerto 8081
EXPOSE 8081
```

### 7.4 Variables de entorno en producción

**Producer (en EC2):**
```bash
-e AWS_ACCESS_KEY_ID="$AWS_ACCESS_KEY" \
-e AWS_SECRET_ACCESS_KEY="$AWS_SECRET_KEY" \
-e AWS_SESSION_TOKEN="$AWS_SESSION_TOKEN" \
-e AWS_S3_BUCKET="$S3_BUCKET" \
-e AWS_EC2_URL_CONSUMER="http://cursosonline-consumer:8081"
```

**Consumer (en EC2):**
```bash
-e AWS_ACCESS_KEY_ID="$AWS_ACCESS_KEY" \
-e AWS_SECRET_ACCESS_KEY="$AWS_SECRET_KEY" \
-e AWS_SESSION_TOKEN="$AWS_SESSION_TOKEN" \
-e AWS_S3_BUCKET="$S3_BUCKET" \
-e AWS_EC2_URL_PRODUCER="http://cursosonline-producer:8080"
```

### 7.5 Red Docker

Los contenedores se comunican internamente mediante la red `cloud-network` de Docker, usando los nombres de los contenedores como hostnames:
- `cursosonline-producer:8080` ↔ `cursosonline-consumer:8081`
- Ambos → `rabbitmq:5672`

### 7.6 Secrets de GitHub requeridos

| Secret | Propósito |
|--------|-----------|
| `DOCKERHUB_USERNAME` | Usuario de DockerHub |
| `DOCKERHUB_TOKEN` | Token de acceso a DockerHub |
| `AWS_ACCESS_KEY_ID` | AWS Academy Learner Lab |
| `AWS_SECRET_ACCESS_KEY` | AWS Academy Learner Lab |
| `AWS_SESSION_TOKEN` | AWS Academy Learner Lab |
| `EC2_HOST` | IP elástica de la instancia EC2 |
| `USER_SERVER` | Usuario SSH (ec2-user) |
| `EC2_SSH_KEY` | Clave privada SSH (.pem) |

### 7.7 Evidencia de cumplimiento

| Requisito | Estado | Evidencia |
|-----------|:------:|-----------|
| Pipeline CI/CD funcional | ✅ | `deploy.yml` con build, push, deploy y health check |
| Docker para desarrollo | ✅ | Dockerfiles multi-stage para ambos módulos |
| Imágenes en DockerHub | ✅ | Push automático en el pipeline |
| Despliegue automático en EC2 | ✅ | SSH + docker pull + docker run |
| RabbitMQ en contenedor Docker | ✅ | `rabbitmq:3-management` desplegado en pipeline |
| Health check post-deploy | ✅ | `curl -sf http://EC2_HOST:8080/actuator/health` |
| Red interna Docker | ✅ | `cloud-network` con comunicación por hostname |

---

## 8. Criterio 7 — Documentación

### 8.1 Documentación incluida en el proyecto

| Elemento | Ubicación | Descripción |
|----------|-----------|-------------|
| Colección Postman | `transportmanagement.postman_collection.json` | 18 requests documentados |
| Pipeline CI/CD | `.github/workflows/deploy.yml` | Pipeline completo comentado |
| Dockerfiles | `transportmanagement-producer/Dockerfile`, `transportmanagement-consumer/Dockerfile` | Multi-stage build |
| Configuración | `application.properties` en ambos módulos | Con comentarios descriptivos |
| Ignore | `.gitignore` | Exclusiones estándar Java/Maven |

### 8.2 Colección Postman

La colección incluye **18 requests** agrupados por funcionalidad:

- **Transportistas:** POST, GET (CRUD)
- **Guías:** POST, GET, PUT, DELETE
- **S3:** Upload, Download, List, Delete
- **Generación EFS:** Generar guía en almacenamiento temporal

URL base: `http://54.86.122.135:8080` (IP elástica de EC2)

### 8.3 Evidencia de cumplimiento

| Requisito | Estado | Evidencia |
|-----------|:------:|-----------|
| Colección Postman documentada | ✅ | 18 requests con bodies de ejemplo |
| Pipeline documentado | ✅ | `deploy.yml` con comentarios de cada paso |
| Dockerfiles incluidos | ✅ | Multi-stage con Maven + JDK 21 |
| Código fuente organizado | ✅ | Paquetes: config, controller, dto, exception, listener, model, repository, service, util |

---

## 9. Criterio 8 — Video Explicativo

### 9.1 Guion propuesto para el video (8-10 minutos)

#### Minuto 0:00-1:00 — Introducción
- Presentación de los integrantes del equipo
- Nombre del proyecto: Plataforma de Gestión de Transporte Cloud Native
- Objetivo: Demostrar sistema de gestión de guías con microservicios, RabbitMQ, Azure AD B2C, S3 y CI/CD

#### Minuto 1:00-2:30 — Código fuente (GitHub + IDE)
- Mostrar estructura del proyecto Maven multi-módulo
- Destacar: producer (BFF securitizado), consumer (procesador de negocio)
- Dependencias clave en pom.xml

#### Minuto 2:30-4:00 — RabbitMQ
- Mostrar `docker ps` con el contenedor RabbitMQ
- Panel de administración (localhost:15672)
- Mostrar colas: `guia.queue`, `transportista.queue`, DLQs
- Exchanges y bindings configurados

#### Minuto 4:00-5:30 — Azure AD B2C
- Mostrar configuración en Azure Portal
- Tenant, App Registration, User Flow
- Demostrar obtención de JWT
- Mostrar SecurityConfig.java y validación JWT

#### Minuto 5:30-6:30 — API Gateway
- Mostrar rutas configuradas en AWS API Gateway
- Autorizador JWT conectado a Azure AD B2C
- URL de invocación del stage

#### Minuto 6:30-8:30 — Demostración en Postman
1. **POST /api/transportistas** — Crear transportista con JWT → 201
2. **POST /api/guias** — Crear guía (envío a RabbitMQ) → 202
3. **GET /api/guias** — Listar guías creadas → 200
4. **POST /api/guias/s3/{id}** — Subir guía a S3 → 200
5. **GET /api/guias/s3/{id}** — Descargar de S3 → 200
6. **Verificar S3** — Mostrar archivo en consola AWS
7. **DELETE /api/guias/{id}** — Eliminar guía → 204
8. **Sin token** — Mostrar 401 Unauthorized

#### Minuto 8:30-9:30 — CI/CD Pipeline
- Mostrar GitHub Actions ejecutándose
- Build → Push DockerHub → Deploy EC2
- Health check exitoso

#### Minuto 9:30-10:00 — Cierre
- Resumen de tecnologías utilizadas
- Conclusiones

### 9.2 Funcionalidades a demostrar

| Funcionalidad | Endpoint | Resultado esperado |
|---------------|----------|-------------------|
| Crear transportista | `POST /api/transportistas` | 200 OK |
| Crear guía | `POST /api/guias` | 202 (mensaje a cola) |
| Listar guías | `GET /api/guias` | 200 + array JSON |
| Buscar por transportista | `GET /api/guias/transportista/{id}` | 200 + array JSON |
| Subir a S3 | `POST /api/guias/s3/{id}` | 200 OK |
| Descargar de S3 | `GET /api/guias/s3/{id}` | 200 + archivo |
| Modificar guía | `PUT /api/guias/{id}` | 200 OK |
| Eliminar guía | `DELETE /api/guias/{id}` | 204 No Content |
| Sin autenticación | Cualquier endpoint | 401 Unauthorized |
| RabbitMQ colas | UI RabbitMQ | Mensajes fluyendo |
| S3 bucket | Consola AWS | Archivos en estructura de carpetas |
| Pipeline CI/CD | GitHub Actions | Build + Deploy exitoso |

---

## 10. Anexo: Estructura Completa del Proyecto

```
cursos_online/
├── .github/
│   └── workflows/
│       └── deploy.yml                          # CI/CD Pipeline
├── .gitignore
├── pom.xml                                      # Parent POM (multi-módulo)
├── transportmanagement.postman_collection.json  # Colección Postman (18 requests)
│
├── transportmanagement-producer/                # BFF — Puerto 8080
│   ├── Dockerfile                               # Multi-stage (Maven + JDK 21)
│   ├── pom.xml                                  # + Spring Security, OAuth2
│   └── src/main/
│       ├── resources/
│       │   └── application.properties           # Azure AD B2C + RabbitMQ + AWS
│       └── java/com/duoc/transportmanagement/
│           ├── TransportmanagementApplication.java
│           ├── config/
│           │   ├── RabbitMQConfig.java           # Colas, exchanges, DLQ, JSON converter
│           │   ├── RestTemplateConfig.java        # HTTP client → Consumer
│           │   └── SecurityConfig.java            # Azure AD B2C JWT validation
│           ├── controller/
│           │   ├── GuiaController.java            # 12 endpoints REST
│           │   └── TransportistaController.java   # 5 endpoints REST
│           ├── dto/
│           │   ├── GuiaCreateDTO.java
│           │   ├── GuiaDTO.java
│           │   ├── GuiaMessageDTO.java            # Mensaje para RabbitMQ (incluye operación)
│           │   ├── GuiaResumenDTO.java
│           │   ├── GuiaUpdateDTO.java
│           │   ├── TransportistaDTO.java
│           │   ├── TransportistaMessageDTO.java
│           │   └── TransportistaResumenDTO.java
│           ├── exception/
│           │   ├── GlobalExceptionHandler.java
│           │   └── ResourceNotFoundException.java
│           ├── service/
│           │   ├── ConsumerClient.java            # HTTP calls to Consumer
│           │   ├── GuiaService.java               # Orquestación: HTTP + RabbitMQ
│           │   ├── RabbitMQGuiaProducer.java      # Producer → guia.queue
│           │   ├── RabbitMQTransportistaProducer.java  # Producer → transportista.queue
│           │   └── TransportistaService.java
│           └── util/
│               └── RabbitConstants.java           # Nombres de colas/exchanges/DLQ
│
└── transportmanagement-consumer/                # Procesador — Puerto 8081
    ├── Dockerfile                               # Multi-stage (Maven + JDK 21)
    ├── pom.xml                                  # Sin Spring Security
    └── src/main/
        ├── resources/
        │   └── application.properties           # server.port=8081, H2, RabbitMQ, AWS
        └── java/com/duoc/transportmanagement/
            ├── TransportmanagementApplication.java
            ├── config/
            │   ├── RabbitMQConfig.java           # Colas, DLX, retry interceptor
            │   └── StorageConfig.java             # AWS S3 client
            ├── controller/
            │   ├── GuiaController.java            # 5 endpoints (lectura)
            │   ├── RabbitMQController.java        # 2 endpoints (procesamiento manual)
            │   ├── S3Controller.java              # 6 endpoints (gestión S3)
            │   └── TransportistaController.java   # 2 endpoints (lectura)
            ├── dto/
            │   ├── ArchivoDTO.java
            │   ├── AssetDTO.java
            │   ├── GuiaCreateDTO.java
            │   ├── GuiaDTO.java
            │   ├── GuiaMessageDTO.java
            │   ├── GuiaResumenDTO.java
            │   ├── GuiaUpdateDTO.java
            │   ├── TransportistaDTO.java
            │   ├── TransportistaMessageDTO.java
            │   └── TransportistaResumenDTO.java
            ├── exception/
            │   ├── GlobalExceptionHandler.java
            │   └── ResourceNotFoundException.java
            ├── listener/
            │   ├── GuiaConsumer.java              # @RabbitListener → guia.queue
            │   └── TransportistaConsumer.java     # @RabbitListener → transportista.queue
            ├── model/
            │   ├── Asset.java
            │   ├── GuiaDespacho.java               # Entidad JPA (JpaRepository)
            │   └── Transportista.java              # Entidad JPA
            ├── repository/
            │   ├── GuiaRepository.java             # JPA + queries personalizadas
            │   ├── S3Repository.java               # Interfaz S3
            │   ├── S3RepositoryImpl.java           # Implementación AmazonS3
            │   └── TransportistaRepository.java    # JPA CRUD
            ├── service/
            │   ├── AwsService.java                 # Interfaz S3
            │   ├── AwsServiceImpl.java             # Implementación S3
            │   ├── GuiaService.java                # Lógica de negocio completa (404 líneas)
            │   ├── RabbitAdminService.java         # Admin dinámico RabbitMQ
            │   ├── RabbitAdminServiceImpl.java     # Implementación AmqpAdmin
            │   └── TransportistaService.java       # CRUD transportistas
            └── util/
                └── RabbitConstants.java

Total: ~50 archivos Java de código fuente
      2 microservicios Spring Boot
      2 Dockerfiles
      1 pipeline CI/CD
      1 colección Postman (18 requests)
```

---

## Resumen de Cumplimiento por Criterio

| # | Criterio | Puntaje Máx. | Estado |
|---|----------|:------------:|:------:|
| 1 | Microservicios backend en Spring Boot | 20 pts | ✅ Implementado |
| 2 | Colas de mensajes en RabbitMQ | 15 pts | ✅ Implementado |
| 3 | Identity as a Service (Azure AD B2C) | 15 pts | ✅ Implementado |
| 4 | API Manager | 10 pts | ⚠️ Código listo, requiere configuración manual |
| 5 | Almacenamiento en la nube (AWS S3) | 10 pts | ✅ Implementado |
| 6 | Despliegue pipeline CI/CD | 10 pts | ✅ Implementado |
| 7 | Documentación | 10 pts | ✅ Este informe + Postman + código documentado |
| 8 | Video explicativo | 10 pts | 📋 Pendiente de grabación |

---

*Documento generado automáticamente a partir del análisis del código fuente del proyecto `cursos_online`.*  
*Evaluación Final Transversal — Desarrollo Cloud Native (CDY2204) — Semana 9*
