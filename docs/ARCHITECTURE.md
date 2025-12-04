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
                                    │  │ MySQL  │ │ MySQL  │ │ Redis  │ │ MySQL  │ │  H2    │   │
                                    │  │ :3306  │ │ :3307  │ │ :6379  │ │ :3308  │ │In-Mem  │   │
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
| **Auth Service**    | 8085 | MySQL       | Authentifizierung & JWT                 |
| **Product Service** | 8081 | MySQL       | Produktkatalog CRUD                     |
| **Cart Service**    | 8082 | Redis       | Warenkorb-Verwaltung                    |
| **Order Service**   | 8083 | MySQL       | Bestellungen & Status                   |
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

### Product Service (MySQL)

```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE product_sizes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    size_name VARCHAR(10) NOT NULL,
    price_modifier DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE product_extras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

### Order Service (MySQL)

```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(10) NOT NULL,
    customer_id BIGINT,                 -- NULL for guest orders, references auth.customers(id)
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    order_type VARCHAR(20) NOT NULL,    -- PICKUP, DELIVERY
    status VARCHAR(30) NOT NULL,        -- NEW, PREPARING, READY, DELIVERED, CANCELLED
    total_amount DECIMAL(10,2) NOT NULL,
    delivery_address TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    size VARCHAR(10),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    extras JSON,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

### Auth Service (MySQL)

```sql
-- Restaurant Staff Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,          -- RESTAURANT_ADMIN, RESTAURANT_STAFF
    restaurant_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Website Customers
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE customer_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    label VARCHAR(50),                  -- "Zuhause", "Arbeit"
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,                     -- References users(id) OR customers(id)
    user_type VARCHAR(20) NOT NULL,     -- 'STAFF' or 'CUSTOMER'
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

| Datenbank | Version | Service                |
| --------- | ------- | ---------------------- |
| MySQL     | 8       | Products, Orders, Auth |
| Redis     | 7       | Cart                   |
| H2        | -       | Payment (Mockup)       |

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
  mysql-products:
    image: mysql:8
    environment:
      MYSQL_DATABASE: products
      MYSQL_USER: app
      MYSQL_PASSWORD: secret
      MYSQL_ROOT_PASSWORD: rootsecret
    ports:
      - "3306:3306"
    volumes:
      - mysql-products-data:/var/lib/mysql

  mysql-orders:
    image: mysql:8
    environment:
      MYSQL_DATABASE: orders
      MYSQL_USER: app
      MYSQL_PASSWORD: secret
      MYSQL_ROOT_PASSWORD: rootsecret
    ports:
      - "3307:3306"
    volumes:
      - mysql-orders-data:/var/lib/mysql

  mysql-auth:
    image: mysql:8
    environment:
      MYSQL_DATABASE: auth
      MYSQL_USER: app
      MYSQL_PASSWORD: secret
      MYSQL_ROOT_PASSWORD: rootsecret
    ports:
      - "3308:3306"
    volumes:
      - mysql-auth-data:/var/lib/mysql

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
      - mysql-auth
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-auth:3306/auth
      SPRING_DATASOURCE_USERNAME: app
      SPRING_DATASOURCE_PASSWORD: secret

  product-service:
    build: ./backend/product-service
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - mysql-products
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-products:3306/products
      SPRING_DATASOURCE_USERNAME: app
      SPRING_DATASOURCE_PASSWORD: secret

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
      - mysql-orders
      - kafka
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-orders:3306/orders
      SPRING_DATASOURCE_USERNAME: app
      SPRING_DATASOURCE_PASSWORD: secret
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
  mysql-products-data:
  mysql-orders-data:
  mysql-auth-data:
```

---

_Letzte Aktualisierung: 04.12.2025_
