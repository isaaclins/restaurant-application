# 🏗️ Architektur-Dokumentation

> Technische Details der Microservices-Architektur

---

## 📐 System-Architektur

```
                                    ┌─────────────────────────────────────────────────────────────┐
                                    │                        DOCKER NETWORK                        │
                                    │                                                              │
┌──────────────┐                    │  ┌─────────────────────────────────────────────────────┐    │
│   Website    │  HTTP              │  │                    API GATEWAY                       │    │
│  (React)     │ ──────────────────▶│  │              (Spring Cloud Gateway)                  │    │
└──────────────┘                    │  │                    Port: 8080                        │    │
                                    │  └─────────────────────────────────────────────────────┘    │
┌──────────────┐                    │                           │                                 │
│  Restaurant  │  HTTP              │                           │ Load Balancing                  │
│   Client     │ ──────────────────▶│                           ▼                                 │
│   (Tauri)    │                    │  ┌─────────────────────────────────────────────────────┐    │
└──────────────┘                    │  │              EUREKA SERVICE DISCOVERY               │    │
                                    │  │                    Port: 8761                        │    │
                                    │  └─────────────────────────────────────────────────────┘    │
                                    │                           │                                 │
                                    │     ┌─────────────────────┼─────────────────────┐          │
                                    │     │           │         │         │           │          │
                                    │     ▼           ▼         ▼         ▼           ▼          │
                                    │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │
                                    │  │ Auth   │ │Product │ │ Cart   │ │ Order  │ │Payment │   │
                                    │  │Service │ │Service │ │Service │ │Service │ │Service │   │
                                    │  │ :8085  │ │ :8081  │ │ :8082  │ │ :8083  │ │ :8084  │   │
                                    │  └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘   │
                                    │      │         │         │         │         │            │
                                    │      ▼         ▼         ▼         ▼         ▼            │
                                    │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │
                                    │  │Postgres│ │Postgres│ │ Redis  │ │Postgres│ │  H2    │   │
                                    │  │ :5434  │ │ :5432  │ │ :6379  │ │ :5433  │ │In-Mem  │   │
                                    │  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘   │
                                    │                                                            │
                                    │  ┌─────────────────────────────────────────────────────┐   │
                                    │  │                   APACHE KAFKA                       │   │
                                    │  │            (Event-Driven Communication)              │   │
                                    │  │                     :9092                            │   │
                                    │  └─────────────────────────────────────────────────────┘   │
                                    │                                                            │
                                    └────────────────────────────────────────────────────────────┘
```

---

## 🔧 Microservices

### Übersicht

| Service             | Port | Datenbank   | Beschreibung                            |
| ------------------- | ---- | ----------- | --------------------------------------- |
| **API Gateway**     | 8080 | -           | Routing, Rate Limiting, Auth-Validation |
| **Eureka Server**   | 8761 | -           | Service Discovery & Registry            |
| **Auth Service**    | 8085 | PostgreSQL  | Authentifizierung & JWT                 |
| **Product Service** | 8081 | PostgreSQL  | Produktkatalog CRUD                     |
| **Cart Service**    | 8082 | Redis       | Warenkorb-Verwaltung                    |
| **Order Service**   | 8083 | PostgreSQL  | Bestellungen & Status                   |
| **Payment Service** | 8084 | H2 (Mockup) | Zahlungs-Simulation                     |

---

## 🔐 Authentifizierung

### Auth-Flow für Restaurant Client

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  Restaurant │      │     API     │      │    Auth     │
│   Client    │      │   Gateway   │      │   Service   │
└──────┬──────┘      └──────┬──────┘      └──────┬──────┘
       │                    │                    │
       │  POST /auth/login  │                    │
       │  {username, pass}  │                    │
       │───────────────────▶│                    │
       │                    │  POST /auth/login  │
       │                    │───────────────────▶│
       │                    │                    │
       │                    │   Validate creds   │
       │                    │   Generate JWT     │
       │                    │                    │
       │                    │   {token, refresh} │
       │                    │◀───────────────────│
       │   {token, refresh} │                    │
       │◀───────────────────│                    │
       │                    │                    │
       │  GET /api/orders   │                    │
       │  Header: Bearer JWT│                    │
       │───────────────────▶│                    │
       │                    │                    │
       │                    │  Validate JWT      │
       │                    │  (Gateway Filter)  │
       │                    │                    │
       │                    │  Route to Order    │
       │                    │  Service           │
       │                    │                    │
