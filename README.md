# AgroPulse Backend

API REST para el sistema IoT de monitoreo de invernaderos. Construido con Java 17 + Spring Boot 3.2.5, desplegado en Render con Supabase (PostgreSQL) como base de datos de producción.

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Seguridad | Spring Security |
| Persistencia | Spring Data JPA + Hibernate |
| BD producción | Supabase (PostgreSQL) vía `SUPABASE_JDBC_URL` |
| BD desarrollo | SQLite local |
| Notificaciones | SendGrid (email) + Resilience4j (circuit breaker) |
| Contenedor | Docker |
| Despliegue | Render |

## Estructura del proyecto

```
src/main/java/com/agropulse/
├── api/                        # Controllers REST
│   ├── AuthController          # POST /api/auth/register, /login
│   ├── GreenhouseController    # CRUD invernaderos
│   ├── SensorController        # CRUD sensores
│   ├── ActuatorController      # CRUD + control actuadores
│   ├── ReadingController       # Lecturas de sensores
│   ├── AlertController         # Alertas y anomalías
│   ├── DeviceController        # Registro de dispositivos ESP32
│   ├── RuleController          # Reglas de automatización
│   ├── ReportController        # Generación de reportes
│   ├── TicketController        # Sistema de tickets
│   ├── LogController           # Logs del sistema
│   └── dto/                    # Data Transfer Objects
├── model/                      # Entidades JPA
├── dao/                        # Repositorios Spring Data
├── service/                    # Lógica de negocio
│   ├── AnomalyDetectionService # Detección de anomalías en sensores
│   ├── NotificationService     # Envío de alertas por email
│   ├── DeviceService           # Gestión de dispositivos IoT
│   └── OwnershipService        # Control de acceso por propietario
├── config/                     # Spring Security, AppConfig
├── pattern/                    # Patrones de diseño GoF
│   ├── creational/             # Singleton, Factory, Builder, Prototype
│   ├── structural/             # Adapter, Bridge, Decorator, Facade, Proxy
│   └── behavioral/             # Observer, Strategy, Command, etc.
└── structure/                  # Estructuras de datos
    ├── array/                  # Buffer circular de sensores
    ├── linkedlist/             # Lista enlazada simple
    ├── doublylinkedlist/       # Historial de lecturas
    ├── circulardoublylinkedlist/ # Planificación de riego
    ├── queue/                  # Cola de irrigación
    └── stack/                  # Pila de alertas
```

## Variables de entorno

```env
# Base de datos (Supabase en producción)
SUPABASE_JDBC_URL=jdbc:postgresql://...
DB_USERNAME=postgres
DB_PASSWORD=...

# JWT
JWT_SECRET=...
JWT_EXPIRATION_MS=86400000

# Notificaciones
SENDGRID_API_KEY=...
SENDGRID_FROM_EMAIL=...

# Frontend (CORS)
FRONTEND_URL=https://agropulse.vercel.app
```

> **Nunca hardcodear** URLs, credenciales ni API keys en el código. Usar siempre las variables de entorno de Render.

## Correr en local

**Prerequisitos:** Java 17, Maven 3.8+

```bash
# 1. Clonar y configurar variables de entorno en application.properties o .env
cd "backend AgroPulse"

# 2. Compilar y ejecutar (usa SQLite local automáticamente si no hay SUPABASE_JDBC_URL)
mvn spring-boot:run

# 3. La API queda disponible en:
# http://localhost:8080/api
```

## Build con Docker

```bash
docker build -t agropulse-backend .
docker run -p 8080:8080 --env-file .env agropulse-backend
```

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Registrar usuario |
| POST | `/api/auth/login` | Login → JWT |
| GET | `/api/greenhouses` | Listar invernaderos del usuario |
| GET/POST | `/api/sensors` | Sensores |
| GET/POST | `/api/actuators` | Actuadores |
| POST | `/api/readings` | Recibir lecturas del ESP32 |
| GET | `/api/alerts` | Alertas activas |
| GET | `/api/devices` | Dispositivos registrados |
| POST | `/api/devices/{id}/command` | Enviar comando a ESP32 |

## Contexto académico

Este backend fue desarrollado para la materia **Patrones de Software y Estructuras de Datos** (Prof. Jhonatan Mideros, Universidad Cooperativa de Colombia). Implementa los 23 patrones GoF y las estructuras de datos del curso aplicadas al dominio de invernaderos inteligentes.
