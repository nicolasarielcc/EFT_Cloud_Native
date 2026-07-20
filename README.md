# CursosOnline — Plataforma de Gestión de Cursos en Línea

**Asignatura:** CDY2204 — Desarrollo Cloud Native (Duoc UC)
**Evaluación Final Transversal — Semana 9**

Plataforma Cloud Native de gestión de cursos en línea: los **estudiantes** pueden inscribirse a cursos, acceder al contenido y rendir exámenes; los **instructores** gestionan sus cursos y las calificaciones en tiempo real. Compuesta por dos microservicios Spring Boot que se comunican de forma asíncrona mediante RabbitMQ (patrón productor-consumidor).

| Componente | Tecnología |
|---|---|
| Backend | Spring Boot 3.5.14 + Java 21 |
| Mensajería | RabbitMQ (Docker) — colas + DLQ |
| IDaaS | Azure AD B2C — roles instructor / estudiante |
| Almacenamiento | AWS S3 — certificados de aprobación |
| API Manager | AWS API Gateway + Lambda Authorizer JWT |
| Base de datos | H2 en memoria |
| CI/CD | GitHub Actions → Docker Hub → AWS EC2 |

---

## Arquitectura

```
                    ┌─────────────────────────────────────────┐
                    │       Docker Network: cloud-network       │
                    │                                          │
  Cliente ──(JWT)──▶   Producer (:8080)                        │
  (Postman)          │  ├─ SecurityConfig (Azure AD B2C)        │
                     │  ├─ RabbitMQProducer ──▶ RabbitMQ        │
                     │  └─ RestTemplate ──(HTTP GET)──┐         │
                     │                                │         │
                     │                    RabbitMQ   │         │
                     │                   (rabbitmq)  │         │
                     │                                │         │
                     │  Consumer (:8081) ◀──listen─────┘         │
                     │  ├─ RabbitMQListener ◀──────────┘         │
                     │  ├─ H2 Database (JPA)                      │
                     │  ├─ CursoService + InscripcionService      │
                     │  └─ AWS S3 (certificados)                  │
                     │                                          │
                     └─────────────────────────────────────────┘
```

**Patrón de comunicación:**
- Lecturas (GET): síncronas via `RestTemplate` → Consumer (:8081)
- Escrituras (POST/PUT/DELETE): asíncronas via RabbitMQ → Consumer procesa (calificaciones en tiempo real)

---

## Modelo de Dominio

| Entidad | Descripción |
|---|---|
| **Curso** | Curso en línea creado y gestionado por un instructor (nombre, instructor, categoría, correo) |
| **Inscripcion** | Inscripción de un estudiante a un curso. Registra estado (`INSCRITA`, `COMPLETADA`, `ANULADA`) y calificación del examen. Al aprobar, se genera un **certificado** que se almacena en AWS S3 |

---

## Estructura del Proyecto

```
cursos_online/
├── pom.xml                                  # Maven multi-módulo
├── cursosonline-producer/                   # BFF — API REST + Security
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/duoc/cursosonline/
│       ├── CursosonlineApplication.java
│       ├── config/SecurityConfig.java       # JWT Azure AD B2C
│       ├── controller/                      # CursoController + InscripcionController
│       ├── dto/                             # CursoDTO, InscripcionDTO, etc.
│       ├── service/                         # ConsumerClient, Producers RabbitMQ
│       └── util/                            # RabbitConstants
├── cursosonline-consumer/                   # Lógica de negocio + BD + S3
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/duoc/cursosonline/
│       ├── CursosonlineApplication.java
│       ├── config/StorageConfig.java        # Cliente AWS S3
│       ├── controller/                      # CursoController, InscripcionController, S3Controller
│       ├── listener/                        # CursoConsumer, InscripcionConsumer
│       ├── model/                           # Entidades JPA (Curso, Inscripcion)
│       ├── repository/                      # Repositorios JPA + S3
│       └── service/                         # CursoService, InscripcionService, AwsService
├── .github/workflows/deploy.yml             # Pipeline CI/CD
└── cursosonline.postman_collection.json
```

---

## API Endpoints

### Inscripciones (`/api/inscripciones`) — estudiantes