```

### JWT Token Struktur

```json
{
  "sub": "restaurant-user-id",
  "role": "RESTAURANT_ADMIN",
  "restaurant_id": "rest-123",
  "iat": 1701734400,
  "exp": 1701820800
}
```

### Rollen

| Rolle              | Zugriff                                      |
| ------------------ | -------------------------------------------- |
| `CUSTOMER`         | Website: Produkte, Cart, eigene Orders       |
| `RESTAURANT_ADMIN` | Client: Alle Orders, Products CRUD, Settings |
| `RESTAURANT_STAFF` | Client: Orders lesen & Status ändern         |

---

## 🔄 Kafka Events

### Event-Typen

| Topic                  | Publisher       | Subscriber      | Beschreibung                  |
| ---------------------- | --------------- | --------------- | ----------------------------- |
| `order.created`        | Order Service   | Payment Service | Neue Bestellung               |
| `order.paid`           | Payment Service | Order Service   | Zahlung erfolgt               |
| `order.status-changed` | Order Service   | - (WebSocket)   | Status-Update                 |
| `product.updated`      | Product Service | Cart Service    | Preis-/Verfügbarkeitsänderung |

### Event-Flow: Bestellung

```
Customer               Order            Kafka           Payment          Restaurant
    │                    │                │                │                │
    │  POST /orders      │                │                │                │
    │───────────────────▶│                │                │                │
    │                    │  order.created │                │                │
    │                    │───────────────▶│                │                │
    │                    │                │ order.created  │                │
    │                    │                │───────────────▶│                │
    │                    │                │                │                │
    │  POST /payments    │                │                │                │
    │────────────────────────────────────────────────────▶│                │
    │                    │                │                │                │
    │                    │                │   order.paid   │                │
    │                    │                │◀───────────────│                │
    │                    │   order.paid   │                │                │
    │                    │◀───────────────│                │                │
    │                    │                │                │                │
    │                    │ order.status   │                │                │
    │                    │───────────────▶│                │                │
    │                    │                │                │  WebSocket     │
    │                    │                │                │───────────────▶│
    │                    │                │                │  New Order! 🔔 │
```

---

## 🛡️ Circuit Breaker (Resilience4j)

### Konfiguration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
```

### Fallback-Strategien

| Service         | Fallback                           |
| --------------- | ---------------------------------- |
| Product Service | Cached Produktliste                |
| Cart Service    | Lokaler Storage im Client          |
| Payment Service | "Zahlung bei Abholung" als Default |

---

## 💾 Datenbank-Schema

### Product Service (PostgreSQL)

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE product_sizes (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    size_name VARCHAR(10) NOT NULL,
    price_modifier DECIMAL(10,2) NOT NULL
);

CREATE TABLE product_extras (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL
);
```

### Order Service (PostgreSQL)

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    ticket_number VARCHAR(10) NOT NULL,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    order_type VARCHAR(20) NOT NULL, -- PICKUP, DELIVERY
    status VARCHAR(30) NOT NULL,     -- NEW, PREPARING, READY, DELIVERED, CANCELLED
    total_amount DECIMAL(10,2) NOT NULL,
    delivery_address TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    size VARCHAR(10),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    extras JSONB
);
```

### Auth Service (PostgreSQL)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    restaurant_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    token VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
```

### Cart Service (Redis)

```
Key: cart:{sessionId}
TTL: 24 hours
Value: {
    "items": [
        {
            "productId": 1,
            "name": "Margherita",
            "size": "M",
            "quantity": 2,
            "unitPrice": 18.50,
            "extras": ["Extra Cheese"]
        }
    ],
    "totalPrice": 41.00,
    "createdAt": "2025-12-04T12:00:00Z"
}
```

---

## 🔌 API Gateway Routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Public Routes (no auth)
        - id: products-public
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
            - Method=GET

        - id: cart
          uri: lb://cart-service
          predicates:
            - Path=/api/cart/**

        # Auth Routes
        - id: auth
          uri: lb://auth-service
          predicates:
            - Path=/auth/**

        # Protected Routes (require JWT)
        - id: orders-create
          uri: lb://order-service
          predicates:
            - Path=/api/orders
            - Method=POST

        - id: orders-restaurant
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - JwtAuthFilter
            - RoleFilter=RESTAURANT_ADMIN,RESTAURANT_STAFF

        - id: products-admin
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
            - Method=POST,PUT,DELETE
          filters:
            - JwtAuthFilter
            - RoleFilter=RESTAURANT_ADMIN
```

