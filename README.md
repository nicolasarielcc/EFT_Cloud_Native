# Plataforma de Gestión de Transporte — Cursos Online

**Asignatura:** CDY2204 — Desarrollo Cloud Native (Duoc UC)
**Evaluación Final Transversal — Semana 9**

Plataforma Cloud Native de gestión de transporte compuesta por dos microservicios Spring Boot que se comunican de forma asíncrona mediante RabbitMQ (patrón productor-consumidor).

| Componente | Tecnología |
|---|---|
| Backend | Spring Boot 3.5.14 + Java 21 |
| Mensajería | RabbitMQ (Docker) — colas + DLQ |
| IDaaS | Azure AD B2C — roles admin / consulta |
| Almacenamiento | AWS S3 — archivos de guías |
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
                     │  ├─ GuiaService + TransportistaService     │
                     │  └─ AWS S3 (subida/descarga/eliminación)  │
                     │                                          │
                     └─────────────────────────────────────────┘
```

**Patrón de comunicación:**
- Lecturas (GET): síncronas via `RestTemplate` → Consumer (:8081)
- Escrituras (POST/PUT/DELETE): asíncronas via RabbitMQ → Consumer procesa

---

## Estructura del Proyecto

```
cursos_online/
├── pom.xml                                  # Maven multi-módulo
├── transportmanagement-producer/            # BFF — API REST + Security
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/duoc/transportmanagement/
│       ├── TransportmanagementApplication.java
│       ├── config/SecurityConfig.java       # JWT Azure AD B2C
│       ├── controller/                      # GuiaController + TransportistaController
│       ├── dto/                             # GuiaDTO, TransportistaDTO, etc.
│       ├── service/                         # ConsumerClient, GuiaProducerService
│       └── rabbitmq/                        # RabbitMQConfig, Producer
├── transportmanagement-consumer/            # Lógica de negocio + BD + S3
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/duoc/transportmanagement/
│       ├── TransportmanagementApplication.java
│       ├── config/S3Config.java             # Cliente AWS S3
│       ├── controller/                      # GuiaController, TransportistaController
│       ├── model/                           # Entidades JPA
│       ├── repository/                      # Repositorios JPA
│       ├── service/                         # GuiaService, S3Service
│       └── rabbitmq/                        # RabbitMQConfig, Consumer
├── .github/workflows/deploy.yml             # Pipeline CI/CD
└── docker-compose.yml
```

---

## API Endpoints

### Guías de Despacho (`/api/guias`)

| Método | Ruta | Descripción | Comunicación |
|---|---|---|---|
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

### Transportistas (`/api/transportistas`)

| Método | Ruta | Descripción | Comunicación |
|---|---|---|---|
| `GET` | `/api/transportistas` | Listar transportistas | HTTP → Consumer |
| `GET` | `/api/transportistas/{id}` | Obtener transportista | HTTP → Consumer |
| `POST` | `/api/transportistas` | Crear transportista | RabbitMQ (async) |
| `PUT` | `/api/transportistas/{id}` | Modificar transportista | RabbitMQ (async) |
| `DELETE` | `/api/transportistas/{id}` | Eliminar transportista | RabbitMQ (async) |

**Total: 16 endpoints REST**

### Ejemplos de requests

**Crear guía:**
```json
POST /api/guias
{
  "codigo": "GUIA-001",
  "transportista": "Transportes Rápidos SpA",
  "fechaDespacho": "2025-06-15T10:30:00",
  "direccionOrigen": "Av. Providencia 1234, Santiago",
  "direccionDestino": "Av. Apoquindo 5678, Las Condes",
  "descripcionCarga": "Equipos electrónicos — 3 pallets",
  "estado": "PENDIENTE"
}
```

**Crear transportista:**
```json
POST /api/transportistas
{
  "nombre": "Transportes Rápidos SpA",
  "rut": "76.123.456-K",
  "direccion": "Av. Los Leones 456, Providencia",
  "telefono": "+56912345678",
  "email": "contacto@trapidospa.cl"
}
```

---

## RabbitMQ — Colas y Mensajería

```
Exchange: exchange-guias (direct)
  ├── routing key "guia.nueva"   → cola-guias-principal (con DLX)
  └── DLX: dlx-exchange          → cola-guias-dlq
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

### Azure AD B2C

| Config | Valor |
|---|---|
| Tenant | `despachoservice2.onmicrosoft.com` |
| Tenant ID | `5199d2b5-40ed-44c1-a8e5-f4a83132a743` |
| App | `despacho-service-api2` |
| Client ID | `0a27f262-a186-457f-b7b5-eb43b284cd4c` |
| User Flow | `B2C_1_despacho_signin` |
| Usuario admin | `nikocarambas@gmail.com` |
| Usuario consulta | `ni.cavieres@duocuc.cl` |

### AWS S3

```
Bucket: despacho-grupo3-bucket (us-east-1)
Estructura: guias/{transportista}/{año}/{mes}/guia-{codigo}.pdf
```

### Variables de entorno (Producer)

| Variable | Descripción |
|---|---|
| `AWS_ACCESS_KEY_ID` | AWS Academy → AWS Details |
| `AWS_SECRET_ACCESS_KEY` | AWS Academy → AWS Details |
| `AWS_SESSION_TOKEN` | AWS Academy → AWS Details |
| `AWS_S3_BUCKET` | `despacho-grupo3-bucket` |
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
cd transportmanagement-consumer && mvn spring-boot:run

# 4. Ejecutar Producer (puerto 8080)
cd transportmanagement-producer && mvn spring-boot:run
```

- Producer: http://localhost:8080
- Consumer: http://localhost:8081
- RabbitMQ UI: http://localhost:15672

### Con Docker

```bash
# Producer
docker build -t cursosonline-producer ./transportmanagement-producer
docker run -d --name cursosonline-producer -p 8080:8080 \
  -e AWS_EC2_URL_CONSUMER="http://host.docker.internal:8081" \
  cursosonline-producer

# Consumer
docker build -t cursosonline-consumer ./transportmanagement-consumer
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
| `S3_BUCKET_NAME` | `despacho-grupo3-bucket` |

---

## API Gateway + Lambda Authorizer

| Componente | Valor |
|---|---|
| API ID | `0ljc9aux4k` |
| URL | `https://0ljc9aux4k.execute-api.us-east-1.amazonaws.com/produccion` |
| Authorizer | Custom Lambda (`b2cJwtAuthorizer`) |
| Lambda Runtime | Python 3.12 |

### Probar con JWT

```bash
curl -H "Authorization: Bearer <TOKEN>" \
  https://0ljc9aux4k.execute-api.us-east-1.amazonaws.com/produccion/api/guias
```

### Endpoints directos EC2 (sin seguridad)

```bash
curl http://54.86.122.135:8080/api/guias
curl http://54.86.122.135:8080/actuator/health
```

---

## SSH a EC2

```bash
ssh -i despacho-key.pem ec2-user@54.86.122.135
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
