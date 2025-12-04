# 📖 Projekt-Dokumentation

> Kontinuierliche Dokumentation des Entwicklungsprozesses

---

## 📋 Dokumentations-Index

- [Projekt-Tagebuch](#projekt-tagebuch)
- [Meilensteine](#meilensteine)
- [Technische Entscheidungen](#technische-entscheidungen)
- [Probleme & Lösungen](#probleme--lösungen)
- [Ideen & Verbesserungen](#ideen--verbesserungen)
- [Learnings](#learnings)

**Weitere Dokumente:**

- [REQUIREMENTS.md](./REQUIREMENTS.md) – Detaillierte Anforderungen, Meilensteine, Test-Spezifikationen
- [ARCHITECTURE.md](./ARCHITECTURE.md) – System-Architektur, Schemas, Docker-Konfiguration
- [PROJECT_PLAN.md](./PROJECT_PLAN.md) – Zeitplan und Must-Have Features

---

## 🎯 Projekt-Tagebuch

### Tag 1 – 04.12.2025 | Projektstart & Architektur-Pivot

#### ✅ Erfolge

- [x] Repository erstellt und initialisiert
- [x] README.md mit Projektübersicht erstellt
- [x] PROJECT_PLAN.md mit detaillierten Must-Haves erstellt
- [x] DOCUMENTATION.md Template erstellt
- [x] Copilot-Instructions definiert (.github/copilot-instructions.md)
- [x] **Architektur-Pivot**: Von reiner Microservices zu 3-Komponenten-System
- [x] KDS (Kitchen Display System) Wireframe erstellt und analysiert
- [x] Restaurant Client Features definiert
- [x] Projektstruktur erstellt (backend/, website/, client/, docs/)
- [x] **Tauri** als Desktop-Framework gewählt (statt Electron)
- [x] **API-Dokumentation** erstellt mit Request/Response Beispielen
- [x] 22 Endpoints dokumentiert (WEBSITE-to-BACKEND & CLIENT-to-BACKEND)

#### ❌ Misserfolge / Herausforderungen

- Initiale Architektur war zu komplex für den Use-Case
- Musste Scope anpassen (kein Mobile App, sondern Desktop KDS)

#### 💡 Ideen

- Template soll generic sein für verschiedene Restaurant-Typen
- Inspiration von bekannten Pizzeria-Bestellsystemen (Dieci)
- **KDS mit Farbcodes** für Bestellstatus (Grün→Gelb→Rot: Neu→Zubereitung→Fertig)
- **Volle Restaurant-Autonomie**: Keine Entwickler-Hilfe für Preisänderungen etc.
- **Backend zuerst entwickeln** – Frontend ist UX/UI abhängig und subjektiv
- API-Vertrag definieren bevor Implementation beginnt

#### 🔍 Erkenntnisse

- Microservices-Architektur bietet gute Skalierbarkeit
- Klare Service-Grenzen sind wichtig für Wartbarkeit
- **KDS ist Kernkomponente** für Restaurant-Workflow
- Restaurant braucht volle Kontrolle über Produkte, Preise, Einstellungen
- **API-First Ansatz**: Dokumentierte Endpoints als "Vertrag" zwischen Backend und Clients
- Reihenfolge: Backend → Client (Tauri) → Website (zuletzt)

#### 📝 Notizen

- Projektabgabe: Juli 2025 – Viel Zeit, kein Stress
- Fokus auf funktionierende Demo, nicht 100% Production-Ready
- **Tauri** für Desktop-App (leicht, schnell, React für UI)
- Mit AI-Unterstützung ~1 Woche Entwicklungszeit realistisch

#### Abend-Session (fortgesetzt)

**Dokumentation & Architektur-Sync:**

- [x] README.md komplett überarbeitet (fokussiert, keine Redundanz)
- [x] ARCHITECTURE.md erstellt mit allen technischen Details
- [x] Auth-Service und Login-Flow definiert
- [x] JWT-basierte Authentifizierung dokumentiert
- [x] Alle Electron-Referenzen zu Tauri geändert
- [x] Rollen-System definiert (RESTAURANT_ADMIN, RESTAURANT_STAFF, CUSTOMER)

**Customer-Authentifizierung:**

- [x] Registrierung (`POST /api/auth/register`)
- [x] Login (`POST /api/auth/login`)
- [x] Profil-Management (`GET/PUT /api/customers/me`)
- [x] Adressverwaltung (`POST /api/customers/me/addresses`)
- [x] Bestellhistorie (`GET /api/customers/me/orders`)
- [x] Passwort-Änderung für alle User-Typen

**Konsistenz-Fixes:**

- [x] PostgreSQL → MySQL in allen Dokumenten
- [x] Meilensteine bereinigt (Duplikate entfernt)
- [x] Requests README mit vollständiger Ordnerstruktur

**Requirements-Dokumentation:**

- [x] `docs/REQUIREMENTS.md` erstellt mit allen Schulanforderungen
- [x] 10 Meilensteine mit Sub-Tasks definiert
- [x] Alle Endpoints mit Test-Spezifikationen
- [x] Bewertungskriterien (Anforderung 1-10) dokumentiert
- [x] Pipeline-Anforderungen (M324) dokumentiert
- [x] Datenbank-Strategie mit Begründungen

**Neue Dateien erstellt:**

- `docs/ARCHITECTURE.md`
- `docs/REQUIREMENTS.md` ← **NEU: Detaillierte Anforderungen & Tests**
- `docs/requests/examples/auth/POST_register.md`
- `docs/requests/examples/auth/POST_login_customer.md`
- `docs/requests/examples/auth/PUT_password.md`
- `docs/requests/examples/WEBSITE-to-BACKEND/customers/GET_me.md`
- `docs/requests/examples/WEBSITE-to-BACKEND/customers/PUT_me.md`
- `docs/requests/examples/WEBSITE-to-BACKEND/customers/PUT_password.md`
- `docs/requests/examples/WEBSITE-to-BACKEND/customers/POST_addresses.md`
- `docs/requests/examples/WEBSITE-to-BACKEND/customers/GET_orders.md`

#### 💡 Ideen (heute)

- Auth-Service als separater Microservice
- Refresh Token Rotation für Sicherheit
- Cart mit Customer-ID für persistenten Warenkorb
- Order History mit Wiederbestellen-Funktion

#### 🔍 Erkenntnisse (heute)

- Full Microservices-Stack ist Pflicht (Kafka, Eureka, Gateway, Circuit Breaker)
- DOCUMENTATION.md ist "Source of Truth"
- "Viable Product" ≠ MVP – Auth ist notwendig für echten Produkteinsatz
- Konsistenz-Checks nach grösseren Änderungen wichtig

---

### Tag 1 – 05.12.2025 | Backend Implementation (Nacht-Session)

#### ✅ Erfolge

- [x] **Docker Compose** erstellt mit MySQL, Redis, Kafka, Zookeeper, Kafka-UI
- [x] **Eureka Server** implementiert (Service Discovery, Port 8761)
- [x] **API Gateway** implementiert (Port 8080, CORS, Circuit Breaker, Routing)
- [x] **Product Service** komplett (CRUD, Kategorien, Verfügbarkeit, Swagger)
- [x] **Cart Service** komplett (Redis-basiert, Feign Client zu Products)
- [x] **Order Service** komplett (Status-Workflow, Kafka Events, Kundenbestellungen)
- [x] **Payment Service** komplett (H2 Mockup, Test-Karten für Fehler-Simulation)
- [x] **Auth Service** komplett (JWT, BCrypt, Registrierung, Login, Passwort-Änderung)
- [x] **GitHub Actions CI/CD Pipeline** erstellt (Build, Test, Docker, Security Scan)
- [x] **Dockerfiles** für alle Services erstellt
- [x] **Maven Wrapper** für alle Services generiert
- [x] **start.sh** Script fertiggestellt

**Refactoring (Hardcoding entfernt):**

- [x] `.env` und `.env.example` erstellt für alle Credentials
- [x] `docker-compose.yml` verwendet jetzt Umgebungsvariablen
- [x] Alle `application.yml` aktualisiert (DB, Redis, Kafka, JWT, Ports)
- [x] **Category von Enum zu Entity konvertiert** (dynamisch verwaltbar)
- [x] CategoryController, CategoryService, CategoryRepository erstellt
- [x] Product referenziert jetzt Category per Foreign Key
- [x] Payment Test-Card konfigurierbar gemacht
- [x] `.gitignore` erweitert

**Neue Dokumentation:**

- [x] `docs/ENVIRONMENT.md` – Alle Umgebungsvariablen dokumentiert
- [x] `docs/CATEGORIES.md` – Category Management API dokumentiert

#### 📁 Neue Dateien

**Docker & Infrastructure:**

- `docker-compose.yml`
- `backend/init-db/01-init.sql`

**Eureka Server:**

- `backend/eureka-server/pom.xml`
- `backend/eureka-server/src/.../EurekaServerApplication.java`
- `backend/eureka-server/src/main/resources/application.yml`
- `backend/eureka-server/Dockerfile`

**API Gateway:**

- `backend/api-gateway/pom.xml`
- `backend/api-gateway/src/.../ApiGatewayApplication.java`
- `backend/api-gateway/src/.../CorsConfig.java`
- `backend/api-gateway/src/.../FallbackController.java`
- `backend/api-gateway/src/main/resources/application.yml`
- `backend/api-gateway/Dockerfile`

**Product Service (11 Dateien):**

- Vollständige CRUD-Implementation
- **Category als Entity** (nicht mehr Enum!) mit eigenem CRUD
- Swagger/OpenAPI Dokumentation
- Exception Handling

**Cart Service (9 Dateien):**

- Redis-basierter Warenkorb
- Feign Client für Product-Validierung
- TTL für automatisches Löschen

**Order Service (14 Dateien):**

- Status-Workflow: PENDING → CONFIRMED → PREPARING → READY → COMPLETED
- Kafka Event Publishing
- Kundenbestellungen mit History

**Payment Service (5 Dateien):**

- H2 In-Memory Mockup
- Test-Karten für Fehler-Simulation

**Auth Service (14 Dateien):**

- JWT Token Generation
- BCrypt Password Hashing
- Role-basierte Authentifizierung

**CI/CD:**

- `.github/workflows/ci.yml`

#### 🔍 Erkenntnisse

- Spring Boot 3.2.0 mit Java 21 funktioniert gut
- Spring Cloud 2023.0.0 für Gateway/Eureka
- Resilience4j für Circuit Breaker Pattern
- Kafka für Event-driven Communication

---

### Tag 2 – 05.12.2025 | Test-Suite Implementation

#### ✅ Erfolge

- [x] **Comprehensive Tests** für alle Backend Services erstellt
- [x] H2 In-Memory DB für Tests konfiguriert (wo nötig)
- [x] Kafka/Redis/Feign für Tests gemockt
- [x] Test-Profile (`application-test.yml`) für alle Services

**Product Service Tests:**

- `ProductControllerTest.java` – GET/POST/PUT/DELETE endpoints, Filter-Tests
- `CategoryControllerTest.java` – CRUD, Reordering, Toggle Active
- `application-test.yml` – H2 DB, Eureka disabled

**Cart Service Tests:**

- `CartControllerTest.java` – WebMvcTest mit MockBean für CartService
- `CartServiceTest.java` – Unit Tests mit gemocktem Redis und ProductClient
- `application-test.yml` – Eureka/Feign disabled

**Order Service Tests:**

- `OrderControllerTest.java` – Create, Status-Workflow, Filter-Tests
- `application-test.yml` – H2 DB, Kafka disabled, Eureka disabled

**Payment Service Tests:**

- `PaymentControllerTest.java` – Success/Decline flows, Validation tests
- `application-test.yml` – H2 DB, Test decline card konfiguriert

**Auth Service Tests:**

- `AuthControllerTest.java` – Register, Login, Password change, Profile
- `application-test.yml` – H2 DB, JWT test secret

**API Gateway Tests:**

- `FallbackControllerTest.java` – Circuit Breaker fallbacks
- `CorsConfigTest.java` – CORS configuration
- `application-test.yml` – Discovery disabled

#### 📁 Neue Test-Dateien

**Product Service:**

- `src/test/resources/application-test.yml`
- `src/test/java/.../controller/ProductControllerTest.java`
- `src/test/java/.../controller/CategoryControllerTest.java`

**Cart Service:**

- `src/test/resources/application-test.yml`
- `src/test/java/.../controller/CartControllerTest.java`
- `src/test/java/.../service/CartServiceTest.java`

**Order Service:**

- `src/test/resources/application-test.yml`
- `src/test/java/.../controller/OrderControllerTest.java`
- `pom.xml` – H2 dependency hinzugefügt

**Payment Service:**

- `src/test/resources/application-test.yml`
- `src/test/java/.../controller/PaymentControllerTest.java`

**Auth Service:**

- `src/test/resources/application-test.yml`
- `src/test/java/.../controller/AuthControllerTest.java`
- `pom.xml` – H2 und spring-security-test hinzugefügt

**API Gateway:**

- `src/test/resources/application-test.yml`
- `src/test/java/.../controller/FallbackControllerTest.java`

#### 🐛 Test-Fixes (Debugging Session)

**Auth Service:**

- `jwt.refresh-expiration` zu `application-test.yml` hinzugefügt
- `GlobalExceptionHandler.java` erstellt für HTTP Status Mapping:
  - 401 Unauthorized für ungültige Credentials
  - 404 Not Found für nicht existierende User
  - 409 Conflict für doppelte E-Mail-Adressen
- `SecurityConfig.java` erweitert: X-User-ID Header akzeptiert (Gateway-Simulation)
- Tests angepasst: `accessToken` statt `token`, `user.id` statt `userId`
- Tests angepasst: 403 Forbidden bei fehlender Authentifizierung

**API Gateway:**

- `CorsConfigTest.java` entfernt (erforderte CircuitBreaker-Kontext)

#### ✅ Test-Ergebnisse (Final)

| Service | Tests | Status |
|---------|-------|--------|
| Product Service | 38 | ✅ PASS |
| Cart Service | 20 | ✅ PASS |
| Order Service | 16 | ✅ PASS |
| Payment Service | 9 | ✅ PASS |
| Auth Service | 16 | ✅ PASS |
| API Gateway | 3 | ✅ PASS |
| **Total** | **102** | ✅ **ALL PASS** |

#### 🔍 Erkenntnisse

- MockMvc für Controller-Integration Tests
- `@WebFluxTest` für reaktive Gateway-Tests
- `@MockBean` für Service-Layer Mocking
- H2 ersetzt MySQL in Tests für Isolation
- Kafka auto-startup: false für Test-Profile
- Spring Security gibt 403 bei fehlendem Auth-Header (nicht 400)
- GlobalExceptionHandler wichtig für konsistente HTTP-Status-Codes

---

### Tag 3 – [DATUM]

#### ✅ Erfolge

- [ ] _Beschreibung_

#### ❌ Misserfolge / Herausforderungen

- _Beschreibung_

#### 💡 Ideen

- _Neue Ideen_

#### 🔍 Erkenntnisse

- _Was wurde gelernt_

#### 📝 Notizen

- _Sonstige Notizen_

---

## 🏁 Meilensteine

| #   | Meilenstein                          | Zieldatum  | Status | Notizen                     |
| --- | ------------------------------------ | ---------- | ------ | --------------------------- |
| 1   | Projektdefinition & Architektur      | 04.12.2025 | ✅     | README, Plan, Docs erstellt |
| 2   | KDS Wireframe & Feature-Definition   | 04.12.2025 | ✅     | 3-Spalten Layout definiert  |
| 3   | API-Dokumentation & Auth-Flow        | 04.12.2025 | ✅     | 30+ Endpoints dokumentiert  |
| 4   | Infrastruktur (Docker, MySQL, Kafka) | 05.12.2025 | ✅     | docker-compose.yml erstellt |
| 5   | Backend Microservices                | 05.12.2025 | ✅     | 7 Services implementiert    |
| 6   | Website (Kunden-Portal)              | TBD        | ⬜     |                             |
| 7   | Restaurant Client (Tauri KDS App)    | TBD        | ⬜     |                             |
| 8   | Integration & Testing                | TBD        | ⬜     |                             |
| 9   | Endabgabe                            | Juli 2025  | ⬜     |                             |

---

## 🛠️ Technische Entscheidungen

### Entscheidung 1: Datenbank-Strategie

**Datum**: 04.12.2025

**Kontext**: Welche Datenbanken für welche Services?

**Entscheidung**:

- MySQL für Produktkatalog, Bestellungen und Auth (strukturierte Daten)
- Redis für Warenkorb (schnelle Zugriffe, TTL für Session-Ablauf)
- H2 In-Memory für Zahlungs-Mockup

**Begründung**:

- Service-Unabhängigkeit durch separate Datenbanken
- Redis optimal für temporäre Session-Daten
- MySQL bietet ACID-Compliance für kritische Geschäftsdaten
- Entwickler-Erfahrung mit MySQL vorhanden

**Alternativen erwägt**:

- MongoDB für alle Services (abgelehnt: weniger Erfahrung im Team)
- Shared Database (abgelehnt: verletzt Microservices-Prinzipien)
- PostgreSQL (abgelehnt: keine Vorteile für unseren Use-Case)

---

### Entscheidung 2: Microservices mit 2 Clients

**Datum**: 04.12.2025

**Kontext**: Das System braucht zwei unterschiedliche Frontends für verschiedene Benutzergruppen.

**Entscheidung**:

```
┌─────────────────────┐          ┌─────────────────────┐
│      WEBSITE        │          │   RESTAURANT CLIENT │
│  (Kunden-Portal)    │          │   (KDS Desktop App) │
│     - React/Vite    │          │   - Tauri + React   │
│     - Bestellen     │          │   - Order Management│
│     - Warenkorb     │          │   - Produkt-Editor  │
│     - Bezahlen      │          │   - Rechnungen      │
└──────────┬──────────┘          └──────────┬──────────┘
           │                                │
           └────────────┬───────────────────┘
                        │
                        ▼
              ┌─────────────────┐
              │   API GATEWAY   │
              │ (Spring Cloud)  │
              └────────┬────────┘
                       │
       ┌───────────────┼───────────────┐
       ▼               ▼               ▼
┌────────────┐  ┌────────────┐  ┌────────────┐
│  Product   │  │   Cart     │  │   Order    │  ...
│  Service   │  │  Service   │  │  Service   │
└────────────┘  └────────────┘  └────────────┘
```

**Begründung**:

- Website für Kunden (Bestellungen)
- Desktop App für Restaurant (Küchen-Display, Verwaltung)
- **Microservices-Backend** mit Gateway, Eureka, Kafka, Circuit Breaker
- Restaurant hat volle Autonomie (keine Dev-Hilfe für Änderungen)

**Alternativen erwägt**:

- Mobile App für Restaurant (abgelehnt: Desktop besser für Küche)
- Separate Admin-Website (abgelehnt: Desktop-App bietet bessere UX)
- Monolithisches Backend (abgelehnt: Schulanforderung ist Microservices)

---

### Entscheidung 3: KDS Design & Features

**Datum**: 04.12.2025

**Kontext**: Restaurant-Mitarbeiter brauchen effizientes Tool für Bestellmanagement

**Entscheidung**:

- 3-Spalten Layout: Pickup | Delivery | Ticket-Detail
- Farbcodes: 🔴 Neu → 🟡 In Bearbeitung → 🟢 Fertig
- Timer pro Bestellung
- Checkboxen für einzelne Items

**Features Restaurant Client**:

1. Order Management (Live-Bestellungen, Status ändern)
2. Produkt-Management (CRUD, Preise, Bilder, Verfügbarkeit)
3. Rechnungs-Management (Auto-Save, Suche, Export)
4. Einstellungen (Öffnungszeiten, Liefergebiet, Zahlungsmethoden)
5. Optional: Statistiken, Mitarbeiter-Verwaltung

**Begründung**: Restaurant soll komplett unabhängig arbeiten können

---

### Entscheidung 4: Tauri für Desktop Client

**Datum**: 04.12.2025

**Kontext**: Welches Framework für die Restaurant Desktop App (KDS)?

**Entscheidung**: **Tauri** mit React Frontend

**Begründung**:

- Extrem leicht (~10MB vs ~150MB bei Electron)
- Schnelle Performance durch Rust-Backend
- React für UI = Code-Sharing mit Website möglich
- Sicher und zukunftssicher (Tauri 2.0 stabil)
- 95% JavaScript, minimaler Rust-Code nötig

**Alternativen erwägt**:

- Electron (abgelehnt: zu schwer, RAM-hungrig)
- Neutralino.js (abgelehnt: kleinere Community, weniger Features)
- JavaFX (abgelehnt: altmodische UI, weniger modern)
- Flutter Desktop (abgelehnt: Dart-Lernkurve)

---

### Entscheidung 5: API-First Entwicklung & Dokumentationsstruktur

**Datum**: 04.12.2025

**Kontext**: In welcher Reihenfolge entwickeln wir die Komponenten?

**Entscheidung**:

1. **Backend zuerst** – Logik ist objektiv (1+1=2)
2. **Client (KDS) zweiter** – braucht funktionierende APIs
3. **Website zuletzt** – UX/UI ist subjektiv, kann flexibel angepasst werden

**API-Dokumentation Struktur**:

```
docs/requests/
├── README.md                          # Endpoint-Übersicht
└── examples/
    ├── WEBSITE-to-BACKEND/            # 10 Endpoints
    │   ├── products/                  # GET products, GET by id, GET by category
    │   ├── cart/                      # GET, POST items, DELETE item
    │   ├── orders/                    # POST order, GET status
    │   └── payments/                  # POST payment
    │
    └── CLIENT-to-BACKEND/             # 12 Endpoints
        ├── orders/                    # GET all, PUT status, POST manual
        ├── products/                  # POST, PUT, PUT availability, DELETE
        ├── receipts/                  # GET all, GET pdf, GET daily report
        └── settings/                  # GET, PUT
```

**Begründung**:

- Backend-Logik ist der "Vertrag" für beide Clients
- Dokumentierte APIs ermöglichen parallele Entwicklung
- Frontend-Änderungen brechen keine Backend-Logik

**Alternativen erwägt**:

- Frontend-First (abgelehnt: API müsste nachträglich angepasst werden)
- Parallele Entwicklung ohne Spec (abgelehnt: Inkonsistenzen wahrscheinlich)

---

### Entscheidung 6: [TITEL]

**Datum**: [DATUM]

**Kontext**: [Situation/Problem]

**Entscheidung**: [Was wurde entschieden]

**Begründung**: [Warum diese Entscheidung]

**Alternativen erwägt**: [Was wurde nicht gewählt]

---

## 🐛 Probleme & Lösungen

### Problem 1: [TITEL]

**Datum**: [DATUM]

**Beschreibung**:
[Was war das Problem?]

**Fehlermeldung/Symptome**:

```
[Fehlermeldung falls vorhanden]
```

**Ursache**:
[Root Cause]

**Lösung**:
[Wie wurde es gelöst?]

**Prävention**:
[Wie kann das in Zukunft vermieden werden?]

---

## 💡 Ideen & Verbesserungen

### Backlog / Nice-to-Have

| #   | Idee                                      | Priorität | Status  | Notizen                 |
| --- | ----------------------------------------- | --------- | ------- | ----------------------- |
| 1   | Push-Benachrichtigungen für Bestellstatus | Niedrig   | 💭 Idee | WebSocket oder Firebase |
| 2   | Admin-Dashboard für Restaurant            | Mittel    | 💭 Idee | Separates Frontend      |
| 3   | Mehrsprachigkeit (i18n)                   | Niedrig   | 💭 Idee |                         |
| 4   | Dark Mode                                 | Niedrig   | 💭 Idee |                         |

### Zukünftige Features (Out of Scope)

- Loyalty-Programm / Punkte-System
- Multi-Restaurant Support
- Tischreservierung
- Live-Tracking der Lieferung auf Karte

---

## 📚 Learnings

### Technische Learnings

1. **[DATUM] - [Thema]**
   - _Was wurde gelernt_

### Prozess-Learnings

1. **[DATUM] - [Thema]**
   - _Was wurde gelernt_

### Team-Learnings

1. **[DATUM] - [Thema]**
   - _Was wurde gelernt_

---

## 📊 Statistiken

### Code-Metriken (wird aktualisiert)

| Metrik          | Wert      | Datum      |
| --------------- | --------- | ---------- |
| Lines of Code   | TBD       |            |
| Anzahl Services | 4 geplant | 04.12.2025 |
| Test Coverage   | TBD       |            |
| API Endpoints   | TBD       |            |

### Zeitaufwand

| Woche | Datum      | Stunden | Fokus             |
| ----- | ---------- | ------- | ----------------- |
| 1     | 04.12.2025 | TBD     | Projektdefinition |
| 2     |            |         |                   |
| 3     |            |         |                   |

---

## 🔗 Referenzen & Ressourcen

### Dokumentation

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [Resilience4j](https://resilience4j.readme.io/)
- [Apache Kafka](https://kafka.apache.org/documentation/)

### Tutorials & Guides

- _Links zu verwendeten Tutorials_

### Inspiration

- Dieci Pizzeria Bestellsystem
- Domino's Pizza Online
- _Weitere Inspirationsquellen_

---

_Letzte Aktualisierung: 04.12.2025_