---

## 📊 Technologie-Stack

### Backend

| Technologie          | Version  | Verwendung                  |
| -------------------- | -------- | --------------------------- |
| Java                 | 21 LTS   | Programmiersprache          |
| Spring Boot          | 3.2.x    | Framework                   |
| Spring Cloud         | 2023.0.x | Microservices-Infrastruktur |
| Spring Cloud Gateway | -        | API Gateway                 |
| Netflix Eureka       | -        | Service Discovery           |
| Resilience4j         | 2.x      | Circuit Breaker             |
| Apache Kafka         | 3.x      | Message Broker              |
| Spring Security      | 6.x      | Authentifizierung           |
| jjwt                 | 0.12.x   | JWT Handling                |

### Datenbanken

| Datenbank  | Version | Service                |
| ---------- | ------- | ---------------------- |
| PostgreSQL | 15      | Products, Orders, Auth |
| Redis      | 7       | Cart                   |
| H2         | -       | Payment (Mockup)       |

### Frontend

| Technologie  | Version | Verwendung   |
| ------------ | ------- | ------------ |
| React        | 18.x    | UI Framework |
| TypeScript   | 5.x     | Type Safety  |
| Vite         | 5.x     | Build Tool   |
| Tailwind CSS | 3.x     | Styling      |
| Tauri        | 2.x     | Desktop App  |
| Axios        | 1.x     | HTTP Client  |

### DevOps

| Technologie     | Verwendung        |
| --------------- | ----------------- |
| Docker          | Containerisierung |
| Docker Compose  | Orchestrierung    |
| Swagger/OpenAPI | API Dokumentation |

---

## 🐳 Docker Compose

```yaml
version: "3.8"

services:
  # Databases
  postgres-products:
    image: postgres:15
    environment:
      POSTGRES_DB: products
      POSTGRES_USER: app
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    volumes:
      - postgres-products-data:/var/lib/postgresql/data

  postgres-orders:
    image: postgres:15
    environment:
      POSTGRES_DB: orders
      POSTGRES_USER: app
      POSTGRES_PASSWORD: secret
    ports:
      - "5433:5432"
    volumes:
      - postgres-orders-data:/var/lib/postgresql/data

  postgres-auth:
    image: postgres:15
    environment:
      POSTGRES_DB: auth
      POSTGRES_USER: app
      POSTGRES_PASSWORD: secret
    ports:
      - "5434:5432"
    volumes:
      - postgres-auth-data:/var/lib/postgresql/data

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  # Kafka
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"

  # Infrastructure Services
  eureka-server:
    build: ./backend/eureka-server
    ports:
      - "8761:8761"

  api-gateway:
    build: ./backend/api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/

  # Application Services
  auth-service:
    build: ./backend/auth-service
    ports:
      - "8085:8085"
    depends_on:
      - eureka-server
      - postgres-auth
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-auth:5432/auth

  product-service:
    build: ./backend/product-service
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - postgres-products
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-products:5432/products

  cart-service:
    build: ./backend/cart-service
    ports:
      - "8082:8082"
    depends_on:
      - eureka-server
      - redis
    environment:
      SPRING_DATA_REDIS_HOST: redis

  order-service:
    build: ./backend/order-service
    ports:
      - "8083:8083"
    depends_on:
      - eureka-server
      - postgres-orders
      - kafka
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-orders:5432/orders
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092

  payment-service:
    build: ./backend/payment-service
    ports:
      - "8084:8084"
    depends_on:
      - eureka-server
      - kafka
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092

volumes:
  postgres-products-data:
  postgres-orders-data:
  postgres-auth-data:
```

---

_Letzte Aktualisierung: 04.12.2025_