| Método | Ruta | Descripción | Comunicación |
|---|---|---|---|
| `GET` | `/api/inscripciones` | Listar inscripciones | HTTP → Consumer |
| `GET` | `/api/inscripciones/{id}` | Obtener inscripción por ID | HTTP → Consumer |
| `POST` | `/api/inscripciones` | Inscribir estudiante a un curso | RabbitMQ (async) |
| `PUT` | `/api/inscripciones/{id}` | Registrar calificación / cambiar estado | RabbitMQ (async) |
| `DELETE` | `/api/inscripciones/{id}` | Anular inscripción | RabbitMQ (async) |
| `GET` | `/api/inscripciones/curso/{id}` | Inscripciones por curso | HTTP → Consumer |
| `GET` | `/api/inscripciones/fecha/{fecha}` | Inscripciones por fecha | HTTP → Consumer |
| `POST` | `/api/inscripciones/certificado/{id}` | Generar y subir certificado a S3 | RabbitMQ (async) |
| `PUT` | `/api/inscripciones/certificado/{id}` | Actualizar certificado en S3 | RabbitMQ (async) |
| `GET` | `/api/inscripciones/certificado/{id}` | Descargar certificado de S3 | HTTP → Consumer |
| `DELETE` | `/api/inscripciones/certificado/{id}` | Eliminar certificado de S3 | RabbitMQ (async) |

### Cursos (`/api/cursos`) — instructores

| Método | Ruta | Descripción | Comunicación |
|---|---|---|---|
| `GET` | `/api/cursos` | Listar cursos | HTTP → Consumer |
| `GET` | `/api/cursos/{id}` | Obtener curso | HTTP → Consumer |
| `POST` | `/api/cursos` | Crear curso | RabbitMQ (async) |
| `PUT` | `/api/cursos/{id}` | Modificar curso | RabbitMQ (async) |
| `DELETE` | `/api/cursos/{id}` | Eliminar curso | RabbitMQ (async) |

**Total: 16 endpoints REST**

### Ejemplos de requests

**Crear curso (instructor):**
```json
POST /api/cursos
{
  "nombre": "Desarrollo Cloud Native",
  "instructor": "Nicolas Cavieres",
  "categoria": "Tecnologia",
  "correoInstructor": "ni.cavieres@duocuc.cl"
}
```

**Inscribir estudiante:**
```json
POST /api/inscripciones
{
  "numeroInscripcion": 1001,
  "estudiante": "Juan Soto",
  "correoEstudiante": "juan.soto@duocuc.cl",
  "cursoId": 1
}
```

**Registrar calificación del examen (instructor, tiempo real):**
```json
PUT /api/inscripciones/1
{
  "estudiante": "Juan Soto",
  "correoEstudiante": "juan.soto@duocuc.cl",
  "calificacion": 6.5,
  "estado": true
}
```
> `estado: true` → `COMPLETADA`, `estado: false` → `ANULADA`. Al completarse, se puede generar el certificado en S3.

---

## RabbitMQ — Colas y Mensajería

```
Exchange: curso.exchange (direct)
  └── routing key "curso.queue"        → curso.queue (con DLX)
Exchange: inscripcion.exchange (direct)
  └── routing key "inscripcion.queue"  → inscripcion.queue (con DLX)
DLX: curso.dlx.exchange / inscripcion.dlx.exchange → *.dlq
```

### Levantar con Docker

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

UI de administración: http://localhost:15672 (guest/guest)

---

## Configuración

### Requisitos Previos

| Herramienta | Versión |
|---|---|
| Java JDK | 21 |
| Maven | 3.8+ |
| Docker | 24+ |
| AWS Academy | Learner Lab |
| Azure | AD B2C Tenant |

### Azure AD B2C (IDaaS)

| Config | Valor |
|---|---|
| Tenant | `cursosonlineb2c.onmicrosoft.com` |
| App | `cursosonline-api` |
| User Flow | `B2C_1_cursosonline_signin` |
| Roles | `instructor` (gestiona cursos y calificaciones) / `estudiante` (se inscribe y consulta) |

> Actualizar `jwk-set-uri` e `issuer-uri` en `cursosonline-producer/src/main/resources/application.properties` con los valores del tenant propio.

