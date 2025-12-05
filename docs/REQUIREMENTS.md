# 📋 Projektanforderungen & Meilensteine

> **Modul**: M321 (Microservices) + M324 (Pipelines)  
> **Zeitbedarf**: 14 Lektionen (7 Tage)  
> **Abgabe**: Tag 7 (Endabgabe)  
> **Form**: Partnerarbeit 2-3 Lernende

---

## 📅 Abgabe-Zeitplan

| Tag | Datum      | Abgabe                                |
| --- | ---------- | ------------------------------------- |
| 1   | 04.12.2025 | Projektdefinition, Repo-Einrichtung   |
| 2   | TBD        | Architekturskizze grafisch, Repo-Push |
| 4   | TBD        | Reflexion erste Abgabe                |
| 5   | TBD        | Update Reflexion                      |
| 6   | TBD        | Update Reflexion                      |
| 7   | TBD        | **Endabgabe**                         |

---

## 🎯 Bewertungskriterien (Anforderungen 1-10)

| Nr  | Anforderung                                        | Status | Priorität |
| --- | -------------------------------------------------- | ------ | --------- |
| 1   | Jeder Service eigene DB + Argumentation            | ✅     | MUSS      |
| 2   | Docker für Datenbanken                             | ✅     | MUSS      |
| 3   | REST API Kommunikation (Spring REST Controllers)   | ✅     | MUSS      |
| 4   | Sicherheitsaspekte (sensible Daten)                | ✅     | MUSS      |
| 5   | Fehlerbehandlung & Circuit Breaker                 | ✅     | MUSS      |
| 6   | Service-Dokumentation (jeder Service dokumentiert) | 🔄     | MUSS      |
| 7   | Grafische Architekturskizze mit Überlegungen       | ✅     | MUSS      |
| 8   | Clean Code Regeln                                  | ✅     | MUSS      |
| 9   | API-Dokumentation (Swagger)                        | ✅     | MUSS      |
| 10  | Formale Ausführung (Gruppe)                        | 🔄     | MUSS      |

### Pipeline-Anforderungen (M324)

| Nr  | Anforderung                                    | Status | Priorität |
| --- | ---------------------------------------------- | ------ | --------- |
| P1  | GitHub Actions Pipeline: Build → Test → Deploy | ✅     | MUSS      |
| P2  | Automatisches Testing bei jedem Push           | ✅     | MUSS      |
| P3  | Docker Image mit Versionierung                 | ✅     | MUSS      |
| P4  | Cloud Deployment (AWS/Azure/Fly.io)            | ⬜     | MUSS      |
| P5  | Unit Tests + Postman Tests automatisch         | ✅     | MUSS      |

---

## 🏗️ Pflicht-Komponenten

Die Anwendung **MUSS** folgende Komponenten beinhalten:

- ✅ **API Gateway** (Spring Cloud Gateway)
- ✅ **Eureka Service Discovery**
- ✅ **Kafka Messaging**
- ✅ **Circuit Breaker Pattern** (Resilience4j)
- ✅ **3-4 individuelle Microservices**

---

# 🚀 MEILENSTEINE

---

## Meilenstein 1: Infrastruktur & DevOps Setup

**Ziel**: Docker, Pipeline, und Basis-Infrastruktur aufsetzen

### 1.1 Docker Compose Infrastruktur

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                      | Status |
| ----- | --------------------------------- | ------ |
| 1.1.1 | `docker-compose.yml` erstellen    | ✅     |
| 1.1.2 | MySQL Container konfigurieren     | ✅     |
| 1.1.3 | Redis Container konfigurieren     | ✅     |
| 1.1.4 | Kafka + Zookeeper Container       | ✅     |
| 1.1.5 | Alle Container starten und testen | ✅     |

**Erfolgskriterium**: `docker-compose up` startet alle Datenbanken fehlerfrei

---

### 1.2 GitHub Actions Pipeline

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                         | Status |
| ----- | ------------------------------------ | ------ |
| 1.2.1 | `.github/workflows/ci.yml` erstellen | ✅     |
| 1.2.2 | Build Stage implementieren           | ✅     |
| 1.2.3 | Test Stage implementieren            | ✅     |
| 1.2.4 | Docker Image Build + Push            | ✅     |
| 1.2.5 | Deploy Stage (Fly.io/Railway)        | ⬜     |

