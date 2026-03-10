# MIFOS Core Banking Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.6-brightgreen?style=flat-square&logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-brightgreen?style=flat-square&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)
![Kubernetes](https://img.shields.io/badge/Kubernetes-EKS-326CE5?style=flat-square&logo=kubernetes)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

**A production-ready microservices core banking system built with Spring Boot, deployed on AWS EKS.**

[Features](#features) · [Architecture](#system-architecture) · [Installation](#installation) · [Configuration](#configuration) · [Testing](#testing) · [CI/CD Pipeline](#cicd-pipeline) · [Deployment](#deployment)

</div>

---

## Description

**MIFOS** (Micro Finance Open Source) is an open-source core banking platform designed for microfinance institutions, digital banks, and fintechs. It provides a full suite of financial services including client onboarding, account management, transactions, general ledger accounting, and audit reporting — all built on a cloud-native microservices architecture.

---

## Table of Contents

1. [Features](#features)
2. [System Architecture](#system-architecture)
3. [Service Architecture](#service-architecture)
4. [API Gateway Routing](#api-gateway-routing)
5. [Inter-Service Communication](#inter-service-communication)
6. [CI/CD Pipeline](#cicd-pipeline)
7. [Deployment Architecture](#deployment-architecture)
8. [Installation](#installation)
9. [Configuration](#configuration)
10. [Testing](#testing)
11. [Usage](#usage)
12. [Contributing](#contributing)
13. [Contact](#contact)
14. [Acknowledgements](#acknowledgements)

---

## Features

| Feature | Description |
|---|---|
| **Client Onboarding** | Register and manage customer profiles |
| **Account Management** | Create and manage savings and current accounts |
| **Transactions** | Deposits, withdrawals, and transfers with full audit trail |
| **General Ledger** | Double-entry bookkeeping with reconciliation support |
| **Audit & Compliance** | Full audit trail of all system events |
| **Reporting & Analytics** | Daily, monthly, and custom financial reports |
| **JWT Security** | Token-based authentication and role-based access control |
| **API Gateway** | Centralised routing via Spring Cloud Gateway |
| **Service Discovery** | Eureka-based dynamic service registration |

---

## System Architecture

The diagram below shows the high-level architecture of the platform. All external traffic enters through the **API Gateway**, which routes requests to the appropriate downstream service. The two core services communicate internally via **Feign clients**, and all services register with **Eureka** for service discovery.

```
                        ┌─────────────────────────────────────────────┐
                        │              EXTERNAL CLIENTS                │
                        │        (Web App, Mobile App, API)           │
                        └───────────────────┬─────────────────────────┘
                                            │ HTTPS
                                            ▼
                        ┌─────────────────────────────────────────────┐
                        │              AWS APPLICATION                 │
                        │             LOAD BALANCER (ALB)             │
                        └───────────────────┬─────────────────────────┘
                                            │
                                            ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                         KUBERNETES CLUSTER (EKS)                          │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                    banking-platform namespace                        │  │
│  │                                                                     │  │
│  │              ┌──────────────────────────┐                           │  │
│  │              │       API GATEWAY        │                           │  │
│  │              │      (api-gate)          │                           │  │
│  │              │       Port: 9080         │                           │  │
│  │              └────────────┬─────────────┘                          │  │
│  │                           │                                         │  │
│  │           ┌───────────────┴───────────────┐                        │  │
│  │           │                               │                        │  │
│  │           ▼                               ▼                        │  │
│  │  ┌─────────────────────┐     ┌─────────────────────┐              │  │
│  │  │  CORE BANKING       │     │   AUDIT SERVICE     │              │  │
│  │  │  SERVICE            │◄────│                     │              │  │
│  │  │  (corebanking)      │     │  (audit-service)    │              │  │
│  │  │  Port: 8080         │     │  Port: 8081         │              │  │
│  │  └──────────┬──────────┘     └──────────┬──────────┘              │  │
│  │             │                           │                          │  │
│  │             └─────────────┬─────────────┘                         │  │
│  │                           │                                        │  │
│  │                           ▼                                        │  │
│  │              ┌──────────────────────────┐                         │  │
│  │              │     EUREKA SERVER        │                         │  │
│  │              │   (Service Discovery)    │                         │  │
│  │              │       Port: 8761         │                         │  │
│  │              └──────────────────────────┘                         │  │
│  │                                                                     │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                                                            │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                        AWS RDS (PostgreSQL)                          │  │
│  │           corebanking_db                audit_db                     │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## Service Architecture

Each microservice is internally organised into sub-services (packages) with dedicated controllers, services, and repositories.

```
banking-platform/
│
├── corebanking/                      ← Core Banking Service (Port 8080)
│   └── src/main/java/
│       ├── auth/                     Sub-service: JWT auth, login, user management
│       │   ├── AuthController
│       │   ├── JwtAuthenticationFilter
│       │   └── SecurityConfig
│       ├── account/                  Sub-service: Account creation & management
│       │   ├── AccountController     GET /api/accounts/**
│       │   ├── AccountService
│       │   └── AccountRepository
│       ├── customer/                 Sub-service: Customer onboarding
│       │   ├── CustomerController    GET /api/customers/**
│       │   └── CustomerService
│       ├── transaction/              Sub-service: Deposits, withdrawals, transfers
│       │   ├── TransactionController GET /api/transactions/**
│       │   └── TransactionService
│       └── ledger/                   Sub-service: Double-entry ledger
│           ├── LedgerController      GET /api/ledger/**
│           └── LedgerService
│
├── audit-service/                    ← Audit & Reporting Service (Port 8081)
│   └── src/main/java/
│       ├── audit/                    Sub-service: Audit trail logging
│       │   ├── AuditController       GET /api/audit/**
│       │   └── AuditTrailRepository
│       └── reporting/                Sub-service: Financial reports
│           ├── ReportingController   GET /api/reports/**
│           └── ReportingService
│
└── api-gate/                         ← API Gateway (Port 9080)
    └── Spring Cloud Gateway
        └── Routes all /api/** traffic
```

---

## API Gateway Routing

The API Gateway (`api-gate`) is the single entry point for all client traffic. It performs **routing only** — no JWT validation, no business logic. Security is enforced downstream in the `auth` sub-service of `corebanking`.

```
                    ┌─────────────────────────────────────────────┐
                    │            API GATEWAY (Port 9080)           │
                    │                                             │
   /api/auth/**  ──►│─── lb://COREBANKING-SERVICE ───────────────►│── Port 8080
   /api/accounts/**─│─── lb://COREBANKING-SERVICE ───────────────►│── Port 8080
   /api/customers/**│─── lb://COREBANKING-SERVICE ───────────────►│── Port 8080
   /api/transactions│─── lb://COREBANKING-SERVICE ───────────────►│── Port 8080
   /api/ledger/**  ─│─── lb://COREBANKING-SERVICE ───────────────►│── Port 8080
                    │                                             │
   /api/audit/**  ──│─── lb://AUDIT-SERVICE ─────────────────────►│── Port 8081
   /api/reports/** ─│─── lb://AUDIT-SERVICE ─────────────────────►│── Port 8081
                    │                                             │
   /actuator/health─│─── Gateway itself ──────────────────────────│── 200 UP
   /api/unknown/** ─│─── No match ────────────────────────────────│── 404
                    └─────────────────────────────────────────────┘
```

---

## Inter-Service Communication

The `audit-service` consumes data from `corebanking` using **Spring Cloud OpenFeign** clients. These are contract-tested independently with `MockWebServer`.

```
  ┌─────────────────────────────────────────────────────────────────┐
  │                        AUDIT SERVICE                            │
  │                                                                 │
  │   ReportingService                                              │
  │        │                                                        │
  │        ├──► AccountClient   ──► GET /api/accounts/**            │
  │        │         └── AccountDto { accountNumber,                │
  │        │                          customerId,                   │
  │        │                          balance,                      │
  │        │                          AccountType (enum) }          │
  │        │                                                        │
  │        ├──► TransactionClient ─► GET /api/transactions/**       │
  │        │         └── TransactionDto { referenceId,              │
  │        │                              accountNumber,            │
  │        │                              amount, type,             │
  │        │                              createdAt }               │
  │        │                                                        │
  │        └──► LedgerClient    ──► GET /api/ledger/**              │
  │                  └── LedgerEntryDto { accountNumber,            │
  │                                       transactionId,            │
  │                                       amount,                   │
  │                                       EntryType (DEBIT/CREDIT), │
  │                                       timestamp }               │
  └────────────────────────┬────────────────────────────────────────┘
                           │  HTTP via Feign
                           ▼
  ┌─────────────────────────────────────────────────────────────────┐
  │                      CORE BANKING SERVICE                       │
  │   AccountController   TransactionController   LedgerController  │
  └─────────────────────────────────────────────────────────────────┘
```

---

## CI/CD Pipeline

The project uses **GitHub Actions** with a two-pipeline strategy: a CI pipeline for the `dev` branch and a CD pipeline for the `main` branch, both targeting **AWS ECR** and **AWS EKS**.

```
  DEV BRANCH (ci.yml)
  ─────────────────────────────────────────────────────────────────
  push to dev
       │
       ▼
  ┌────────────────────────────────────────────────────────────┐
  │  Run Tests (parallel)                                      │
  │  ┌─────────────────┐ ┌──────────────────┐ ┌────────────┐  │
  │  │ test-corebanking│ │ test-audit-service│ │test-api-gate│ │
  │  └────────┬────────┘ └────────┬─────────┘ └─────┬──────┘  │
  └───────────┼──────────────────┼─────────────────┼──────────┘
              └──────────────────┼─────────────────┘
                                 │ all pass
                                 ▼
                    ┌─────────────────────────┐
                    │  Build & Push to ECR    │
                    │  (matrix: 3 services)   │
                    │  Tag: <sha7>            │
                    │  Tag: dev-latest        │
                    └─────────────────────────┘


  MAIN BRANCH (cd.yml)
  ─────────────────────────────────────────────────────────────────
  push to main
       │
       ▼
  ┌────────────────────────────────────────────────────────────┐
  │  Run Tests (parallel — same as CI)                         │
  └───────────────────────────┬────────────────────────────────┘
                              │ all pass
                              ▼
                 ┌────────────────────────┐
                 │  Build & Push to ECR   │
                 │  Tag: <sha7>           │
                 │  Tag: latest           │
                 └────────────┬───────────┘
                              │
                              ▼
                 ┌────────────────────────┐
                 │  Deploy to EKS         │
                 │  1. Apply ConfigMaps   │
                 │  2. Deploy corebanking │
                 │  3. Deploy audit-svc   │
                 │  4. Deploy api-gate    │
                 │  5. Wait for rollouts  │
                 └────────────┬───────────┘
                              │
                    ┌─────────┴──────────┐
                    │                    │
                 success              failure
                    │                    │
                    ▼                    ▼
              Pods running         Auto rollback
                                  (kubectl rollout undo)
```

**Required GitHub Secrets:**

| Secret | Description |
|---|---|
| `AWS_ACCOUNT_ID` | Your 12-digit AWS account ID |
| `AWS_ACCESS_KEY_ID` | IAM access key with ECR + EKS permissions |
| `AWS_SECRET_ACCESS_KEY` | IAM secret key |

---

## Deployment Architecture

```
  ┌──────────────────────────────────────────────────────────────────────┐
  │                          AWS CLOUD                                    │
  │                                                                       │
  │   ┌─────────────┐    ┌──────────────────────────────────────────┐   │
  │   │  Amazon ECR  │    │           Amazon EKS Cluster             │   │
  │   │             │    │                                          │   │
  │   │ banking-    │    │  Namespace: banking-platform             │   │
  │   │ platform/   │    │                                          │   │
  │   │ corebanking │    │  ┌─────────────┐  ┌─────────────┐       │   │
  │   │ audit-svc   │───►│  │ corebanking │  │ audit-svc   │       │   │
  │   │ api-gate    │    │  │ replicas: 2 │  │ replicas: 2 │       │   │
  │   └─────────────┘    │  └─────────────┘  └─────────────┘       │   │
  │                       │  ┌─────────────┐  ┌─────────────┐       │   │
  │   ┌─────────────┐    │  │  api-gate   │  │   eureka    │       │   │
  │   │ GitHub      │    │  │ replicas: 2 │  │ replicas: 1 │       │   │
  │   │ Actions     │───►│  └─────────────┘  └─────────────┘       │   │
  │   │ CI/CD       │    │                                          │   │
  │   └─────────────┘    │  ┌──────────────────────────────────┐   │   │
  │                       │  │    ConfigMap + Secrets            │   │   │
  │                       │  │    (DB creds, JWT secret)        │   │   │
  │                       │  └──────────────────────────────────┘   │   │
  │                       └──────────────────────────────────────────┘   │
  │                                                                       │
  │   ┌─────────────────────────────────────────────────────────────┐   │
  │   │                  Amazon RDS (PostgreSQL)                      │   │
  │   │           corebanking_db          audit_db                   │   │
  │   └─────────────────────────────────────────────────────────────┘   │
  └──────────────────────────────────────────────────────────────────────┘
```

---

## Installation

### Prerequisites

| Tool | Version | Download |
|---|---|---|
| JDK | 17+ | [Adoptium Temurin](https://adoptium.net) |
| Apache Maven | 3.8+ | [maven.apache.org](https://maven.apache.org) |
| Git | Any | [git-scm.com](https://git-scm.com) |
| PostgreSQL | 14+ | [postgresql.org](https://www.postgresql.org) |
| Docker | 24+ | [docker.com](https://www.docker.com) _(optional)_ |
| IDE | Any | [IntelliJ IDEA](https://www.jetbrains.com) recommended |

### Clone the Repository

```bash
git clone https://github.com/Stephenekeh-dev/banking-platform.git
cd banking-platform
```

This is a **mono-repository** — both services live in the same repo but are separate Spring Boot projects. Open them in separate IDE windows.

### Install Dependencies

Run for each service:

```bash
# Core Banking Service
cd corebanking
./mvnw clean install

# Audit Service
cd ../audit-service
./mvnw clean install

# API Gateway
cd ../api-gate
./mvnw clean install
```

---

## Configuration

### Core Banking Service (`corebanking/src/main/resources/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/corebanking_db
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Audit Service (`audit-service/src/main/resources/application.yml`)

```yaml
spring:
  application:
    name: audit-service
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/corebanking_db
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

corebanking:
  service:
    url: http://localhost:8080

server:
  port: 8081

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### API Gateway (`api-gate/src/main/resources/application.yml`)

```yaml
spring:
  application:
    name: api_gate
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: corebanking-auth
          uri: lb://COREBANKING-SERVICE
          predicates: [Path=/api/auth/**]
        - id: corebanking-account
          uri: lb://COREBANKING-SERVICE
          predicates: [Path=/api/accounts/**]
        - id: corebanking-transaction
          uri: lb://COREBANKING-SERVICE
          predicates: [Path=/api/transactions/**]
        - id: corebanking-ledger
          uri: lb://COREBANKING-SERVICE
          predicates: [Path=/api/ledger/**]
        - id: corebanking-customer
          uri: lb://COREBANKING-SERVICE
          predicates: [Path=/api/customers/**]
        - id: audit-service
          uri: lb://AUDIT-SERVICE
          predicates: [Path=/api/audit/**]
        - id: reporting-service
          uri: lb://AUDIT-SERVICE
          predicates: [Path=/api/reports/**]

server:
  port: 9080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## Testing

All services have comprehensive automated test suites. Tests are run automatically on every push and pull request via the CI pipeline.

### Test Summary

| Service | Test Class | Tests | Description |
|---|---|---|---|
| `audit-service` | `AccountClientContractTest` | 3 | Feign client contract tests for Account API |
| `audit-service` | `LedgerClientContractTest` | 3 | Feign client contract tests for Ledger API |
| `audit-service` | `TransactionClientContractTest` | 3 | Feign client contract tests for Transaction API |
| `audit-service` | `ReportingServiceTest` | 4 | Unit tests for ReportingService logic |
| `api-gate` | `ApiGatewayRoutingTest` | 9 | Integration tests for all gateway routes |
| **Total** | | **22** | **All passing ✅** |

### Running Tests

```bash
# Run all tests for a specific service
cd corebanking && ./mvnw test
cd audit-service && ./mvnw test
cd api-gate && ./mvnw test

# Run a specific test class
./mvnw test -Dtest=ApiGatewayRoutingTest -e

# View test reports
cat target/surefire-reports/<TestClassName>.txt
```

### Test Architecture

The Feign client tests use `MockWebServer` to intercept HTTP calls without needing a running corebanking service:

```
  ┌─────────────────────────────────────────────────┐
  │              AUDIT SERVICE TEST                  │
  │                                                 │
  │  AccountClientContractTest                      │
  │  ┌─────────────────────────────────────────┐   │
  │  │  @SpringBootTest (WebEnvironment.NONE)  │   │
  │  │                                         │   │
  │  │  AccountClient (Feign)                  │   │
  │  │       │                                 │   │
  │  │       ▼                                 │   │
  │  │  MockWebServer  ← enqueue(MockResponse) │   │
  │  │       │                                 │   │
  │  │       ▼                                 │   │
  │  │  RecordedRequest → assert path, method  │   │
  │  │  Deserialized DTO → assert field values │   │
  │  └─────────────────────────────────────────┘   │
  └─────────────────────────────────────────────────┘
```

---

## Usage

### Running Locally (IDE or Command Line)

Start services in this order:

```bash
# 1. Start Eureka Server (if you have a standalone eureka module)
#    OR ensure it starts as part of corebanking

# 2. Start Core Banking Service
cd corebanking
./mvnw spring-boot:run
# Runs on http://localhost:8080

# 3. Start Audit Service
cd audit-service
./mvnw spring-boot:run
# Runs on http://localhost:8081

# 4. Start API Gateway
cd api-gate
./mvnw spring-boot:run
# Runs on http://localhost:9080
```

All external API calls should go through the gateway on port `9080`.

### Running via JAR

```bash
java -jar corebanking/target/corebanking-0.0.1-SNAPSHOT.jar
java -jar audit-service/target/audit-service-0.0.1-SNAPSHOT.jar
java -jar api-gate/target/api-gate-0.0.1-SNAPSHOT.jar
```

### Running with Docker

Each service has a multi-stage Dockerfile:

```bash
# Core Banking
cd corebanking
docker build -t banking-platform/corebanking .
docker run -p 8080:8080 banking-platform/corebanking

# Audit Service
cd audit-service
docker build -t banking-platform/audit-service .
docker run -p 8081:8081 banking-platform/audit-service

# API Gateway
cd api-gate
docker build -t banking-platform/api-gate .
docker run -p 9080:9080 banking-platform/api-gate
```

### API Endpoints (via Gateway)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate and receive JWT token |
| `POST` | `/api/accounts/create` | Create a new account |
| `GET` | `/api/accounts/all` | List all accounts |
| `GET` | `/api/accounts/{id}` | Get account by ID |
| `GET` | `/api/customers/all` | List all customers |
| `GET` | `/api/transactions/all` | List all transactions |
| `GET` | `/api/transactions/account/{accountNumber}` | Transactions by account |
| `GET` | `/api/ledger/all/{accountNumber}` | Ledger entries by account |
| `GET` | `/api/ledger/reconcile/{accountNumber}` | Reconcile account |
| `GET` | `/api/audit/all` | All audit trail entries |
| `GET` | `/api/reports/daily` | Daily financial report |
| `GET` | `/actuator/health` | Gateway health check |

---

## CI/CD Pipeline

The project includes a fully automated GitHub Actions CI/CD pipeline. For the complete setup guide see [`CICD_SETUP.md`](CICD_SETUP.md).

### Branch Strategy

| Branch | Trigger | Actions |
|---|---|---|
| `dev` | Push | Run all tests → Build Docker images → Push to ECR (`:dev-latest`) |
| `main` | Push | Run all tests → Build images → Push to ECR (`:latest`) → Deploy to EKS |
| Any PR | Open/Update | Run all tests only |

### Kubernetes Manifests

The `k8s/` directory contains all deployment manifests:

```
k8s/
├── infrastructure/
│   ├── configmap.yml         # Shared environment config
│   ├── secrets-template.yml  # Secret key reference (do not commit real values)
│   └── eureka.yml            # Eureka Server deployment
├── corebanking/
│   └── deployment.yml        # Deployment + ClusterIP Service
├── audit-service/
│   └── deployment.yml        # Deployment + ClusterIP Service
└── api-gate/
    └── deployment.yml        # Deployment + ClusterIP Service + ALB Ingress
```

---

## Project Structure

```
banking-platform/
├── .github/
│   └── workflows/
│       ├── ci.yml              # CI pipeline (dev branch)
│       └── cd.yml              # CD pipeline (main branch)
├── k8s/
│   ├── infrastructure/         # Eureka, ConfigMap, Secrets
│   ├── corebanking/            # K8s manifests for corebanking
│   ├── audit-service/          # K8s manifests for audit-service
│   └── api-gate/               # K8s manifests + Ingress for api-gate
├── corebanking/                # Core Banking Spring Boot project
│   ├── Dockerfile
│   └── src/
├── audit-service/              # Audit & Reporting Spring Boot project
│   ├── Dockerfile
│   └── src/
├── api-gate/                   # API Gateway Spring Boot project
│   ├── Dockerfile
│   └── src/
├── docs/
│   └── system-design.png
├── CICD_SETUP.md               # Full CI/CD setup guide
└── README.md
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.6 |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Inter-service Calls | Spring Cloud OpenFeign |
| Security | Spring Security + JWT (JJWT) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Testing | JUnit 5, Mockito, MockWebServer, WebTestClient |
| Containerisation | Docker (multi-stage Alpine builds) |
| Orchestration | Kubernetes (AWS EKS) |
| Container Registry | AWS ECR |
| CI/CD | GitHub Actions |
| Load Balancer | AWS ALB (via AWS Load Balancer Controller) |

---

## Contributing

Contributions are welcome and appreciated!

1. Fork the project
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Write tests for your changes
4. Commit your changes: `git commit -m 'Add some AmazingFeature'`
5. Push to the branch: `git push origin feature/AmazingFeature`
6. Open a Pull Request against `dev`

Please ensure all tests pass before opening a PR:
```bash
./mvnw test  # run in each service directory
```

---

## Contact

**Stephen Ekeh** — stevenadibee@yahoo.com

Project: [https://github.com/Stephenekeh-dev/banking-platform](https://github.com/Stephenekeh-dev/banking-platform)

---

## Acknowledgements

- [AB Microfinance](https://www.ab-microfinance.com)
- [Mifos Initiative](https://mifos.org)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [AWS EKS Documentation](https://docs.aws.amazon.com/eks/)
