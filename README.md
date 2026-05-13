# Vibe: Backend Core

> **Note:** For the full system architecture, high-level performance metrics, and the complete Vibe ecosystem overview,
> please visit our [Vibe Organization README](https://github.com/Vibe-Shared-real-time-experiences).*

Welcome to the backend engine of the Vibe real-time chat platform. This repository contains the core monolithic Spring
Boot application responsible for routing messages, maintaining WebSocket connections, and managing the asynchronous data
ingestion pipeline.

## Core Tech Stack & Infrastructure

Our backend is built for high concurrency and relies on the following core technologies:

| Layer | Technology |
|-------|-----------|
| **Language & Framework** | Java 21+, Spring Boot 4.0.1 |
| **Primary Database** | PostgreSQL |
| **In-Memory Store & Message Broker** | Redis |
| **Object Storage** | MinIO (S3-compatible) |
| **Real-time Communication** | WebSocket with STOMP |
| **Security** | Spring Security with OAuth2 & JWT |
| **API Documentation** | SpringDoc OpenAPI / Swagger UI |
| **Database Migration** | Flyway |
| **Containerization** | Docker & Docker Compose |

## Codebase Navigation

Here is the directory structure of our backend application:

```text
src/main/java/vn/vibeteam/vibe/
├── app/                     # Benchmark utilities and application initialization
├── common/                  # Shared constants, enums, and data structures
├── configuration/           # Framework configurations (Security, WebSocket, Redis, S3, etc.)
├── controller/              # REST API endpoints (auth, chat, media, user)
├── dto/                     # Request, Response, and Event objects
├── exception/               # Custom exception types and global error handler
├── model/                   # JPA entity classes
├── repository/              # Database access layer (Spring Data JPA)
├── service/                 # Business logic and domain services
├── util/                    # Utility and helper classes
└── worker/                  # Background asynchronous workers

src/main/resources/
├── application.yaml         # Spring Boot configuration
├── db/migration/            # Flyway SQL migrations

docker-compose.yml          # Local dev infrastructure
```

## Getting Started

Follow these instructions to set up the Vibe backend environment on your local machine.

### Prerequisites

- **Java 21** or higher installed
- **Maven 3.8.1** or higher installed
- **Docker & Docker Compose** installed
- **Git** for version control

### 1. Clone the Repository

```bash
git clone https://github.com/Vibe-Shared-real-time-experiences/vibe-backend.git
cd vibe
```

### 2. Start the Infrastructure

We provide a complete `docker-compose.yml` file with PostgreSQL, Redis, and MinIO all pre-configured.

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** on port `5432` (database: `vibe_db`)
- **Redis** on port `6379`
- **MinIO** on port `9000` (Console: `http://localhost:9001`)

### 3. Configure Environment Variables

Create a `.env` file in the project root from the provided `example.env`:

```bash
cp example.env .env
```

Edit `.env` with your configuration:

```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/vibe_db
DB_USERNAME=postgres
DB_PASSWORD=root

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# MinIO / S3
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_NAME=vibe-storage
MINIO_REGION=us-east-1

# JWT & Security
JWT_SECRET_KEY=your-256-bit-hex-secret-key-here
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Server
SERVER_PORT=8080
```

### 4. Run the Application

Start the Spring Boot application using Maven:

```bash
mvn clean spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/vibe-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`.

## API Documentation

The backend provides comprehensive REST API endpoints built with Spring Web MVC and secured with OAuth2 JWT authentication.

### Interactive API Explorer

Once the application is running, access the Swagger UI documentation at:

```
http://localhost:8080/swagger-ui.html
```

## Support

If you like this project, feel free to:

- ⭐ this repository. And we will be happy together :)

Thanks for supporting me!