**Pipeline-Stages**:

```yaml
stages:
  - build # Maven/Gradle Build
  - test # Unit Tests + Integration Tests
  - deploy # Docker Push + Cloud Deploy
```

**Erfolgskriterium**: Push auf `main` → Automatischer Build + Test + Deploy

---

## Meilenstein 2: Eureka Service Discovery

**Ziel**: Zentrales Service-Registry für alle Microservices

### 2.1 Eureka Server

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                                    | Status |
| ----- | ----------------------------------------------- | ------ |
| 2.1.1 | Spring Boot Projekt erstellen (`eureka-server`) | ✅     |
| 2.1.2 | `@EnableEurekaServer` Annotation                | ✅     |
| 2.1.3 | `application.yml` konfigurieren (Port 8761)     | ✅     |
| 2.1.4 | Dockerfile erstellen                            | ✅     |
| 2.1.5 | Health Check implementieren                     | ✅     |

**Konfiguration**:

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

**Test-Anforderungen**:
| Test | Beschreibung | Erwartetes Ergebnis |
|------|--------------|---------------------|
| T2.1.1 | Server startet | `http://localhost:8761` erreichbar |
| T2.1.2 | Dashboard zeigt registrierte Services | UI zeigt Service-Liste |

**Erfolgskriterium**: Eureka Dashboard unter `http://localhost:8761` erreichbar

---

## Meilenstein 3: API Gateway

**Ziel**: Zentraler Einstiegspunkt für alle API-Anfragen

### 3.1 Gateway Service

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                                  | Status |
| ----- | --------------------------------------------- | ------ |
| 3.1.1 | Spring Boot Projekt erstellen (`api-gateway`) | ✅     |
| 3.1.2 | Spring Cloud Gateway Dependency               | ✅     |
| 3.1.3 | Route-Konfiguration für alle Services         | ✅     |
| 3.1.4 | Eureka Client Integration                     | ✅     |
| 3.1.5 | CORS Konfiguration                            | ✅     |
| 3.1.6 | Rate Limiting (optional)                      | ⬜     |

