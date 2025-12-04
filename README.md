# 🍕 Restaurant Online-Bestellsystem

> Ein skalierbares Microservices-System für Restaurant-Online-Bestellungen – entwickelt als Schulprojekt

---

## 📖 Was ist das?

Dieses Projekt ist ein **vollständiges Restaurant-Bestellsystem** bestehend aus drei Hauptkomponenten:

| Komponente            | Beschreibung                             | Technologie                |
| --------------------- | ---------------------------------------- | -------------------------- |
| **Website**           | Kunden-Portal zum Bestellen              | React + Vite               |
| **Restaurant Client** | Desktop-App für Küche & Management (KDS) | Tauri + React              |
| **Backend**           | Microservices-API                        | Spring Boot + Spring Cloud |

### 🎯 Ziel

Kunden können online bestellen, das Restaurant sieht Bestellungen live auf einem Küchen-Display und kann Produkte, Preise und Einstellungen selbst verwalten – **ohne Entwickler-Hilfe**.

---

## 🏗️ Architektur-Übersicht

```
┌─────────────────────┐          ┌─────────────────────┐
│      WEBSITE        │          │  RESTAURANT CLIENT  │
│  (Kunden-Portal)    │          │  (KDS Desktop App)  │
│     React/Vite      │          │    Tauri + React    │
└──────────┬──────────┘          └──────────┬──────────┘
           │                                │
           └────────────┬───────────────────┘
                        │ REST API
                        ▼
              ┌───────────────────┐
              │    API GATEWAY    │
              │ (Spring Cloud)    │
              └─────────┬─────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Product     │ │    Cart      │ │   Order      │ ...
│  Service     │ │   Service    │ │   Service    │
└──────────────┘ └──────────────┘ └──────────────┘
        │               │               │
        ▼               ▼               ▼
     MySQL           Redis           MySQL
```

### Microservices-Stack

- **Spring Cloud Gateway** – Zentraler API-Einstiegspunkt
- **Netflix Eureka** – Service Discovery & Registry
- **Apache Kafka** – Event-Driven Kommunikation
- **Resilience4j** – Circuit Breaker für Fehlertoleranz

---

## 📁 Projektstruktur

```
restaurant-application/
├── README.md                    # Diese Datei
├── docker-compose.yml           # Infrastruktur (DB, Kafka, etc.)
│
├── backend/                     # Microservices
│   ├── eureka-server/           # Service Discovery
│   ├── api-gateway/             # Gateway
│   ├── product-service/         # Produktkatalog
│   ├── cart-service/            # Warenkorb
│   ├── order-service/           # Bestellungen
│   ├── payment-service/         # Zahlung (Mockup)
│   └── auth-service/            # Authentifizierung
│
├── website/                     # React Kunden-Portal
├── client/                      # Tauri KDS Desktop App
│
└── docs/                        # Dokumentation
    ├── PROJECT_PLAN.md          # Aufgaben & Meilensteine
    ├── DOCUMENTATION.md         # Entwicklungs-Tagebuch
    └── requests/                # API Request-Beispiele
```

---

## 🚀 Quick Start

### Voraussetzungen

- Docker & Docker Compose
- Java 21+
- Node.js 18+
- Rust (für Tauri Client)

### Starten

```bash
# 1. Repository klonen
git clone https://github.com/isaaclins/restaurant-application.git
cd restaurant-application

# 2. Infrastruktur starten
docker-compose up -d

# 3. Backend Services starten
./start-services.sh

# 4. Website starten
cd website && npm install && npm run dev

# 5. Restaurant Client starten
cd client && npm install && npm run tauri dev
```

---

## 📚 Dokumentation

| Dokument                                    | Inhalt                                           |
| ------------------------------------------- | ------------------------------------------------ |
| [PROJECT_PLAN.md](./docs/PROJECT_PLAN.md)   | Detaillierter Projektplan, Must-Haves, Zeitplan  |
| [DOCUMENTATION.md](./docs/DOCUMENTATION.md) | Entwicklungs-Tagebuch, Entscheidungen, Learnings |
| [docs/requests/](./docs/requests/)          | API Endpoint-Dokumentation mit Beispielen        |
| [ARCHITECTURE.md](./docs/ARCHITECTURE.md)   | Technische Architektur-Details                   |

---

## ✅ Projekt-Anforderungen (Schule)

| Nr. | Anforderung                    | Status |
| --- | ------------------------------ | ------ |
| 1   | Eigene Datenbank pro Service   | ⬜     |
| 2   | Docker für Container           | ⬜     |
| 3   | REST APIs (Spring Controllers) | ⬜     |
| 4   | Sicherheit bei sensiblen Daten | ⬜     |
| 5   | Circuit Breaker (Resilience4j) | ⬜     |
| 6   | Dokumentierte Services         | ⬜     |
| 7   | Architektur-Diagramm           | ⬜     |
| 8   | Clean Code                     | ⬜     |
| 9   | Swagger/OpenAPI Dokumentation  | ⬜     |

---

## 👤 Autor

- Isaac Lins

---

## 📄 Lizenz

Schulprojekt – Dezember 2025

---

_Letzte Aktualisierung: 04.12.2025_