### AWS S3

```
Bucket: cursosonline-grupo3-bucket (us-east-1)
Estructura: certificados/{año}/{mes}/{dia}/{curso}/certificado_{numeroInscripcion}.txt
```

### Variables de entorno (Producer)

| Variable | Descripción |
|---|---|
| `AWS_ACCESS_KEY_ID` | AWS Academy → AWS Details |
| `AWS_SECRET_ACCESS_KEY` | AWS Academy → AWS Details |
| `AWS_SESSION_TOKEN` | AWS Academy → AWS Details |
| `AWS_S3_BUCKET` | `cursosonline-grupo3-bucket` |
| `AWS_EC2_URL_CONSUMER` | `http://cursosonline-consumer:8081` |

---

## Ejecución

### Desarrollo local

```bash
# 1. RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 2. Construir módulos
mvn clean install -DskipTests

# 3. Ejecutar Consumer (puerto 8081)
cd cursosonline-consumer && mvn spring-boot:run

# 4. Ejecutar Producer (puerto 8080)
cd cursosonline-producer && mvn spring-boot:run
```

- Producer: http://localhost:8080
- Consumer: http://localhost:8081
- RabbitMQ UI: http://localhost:15672

### Con Docker

```bash
# Producer
docker build -t cursosonline-producer ./cursosonline-producer
docker run -d --name cursosonline-producer -p 8080:8080 \
  -e AWS_EC2_URL_CONSUMER="http://host.docker.internal:8081" \
  cursosonline-producer

# Consumer
docker build -t cursosonline-consumer ./cursosonline-consumer
docker run -d --name cursosonline-consumer -p 8081:8081 \
  cursosonline-consumer
```

---

## CI/CD Pipeline

El pipeline en `.github/workflows/deploy.yml`:

1. Push a `main` → se gatilla
2. Build de imágenes `cursosonline-producer` y `cursosonline-consumer`
3. Push a DockerHub
4. SSH a EC2 → pull de imágenes → deploy contenedores + RabbitMQ
5. Health check en `:8080/actuator/health`

### GitHub Secrets requeridos

| Secret | Descripción |
|---|---|
| `DOCKERHUB_USERNAME` | Usuario Docker Hub |
| `DOCKERHUB_TOKEN` | Token de acceso Docker Hub |
| `EC2_HOST` | IP pública de EC2 |
| `EC2_SSH_KEY` | Llave privada .pem |
| `USER_SERVER` | Usuario EC2 (`ec2-user`) |
| `AWS_ACCESS_KEY_ID` | AWS Academy |
| `AWS_SECRET_ACCESS_KEY` | AWS Academy |
| `AWS_SESSION_TOKEN` | AWS Academy |

### Variables de Actions

| Variable | Valor |
|---|---|
| `S3_BUCKET_NAME` | `cursosonline-grupo3-bucket` |

---

## API Gateway + Lambda Authorizer (API Manager)

| Componente | Valor |
|---|---|
| API ID | `0ljc9aux4k` |
| URL | `https://0ljc9aux4k.execute-api.us-east-1.amazonaws.com/produccion` |
| Authorizer | Custom Lambda (`b2cJwtAuthorizer`) |
| Lambda Runtime | Python 3.12 |

### Probar con JWT

```bash
curl -H "Authorization: Bearer <TOKEN>" \
  https://0ljc9aux4k.execute-api.us-east-1.amazonaws.com/produccion/api/cursos
```

### Endpoints directos EC2 (sin seguridad)

```bash
curl http://54.86.122.135:8080/api/cursos
curl http://54.86.122.135:8080/actuator/health
```

---

## SSH a EC2

```bash
ssh -i cursosonline-key.pem ec2-user@54.86.122.135
```

---

## Tecnologías

- Java 21
- Spring Boot 3.5.14
- Spring Security + OAuth2 Resource Server (JWT)
- Spring AMQP (RabbitMQ)
- Spring Data JPA
- Spring Cloud AWS 2.3.0
- Docker + Docker Compose
- GitHub Actions
- AWS EC2, S3, API Gateway, Lambda
- Azure AD B2C
- RabbitMQ 3
- H2 Database