**Route-Konfiguration**:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/products/**
        - id: cart-service
          uri: lb://CART-SERVICE
          predicates:
            - Path=/api/cart/**
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/auth/**
```

**Test-Anforderungen**:
| Test | Beschreibung | Erwartetes Ergebnis |
|------|--------------|---------------------|
| T3.1.1 | Gateway startet | Port 8080 erreichbar |
| T3.1.2 | Gateway registriert bei Eureka | Sichtbar im Dashboard |
| T3.1.3 | Routing zu Product Service | `/api/products` → Product Service |
| T3.1.4 | Routing zu Cart Service | `/api/cart` → Cart Service |
| T3.1.5 | CORS funktioniert | Frontend kann zugreifen |

**Erfolgskriterium**: Alle Requests über `http://localhost:8080` werden korrekt geroutet

---

## Meilenstein 4: Product Catalog Service

**Ziel**: Produktverwaltung mit CRUD-Operationen

### 4.1 Service Setup

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                    | Status |
| ----- | ------------------------------- | ------ |
| 4.1.1 | Spring Boot Projekt erstellen   | ✅     |
| 4.1.2 | MySQL Datenbank-Anbindung       | ✅     |
| 4.1.3 | JPA Entity `Product` erstellen  | ✅     |
| 4.1.4 | JPA Entity `Category` erstellen | ✅     |
| 4.1.5 | Repository Layer                | ✅     |
| 4.1.6 | Service Layer                   | ✅     |
| 4.1.7 | Eureka Client Integration       | ✅     |
| 4.1.8 | Swagger/OpenAPI Dokumentation   | ✅     |

### 4.2 REST Endpoints

**Status**: ✅ Abgeschlossen

#### `GET /api/products` - Alle Produkte abrufen

| Aspekt           | Details                         |
| ---------------- | ------------------------------- |
| **Method**       | GET                             |
| **Path**         | `/api/products`                 |
| **Query Params** | `?category=`, `?available=true` |
| **Response**     | `200 OK` + Array von Produkten  |
| **Auth**         | ❌ Keine                        |

```json
// Response 200
[
  {
    "id": 1,
    "name": "Margherita Pizza",
    "description": "Classic tomato and mozzarella",
    "price": 12.5,
    "category": "PIZZA",
    "available": true,
    "imageUrl": "/images/margherita.jpg"
  }
]
```

**Tests für GET /api/products**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T4.2.1 | Alle Produkte abrufen | - | 200 + Liste |
| T4.2.2 | Nach Kategorie filtern | `?category=PIZZA` | Nur Pizza-Produkte |
| T4.2.3 | Nur verfügbare Produkte | `?available=true` | Nur available=true |
| T4.2.4 | Leere Liste | Keine Produkte in DB | 200 + `[]` |

---

#### `GET /api/products/{id}` - Einzelnes Produkt

| Aspekt               | Details              |
| -------------------- | -------------------- |
| **Method**           | GET                  |
| **Path**             | `/api/products/{id}` |
| **Path Param**       | `id` (Long)          |
| **Response Success** | `200 OK` + Produkt   |
| **Response Error**   | `404 Not Found`      |

**Tests für GET /api/products/{id}**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T4.2.5 | Existierendes Produkt | `id=1` | 200 + Produkt |
| T4.2.6 | Nicht existierendes Produkt | `id=9999` | 404 Not Found |
| T4.2.7 | Ungültige ID | `id=abc` | 400 Bad Request |

---

#### `POST /api/products` - Produkt erstellen

| Aspekt               | Details                               |
| -------------------- | ------------------------------------- |
| **Method**           | POST                                  |
| **Path**             | `/api/products`                       |
| **Auth**             | 🔒 `RESTAURANT_ADMIN`                 |
| **Body**             | JSON Product                          |
| **Response Success** | `201 Created` + Produkt               |
| **Response Error**   | `400 Bad Request`, `401 Unauthorized` |

```json
// Request Body
{
  "name": "Pepperoni Pizza",
  "description": "Spicy pepperoni with cheese",
  "price": 14.5,
  "category": "PIZZA",
  "available": true
}
```

**Tests für POST /api/products**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T4.2.8 | Produkt erstellen (Admin) | Valid Body + Admin Token | 201 Created |
| T4.2.9 | Ohne Auth | Valid Body, kein Token | 401 Unauthorized |
| T4.2.10 | Ungültiger Body | `name: null` | 400 Bad Request |
| T4.2.11 | Negative Preis | `price: -5` | 400 Bad Request |
| T4.2.12 | Duplikat Name | Existierender Name | 409 Conflict |

---

#### `PUT /api/products/{id}` - Produkt aktualisieren

| Aspekt               | Details                             |
| -------------------- | ----------------------------------- |
| **Method**           | PUT                                 |
| **Path**             | `/api/products/{id}`                |
| **Auth**             | 🔒 `RESTAURANT_ADMIN`               |
| **Response Success** | `200 OK` + aktualisiertes Produkt   |
| **Response Error**   | `404 Not Found`, `401 Unauthorized` |

**Tests für PUT /api/products/{id}**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T4.2.13 | Produkt aktualisieren | Valid Body + Admin | 200 OK |
| T4.2.14 | Nicht existierendes Produkt | `id=9999` | 404 Not Found |
| T4.2.15 | Partial Update (nur Preis) | `{price: 15.00}` | 200 OK |

---

#### `DELETE /api/products/{id}` - Produkt löschen

| Aspekt               | Details               |
| -------------------- | --------------------- |
| **Method**           | DELETE                |
| **Path**             | `/api/products/{id}`  |
| **Auth**             | 🔒 `RESTAURANT_ADMIN` |
| **Response Success** | `204 No Content`      |
| **Response Error**   | `404 Not Found`       |

**Tests für DELETE /api/products/{id}**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T4.2.16 | Produkt löschen | Valid ID + Admin | 204 No Content |
| T4.2.17 | Nicht existierend | `id=9999` | 404 Not Found |
| T4.2.18 | Ohne Auth | Valid ID | 401 Unauthorized |

---

#### `PUT /api/products/{id}/availability` - Verfügbarkeit ändern

| Aspekt       | Details                                       |
| ------------ | --------------------------------------------- |
| **Method**   | PUT                                           |
| **Path**     | `/api/products/{id}/availability`             |
| **Auth**     | 🔒 `RESTAURANT_ADMIN` oder `RESTAURANT_STAFF` |
| **Body**     | `{ "available": false }`                      |
| **Response** | `200 OK`                                      |

**Tests für PUT /api/products/{id}/availability**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T4.2.19 | Verfügbarkeit setzen | `available: false` | 200 OK |
| T4.2.20 | Staff kann ändern | Staff Token | 200 OK |

---

## Meilenstein 5: Cart Service (Warenkorb)

**Ziel**: Session-basierter Warenkorb mit Redis

### 5.1 Service Setup

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                        | Status |
| ----- | ----------------------------------- | ------ |
| 5.1.1 | Spring Boot Projekt erstellen       | ✅     |
| 5.1.2 | Redis Anbindung (Spring Data Redis) | ✅     |
| 5.1.3 | Cart Model erstellen                | ✅     |
| 5.1.4 | CartItem Model erstellen            | ✅     |
| 5.1.5 | Repository Layer                    | ✅     |
| 5.1.6 | Service Layer                       | ✅     |
| 5.1.7 | Eureka Client Integration           | ✅     |
| 5.1.8 | Swagger Dokumentation               | ✅     |

**Datenbank-Argumentation** (Anforderung 1):

> **Redis** statt MySQL, weil:
>
> - Warenkorb ist temporär (Session-basiert)
> - Schnelle Read/Write-Operationen erforderlich
> - Automatische TTL (Time-To-Live) für Session-Cleanup
> - Key-Value Struktur ideal für Cart-by-SessionID

### 5.2 REST Endpoints

**Status**: ✅ Abgeschlossen

#### `GET /api/cart` - Warenkorb abrufen

| Aspekt       | Details                |
| ------------ | ---------------------- |
| **Method**   | GET                    |
| **Path**     | `/api/cart`            |
| **Header**   | `X-Session-ID: <uuid>` |
| **Response** | `200 OK` + Cart Object |

```json
// Response 200
{
  "sessionId": "abc-123",
  "items": [
    {
      "productId": 1,
      "productName": "Margherita Pizza",
      "quantity": 2,
      "unitPrice": 12.5,
      "totalPrice": 25.0
    }
  ],
  "totalItems": 2,
  "totalPrice": 25.0
}
```

**Tests für GET /api/cart**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T5.2.1 | Leerer Warenkorb | Neue Session | 200 + leere items |
| T5.2.2 | Warenkorb mit Items | Bestehende Session | 200 + items |
| T5.2.3 | Ohne Session-ID | Kein Header | 400 Bad Request |

---

#### `POST /api/cart/items` - Produkt hinzufügen

| Aspekt       | Details                        |
| ------------ | ------------------------------ |
| **Method**   | POST                           |
| **Path**     | `/api/cart/items`              |
| **Header**   | `X-Session-ID: <uuid>`         |
| **Body**     | `{ productId, quantity }`      |
| **Response** | `200 OK` + aktualisierter Cart |

```json
// Request
{
  "productId": 1,
  "quantity": 2
}
```

**Tests für POST /api/cart/items**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T5.2.4 | Produkt hinzufügen | Valid productId | 200 + Cart |
| T5.2.5 | Quantity erhöhen | Produkt bereits im Cart | Quantity addiert |
| T5.2.6 | Ungültiges Produkt | `productId: 9999` | 404 Not Found |
| T5.2.7 | Quantity 0 | `quantity: 0` | 400 Bad Request |
| T5.2.8 | Negative Quantity | `quantity: -1` | 400 Bad Request |

---

#### `DELETE /api/cart/items/{productId}` - Produkt entfernen

| Aspekt       | Details                        |
| ------------ | ------------------------------ |
| **Method**   | DELETE                         |
| **Path**     | `/api/cart/items/{productId}`  |
| **Header**   | `X-Session-ID: <uuid>`         |
| **Response** | `200 OK` + aktualisierter Cart |

**Tests für DELETE /api/cart/items/{productId}**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T5.2.9 | Produkt entfernen | Existierendes Produkt | 200 + Cart |
| T5.2.10 | Nicht im Cart | Produkt nicht im Cart | 404 Not Found |

---

## Meilenstein 6: Order Service (Bestellungen)

**Ziel**: Bestellverwaltung mit Status-Tracking

### 6.1 Service Setup

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                              | Status |
| ----- | ----------------------------------------- | ------ |
| 6.1.1 | Spring Boot Projekt erstellen             | ✅     |
| 6.1.2 | MySQL Datenbank-Anbindung                 | ✅     |
| 6.1.3 | JPA Entity `Order` erstellen              | ✅     |
| 6.1.4 | JPA Entity `OrderItem` erstellen          | ✅     |
| 6.1.5 | Kafka Producer Integration                | ✅     |
| 6.1.6 | Circuit Breaker für Product-Service Calls | ✅     |
| 6.1.7 | Swagger Dokumentation                     | ✅     |

**Kafka Events**:

```
order.created   → Neue Bestellung (für KDS)
order.updated   → Status-Änderung
order.completed → Bestellung abgeschlossen
```

### 6.2 REST Endpoints

**Status**: ✅ Abgeschlossen

#### `POST /api/orders` - Bestellung erstellen

| Aspekt       | Details                                       |
| ------------ | --------------------------------------------- |
| **Method**   | POST                                          |
| **Path**     | `/api/orders`                                 |
| **Header**   | `X-Session-ID: <uuid>` (optional: Auth Token) |
| **Body**     | Order Details                                 |
| **Response** | `201 Created` + Order                         |

```json
// Request
{
  "customerName": "Max Mustermann",
  "customerEmail": "max@example.com",
  "customerPhone": "+41 79 123 45 67",
  "deliveryAddress": {
    "street": "Hauptstrasse 1",
    "city": "Zürich",
    "postalCode": "8001"
  },
  "orderType": "DELIVERY",
  "paymentMethod": "CARD",
  "notes": "Keine Zwiebeln bitte"
}
```

```json
// Response 201
{
  "id": 1,
  "orderNumber": "ORD-2025-0001",
  "status": "PENDING",
  "items": [...],
  "totalPrice": 37.50,
  "createdAt": "2025-12-04T20:00:00Z",
  "estimatedDelivery": "2025-12-04T20:45:00Z"
}
```

**Tests für POST /api/orders**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T6.2.1 | Bestellung erstellen | Valid Body + Cart | 201 Created |
| T6.2.2 | Leerer Warenkorb | Keine Items | 400 Bad Request |
| T6.2.3 | Ungültige Adresse | Fehlende Felder | 400 Bad Request |
| T6.2.4 | Kafka Event gesendet | Nach Bestellung | `order.created` Event |

---

#### `GET /api/orders/{id}` - Bestellung abrufen

| Aspekt       | Details                  |
| ------------ | ------------------------ |
| **Method**   | GET                      |
| **Path**     | `/api/orders/{id}`       |
| **Response** | `200 OK` + Order Details |

**Tests für GET /api/orders/{id}**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T6.2.5 | Bestellung abrufen | Valid ID | 200 + Order |
| T6.2.6 | Nicht existierend | `id=9999` | 404 Not Found |

---

#### `PUT /api/orders/{id}/status` - Status ändern (KDS)

| Aspekt       | Details                       |
| ------------ | ----------------------------- |
| **Method**   | PUT                           |
| **Path**     | `/api/orders/{id}/status`     |
| **Auth**     | 🔒 `RESTAURANT_STAFF`         |
| **Body**     | `{ "status": "IN_PROGRESS" }` |
| **Response** | `200 OK`                      |

**Order Status Flow**:

```
PENDING → CONFIRMED → IN_PROGRESS → READY → DELIVERED/PICKED_UP
                ↓
            CANCELLED
```

**Tests für PUT /api/orders/{id}/status**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T6.2.7 | Status ändern | `CONFIRMED` | 200 OK |
| T6.2.8 | Ungültiger Übergang | `PENDING → READY` | 400 Bad Request |
| T6.2.9 | Kafka Event | Nach Update | `order.updated` Event |

---

#### `GET /api/orders` - Alle Bestellungen (KDS)

| Aspekt       | Details                           |
| ------------ | --------------------------------- |
| **Method**   | GET                               |
| **Path**     | `/api/orders`                     |
| **Auth**     | 🔒 `RESTAURANT_STAFF`             |
| **Query**    | `?status=PENDING&date=2025-12-04` |
| **Response** | `200 OK` + Liste                  |

**Tests für GET /api/orders**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T6.2.10 | Alle Bestellungen | Admin Token | 200 + Liste |
| T6.2.11 | Nach Status filtern | `?status=PENDING` | Nur PENDING |
| T6.2.12 | Nach Datum filtern | `?date=today` | Nur heute |

---

## Meilenstein 7: Payment Service (Mockup)

**Ziel**: Zahlungs-Simulation (kein echtes Payment Gateway)

### 7.1 Service Setup

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                  | Status |
| ----- | ----------------------------- | ------ |
| 7.1.1 | Spring Boot Projekt erstellen | ✅     |
| 7.1.2 | H2 In-Memory Datenbank        | ✅     |
| 7.1.3 | Payment Entity                | ✅     |
| 7.1.4 | Mock Payment Processing       | ✅     |
| 7.1.5 | Swagger Dokumentation         | ✅     |

**Datenbank-Argumentation** (Anforderung 1):

> **H2 (In-Memory)** statt MySQL, weil:
>
> - Payment ist nur ein Mockup
> - Keine persistenten Zahlungsdaten erforderlich
> - Einfaches Testing ohne externe DB
> - Demonstriert verschiedene DB-Strategien

### 7.2 REST Endpoints

#### `POST /api/payments` - Zahlung durchführen

| Aspekt       | Details                   |
| ------------ | ------------------------- |
| **Method**   | POST                      |
| **Path**     | `/api/payments`           |
| **Body**     | Payment Request           |
| **Response** | `200 OK` + Payment Result |

```json
// Request
{
  "orderId": 1,
  "amount": 37.50,
  "method": "CARD",
  "cardNumber": "4242424242424242",
  "expiryDate": "12/26",
  "cvv": "123"
}

// Response 200 (Mockup - immer erfolgreich)
{
  "paymentId": "PAY-2025-0001",
  "status": "COMPLETED",
  "transactionId": "TXN-ABC123",
  "processedAt": "2025-12-04T20:00:00Z"
}
```

**Tests für POST /api/payments**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T7.2.1 | Zahlung erfolgreich | Valid Data | 200 + COMPLETED |
| T7.2.2 | Test-Karte "fail" | `4000000000000002` | 402 Payment Failed |
| T7.2.3 | Ungültige Karte | `1234` | 400 Bad Request |

---

## Meilenstein 8: Auth Service

**Ziel**: JWT-basierte Authentifizierung

### 8.1 Service Setup

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                  | Status |
| ----- | ----------------------------- | ------ |
| 8.1.1 | Spring Boot Projekt erstellen | ✅     |
| 8.1.2 | MySQL Datenbank-Anbindung     | ✅     |
| 8.1.3 | Spring Security Konfiguration | ✅     |
| 8.1.4 | JWT Token Generation          | ✅     |
| 8.1.5 | User Entity                   | ✅     |
| 8.1.6 | Password Hashing (BCrypt)     | ✅     |
| 8.1.7 | Swagger Dokumentation         | ✅     |

### 8.2 REST Endpoints

#### `POST /api/auth/register` - Kunden-Registrierung

| Aspekt       | Details                       |
| ------------ | ----------------------------- |
| **Method**   | POST                          |
| **Path**     | `/api/auth/register`          |
| **Body**     | User Registration             |
| **Response** | `201 Created` + User + Tokens |

**Tests für POST /api/auth/register**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T8.2.1 | Registrierung erfolgreich | Valid Data | 201 + Tokens |
| T8.2.2 | Email bereits existiert | Duplicate Email | 409 Conflict |
| T8.2.3 | Ungültige Email | `invalid-email` | 400 Bad Request |
| T8.2.4 | Passwort zu kurz | `<8 chars` | 400 Bad Request |

---

#### `POST /api/auth/login` - Login

| Aspekt       | Details               |
| ------------ | --------------------- |
| **Method**   | POST                  |
| **Path**     | `/api/auth/login`     |
| **Body**     | `{ email, password }` |
| **Response** | `200 OK` + Tokens     |

**Tests für POST /api/auth/login**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T8.2.5 | Login erfolgreich | Valid Credentials | 200 + Tokens |
| T8.2.6 | Falsches Passwort | Wrong Password | 401 Unauthorized |
| T8.2.7 | User nicht gefunden | Unknown Email | 401 Unauthorized |

---

## Meilenstein 9: Circuit Breaker & Resilience

**Ziel**: Fehlertoleranz zwischen Services

### 9.1 Resilience4j Integration

**Status**: ✅ Abgeschlossen

| Task  | Beschreibung                              | Status |
| ----- | ----------------------------------------- | ------ |
| 9.1.1 | Resilience4j Dependency hinzufügen        | ✅     |
| 9.1.2 | Circuit Breaker für Product-Service Calls | ✅     |
| 9.1.3 | Fallback-Methoden implementieren          | ✅     |
| 9.1.4 | Retry-Mechanismus                         | ✅     |
| 9.1.5 | Rate Limiter (optional)                   | ⬜     |

**Konfiguration**:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
```

**Tests für Circuit Breaker**:
| Test-ID | Test-Beschreibung | Input | Erwartetes Ergebnis |
|---------|-------------------|-------|---------------------|
| T9.1.1 | Service nicht erreichbar | Product Service down | Fallback Response |
| T9.1.2 | Circuit opens | 50% Failures | Circuit OPEN |
| T9.1.3 | Circuit half-open | Nach 10s | 3 Test-Calls |

---

## Meilenstein 10: Testing & Dokumentation

### 10.1 Unit Tests

**Status**: ✅ Abgeschlossen (102 Tests bestanden)

| Task   | Beschreibung               | Status        |
| ------ | -------------------------- | ------------- |
| 10.1.1 | Product Service Unit Tests | ✅ (38 Tests) |
| 10.1.2 | Cart Service Unit Tests    | ✅ (20 Tests) |
| 10.1.3 | Order Service Unit Tests   | ✅ (16 Tests) |
| 10.1.4 | Auth Service Unit Tests    | ✅ (16 Tests) |
| 10.1.5 | Payment Service Unit Tests | ✅ (9 Tests)  |
| 10.1.6 | API Gateway Unit Tests     | ✅ (3 Tests)  |

### 10.2 Integration Tests (Postman)

**Status**: ⬜ Nicht gestartet

| Task   | Beschreibung                 | Status |
| ------ | ---------------------------- | ------ |
| 10.2.1 | Postman Collection erstellen | ⬜     |
| 10.2.2 | Environment Variables        | ⬜     |
| 10.2.3 | Automated Test Scripts       | ⬜     |
| 10.2.4 | Newman CLI Integration       | ⬜     |

### 10.3 Swagger/OpenAPI

**Status**: ✅ Abgeschlossen

| Task   | Beschreibung                 | Status |
| ------ | ---------------------------- | ------ |
| 10.3.1 | Springdoc OpenAPI Dependency | ✅     |
| 10.3.2 | API Annotations              | ✅     |
| 10.3.3 | Swagger UI verfügbar         | ✅     |

**Erfolgskriterium**: Swagger UI unter `http://localhost:8080/swagger-ui.html`

---

## 📊 Datenbank-Strategie (Anforderung 1)

| Service             | Datenbank   | Begründung                                             |
| ------------------- | ----------- | ------------------------------------------------------ |
| **Product Service** | MySQL       | Persistente Produktdaten, relationale Struktur         |
| **Order Service**   | MySQL       | Bestellhistorie muss persistent sein, komplexe Queries |
| **Auth Service**    | MySQL       | User-Daten müssen sicher gespeichert werden            |
| **Cart Service**    | Redis       | Temporär, schnell, TTL für Sessions                    |
| **Payment Service** | H2 (Memory) | Mockup, keine echte Persistenz nötig                   |

---

## 📁 Projekt-Struktur

```
restaurant-application/
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions Pipeline
├── docker-compose.yml          # Infrastruktur
├── eureka-server/              # Service Discovery
├── api-gateway/                # Gateway
├── product-service/            # Produkte (MySQL)
├── cart-service/               # Warenkorb (Redis)
├── order-service/              # Bestellungen (MySQL)
├── payment-service/            # Zahlung Mockup (H2)
├── auth-service/               # Authentifizierung (MySQL)
├── website/                    # React Frontend
└── client/                     # Tauri KDS App
```

---

## ✅ Checkliste Endabgabe

- [ ] Alle Services laufen in Docker
- [ ] Eureka Dashboard zeigt alle Services
- [ ] Gateway routet korrekt
- [ ] Alle CRUD-Endpoints funktionieren
- [ ] Kafka Events werden gesendet
- [ ] Circuit Breaker funktioniert
- [ ] JWT Auth funktioniert
- [ ] Swagger Dokumentation vollständig
- [ ] Unit Tests vorhanden
- [ ] Postman Collection funktioniert
- [ ] GitHub Pipeline grün
- [ ] Cloud Deployment aktiv

---

_Erstellt: 04.12.2025_  
_Letzte Aktualisierung: 04.12.2025_
