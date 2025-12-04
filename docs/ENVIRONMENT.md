# 🔐 Environment Variables

> Complete reference of all configurable environment variables for the Restaurant Application

---

## Quick Start

```bash
# Copy the example file
cp .env.example .env

# Edit with your values
nano .env  # or code .env

# Start the application
./start.sh --all
```

---

## Database Configuration

### MySQL

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | _(required)_ | Root password for MySQL |
| `MYSQL_USER` | `restaurant` | Application database user |
| `MYSQL_PASSWORD` | _(required)_ | Password for application user |
| `MYSQL_HOST` | `localhost` | MySQL host address |
| `MYSQL_PORT` | `3306` | MySQL port |
| `PRODUCT_DB_NAME` | `product_db` | Product service database name |
| `ORDER_DB_NAME` | `order_db` | Order service database name |
| `AUTH_DB_NAME` | `auth_db` | Auth service database name |

### Redis

| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_HOST` | `localhost` | Redis host address |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | _(empty)_ | Redis password (optional) |

---

## Kafka Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `KAFKA_BROKER_ID` | `1` | Kafka broker ID |
| `ZOOKEEPER_HOST` | `zookeeper` | Zookeeper host |
| `ZOOKEEPER_PORT` | `2181` | Zookeeper port |

---

## Service Ports

| Variable | Default | Service |
|----------|---------|---------|
| `EUREKA_SERVER_PORT` | `8761` | Service Discovery |
| `API_GATEWAY_PORT` | `8080` | API Gateway |
| `PRODUCT_SERVICE_PORT` | `8081` | Product & Category Service |
| `CART_SERVICE_PORT` | `8082` | Cart Service |
| `ORDER_SERVICE_PORT` | `8083` | Order Service |
| `PAYMENT_SERVICE_PORT` | `8084` | Payment Service (Mockup) |
| `AUTH_SERVICE_PORT` | `8085` | Authentication Service |

---

## Authentication (JWT)

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | _(required)_ | Secret key for JWT signing (min 256 bits) |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiration in milliseconds (24h default) |

### Generate a Secure JWT Secret

```bash
# Using openssl
openssl rand -base64 64

# Using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

---

## Eureka Service Discovery

| Variable | Default | Description |
|----------|---------|-------------|
| `EUREKA_HOST` | `localhost` | Eureka server host |
| `EUREKA_PORT` | `8761` | Eureka server port |

---

## Logging & Debug

| Variable | Default | Description |
|----------|---------|-------------|
| `LOG_LEVEL` | `INFO` | Root logging level |
| `SHOW_SQL` | `false` | Show SQL queries in logs |
| `H2_CONSOLE_ENABLED` | `true` | Enable H2 console (Payment service) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |

---

## Payment Service (Mockup)

| Variable | Default | Description |
|----------|---------|-------------|
| `PAYMENT_TEST_DECLINE_CARD` | `4000000000000002` | Card number that triggers decline |

---

## Environment Profiles

### Development (`.env`)

```properties
MYSQL_ROOT_PASSWORD=DevRootPass2025!
MYSQL_PASSWORD=RestaurantDev2025!
JWT_SECRET=xK8pN2mQ5vR9tY3wZ6aD0gH4jL7oS1uF8bE2cI5nM8pQ3rT6wY9zA2dG5hJ8kN1qV4xB7yC0
SHOW_SQL=true
LOG_LEVEL=DEBUG
```

### Production (`.env.production`)

```properties
MYSQL_ROOT_PASSWORD=<secure-generated-password>
MYSQL_PASSWORD=<secure-generated-password>
JWT_SECRET=<secure-256-bit-key>
SHOW_SQL=false
LOG_LEVEL=WARN
H2_CONSOLE_ENABLED=false
SPRING_PROFILES_ACTIVE=prod
```

---

## Docker Compose

The `docker-compose.yml` automatically reads from `.env`:

```yaml
# From docker-compose.yml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
  MYSQL_USER: ${MYSQL_USER}
  MYSQL_PASSWORD: ${MYSQL_PASSWORD}
```

---

## Security Notes

⚠️ **IMPORTANT:**

1. **Never commit `.env` to version control** - it's already in `.gitignore`
2. **Use strong passwords** in production (min 16 characters, mixed case, numbers, symbols)
3. **Rotate JWT secrets** periodically
4. **Use secrets management** in cloud deployments (AWS Secrets Manager, HashiCorp Vault, etc.)
5. **Different credentials** for each environment (dev, staging, prod)

---

_Last Updated: 05.12.2025_
