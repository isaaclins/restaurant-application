# 🎯 Projekt-Plan: Restaurant Online-Bestellsystem

> Detaillierter Plan mit Must-Haves, Zielen und Meilensteinen

---

## 📌 Projekt-Ziele

### Hauptziel

Ein **funktionierendes Restaurant-Bestellsystem** mit 3 Hauptkomponenten:

1. **Website** – Kunden-Bestellportal
2. **Backend Server** – API & Datenbank
3. **Restaurant Client** – KDS (Kitchen Display System) Desktop App

### System-Architektur

```
┌─────────────────────┐          ┌─────────────────────┐
│      WEBSITE        │          │  RESTAURANT CLIENT  │
│  (Kunden-Portal)    │          │  (KDS Desktop App)  │
│  ─────────────────  │          │  ─────────────────  │
│  • Produkte ansehen │          │  • Live-Bestellungen│
│  • Warenkorb        │          │  • Status ändern    │
│  • Bestellen        │          │  • Produkte pflegen │
│  • Bezahlen         │          │  • Rechnungen       │
│  • React/Vite       │          │  • Tauri + React    │
└──────────┬──────────┘          └──────────┬──────────┘
           │                                │
           └────────────┬───────────────────┘
                        │ REST API
                        ▼
              ┌─────────────────────┐
              │   BACKEND SERVER    │
              │   (Spring Boot)     │
              │  ─────────────────  │
              │   • Orders API      │
              │   • Products API    │
              │   • Payments API      │
              │   • MySQL           │
              │   • Redis (Cache)   │
              └─────────────────────┘
```

### Erfolgskriterien

- [ ] Kunde kann Produkte durchsuchen (Website)
- [ ] Kunde kann Produkte in Warenkorb legen (Website)
- [ ] Kunde kann Bestellung aufgeben (Website)
- [ ] Kunde kann (simuliert) bezahlen (Website)
- [ ] Restaurant sieht Live-Bestellungen (Client)
- [ ] Restaurant kann Bestellstatus ändern (Client)
- [ ] Restaurant kann Produkte/Preise verwalten (Client)
- [ ] Restaurant kann Rechnungen einsehen (Client)
- [ ] Alles über Docker startbar
- [ ] Dokumentation ist vollständig

---

## 🚨 MUST-HAVES (Pflicht)

Diese Komponenten **MÜSSEN** implementiert werden, um das Projekt als abgeschlossen zu betrachten.

### Phase 1: Infrastruktur (Grundlage)

| #   | Aufgabe                  | Beschreibung                          | Abhängigkeit | Status |
| --- | ------------------------ | ------------------------------------- | ------------ | ------ |
| 1.1 | **Docker Compose Setup** | Basis docker-compose.yml mit Netzwerk | -            | ✅     |
| 1.2 | **MySQL Container**      | Datenbank für Produkte & Bestellungen | 1.1          | ✅     |
| 1.3 | **Redis Container**      | Cache für Sessions/Warenkorb          | 1.1          | ✅     |

**Definition of Done Phase 1:**

- `docker-compose up` startet alle Container
- Datenbanken sind erreichbar und persistieren Daten

---

### Phase 2: Backend Server (Spring Boot)

| #     | Aufgabe                   | Beschreibung                   | Abhängigkeit | Status |
| ----- | ------------------------- | ------------------------------ | ------------ | ------ |
| 2.1   | **Spring Boot Projekt**   | Basis-Setup mit Dependencies   | 1.2          | ✅     |
| 2.2   | **Products API**          | CRUD für Produkte              | 2.1          | ✅     |
| 2.2.1 | - Entity & Repository     | Product JPA Entity             | 2.2          | ✅     |
| 2.2.2 | - REST Controller         | GET/POST/PUT/DELETE Endpoints  | 2.2.1        | ✅     |
| 2.2.3 | - Kategorien              | Pizza, Drinks, Desserts, etc.  | 2.2.1        | ✅     |
| 2.2.4 | - Bild-Upload             | Produkt-Bilder speichern       | 2.2.1        | ⬜     |
| 2.3   | **Orders API**            | Bestellungen verarbeiten       | 2.1          | ✅     |
| 2.3.1 | - Entity & Repository     | Order JPA Entity               | 2.3          | ✅     |
| 2.3.2 | - REST Controller         | Create/Get/Update Endpoints    | 2.3.1        | ✅     |
| 2.3.3 | - Status-Workflow         | NEW→PREPARING→READY→DELIVERED  | 2.3.1        | ✅     |
| 2.3.4 | - Pickup vs Delivery      | Unterscheidung im Order        | 2.3.1        | ✅     |
| 2.4   | **Cart API**              | Warenkorb-Verwaltung           | 2.1, 1.3     | ✅     |
| 2.4.1 | - Session-basiert         | Redis für schnellen Zugriff    | 2.4          | ✅     |
| 2.4.2 | - Add/Remove/Update       | Warenkorb-Operationen          | 2.4.1        | ✅     |
| 2.5   | **Payments API (Mockup)** | Simulierte Zahlung             | 2.1          | ✅     |
| 2.5.1 | - Payment Endpoint        | POST /payments                 | 2.5          | ✅     |
| 2.5.2 | - Payment Types           | Twint, Bar, Card (simuliert)   | 2.5.1        | ✅     |
| 2.6   | **Receipts API**          | Rechnungen speichern & abrufen | 2.3          | ⬜     |
| 2.7   | **Settings API**          | Restaurant-Einstellungen       | 2.1          | ⬜     |
| 2.7.1 | - Öffnungszeiten          | CRUD für Zeiten                | 2.7          | ⬜     |
| 2.7.2 | - Liefergebiet            | PLZ oder Radius                | 2.7          | ⬜     |
| 2.7.3 | - Mindestbestellwert      | Konfigurierbar                 | 2.7          | ⬜     |
| 2.8   | **Auth API**              | Authentifizierung              | 2.1          | ✅     |
| 2.8.1 | - Restaurant Login        | JWT für Client                 | 2.8          | ✅     |
| 2.8.2 | - Kunden-Registrierung    | Email, Passwort, Profil        | 2.8          | ✅     |
| 2.8.3 | - Kunden-Login            | JWT für Website                | 2.8          | ✅     |
| 2.8.4 | - Token Refresh           | Access Token erneuern          | 2.8          | ⬜     |
| 2.9   | **Customers API**         | Kundenprofil-Verwaltung        | 2.8          | ⬜     |
| 2.9.1 | - Profil CRUD             | GET/PUT eigenes Profil         | 2.9          | ⬜     |
| 2.9.2 | - Adressen-Verwaltung     | CRUD für Lieferadressen        | 2.9          | ⬜     |
| 2.9.3 | - Bestellhistorie         | Eigene Orders abrufen          | 2.9, 2.3     | ⬜     |
| 2.10  | **Swagger/OpenAPI**       | API Dokumentation              | 2.2-2.9      | ✅     |

**Definition of Done Phase 2:**

- Alle APIs funktionieren und sind dokumentiert
- Postman Collection mit Beispiel-Requests
- Daten persistieren in MySQL
- Kunden können sich registrieren und einloggen

---

### Phase 3: Website (Kunden-Portal)

| #     | Aufgabe                 | Beschreibung                    | Abhängigkeit | Status |
| ----- | ----------------------- | ------------------------------- | ------------ | ------ |
| 3.1   | **React Projekt Setup** | Vite + React + TypeScript       | -            | ⬜     |
| 3.2   | **Startseite**          | Restaurant-Info, Hero           | 3.1          | ⬜     |
| 3.3   | **Produkt-Übersicht**   | Kategorien, Produktliste        | 2.2          | ⬜     |
| 3.4   | **Produkt-Details**     | Einzelansicht, Extras, Grössen  | 3.3          | ⬜     |
| 3.5   | **Warenkorb**           | Sidebar oder eigene Seite       | 2.4          | ⬜     |
| 3.6   | **Auth UI**             | Login & Registrierung           | 2.8          | ⬜     |
| 3.6.1 | - Login-Seite           | Email + Passwort                | 3.6          | ⬜     |
| 3.6.2 | - Registrierung         | Formular mit Validierung        | 3.6          | ⬜     |
| 3.6.3 | - Token-Handling        | JWT speichern & refreshen       | 3.6          | ⬜     |
| 3.7   | **Kundenprofil**        | Eigene Daten verwalten          | 2.9          | ⬜     |
| 3.7.1 | - Profil bearbeiten     | Name, Telefon ändern            | 3.7          | ⬜     |
| 3.7.2 | - Adressen verwalten    | Hinzufügen, Bearbeiten, Löschen | 3.7          | ⬜     |
| 3.7.3 | - Bestellhistorie       | Vergangene Bestellungen         | 3.7          | ⬜     |
| 3.8   | **Checkout**            | Adresse, Pickup/Delivery        | 2.3          | ⬜     |
| 3.9   | **Zahlung**             | Zahlungsart wählen              | 2.5          | ⬜     |
| 3.10  | **Bestellbestätigung**  | Danke-Seite mit Details         | 3.9          | ⬜     |
| 3.11  | **Responsive Design**   | Mobile-First                    | 3.1-3.10     | ⬜     |

**Definition of Done Phase 3:**

- Kompletter Bestellprozess durchführbar
- Responsive auf Mobile & Desktop
- Verbindung zum Backend funktioniert

---

### Phase 4: Restaurant Client (KDS Desktop App)

| #     | Aufgabe                    | Beschreibung                  | Abhängigkeit | Status |
| ----- | -------------------------- | ----------------------------- | ------------ | ------ |
| 4.1   | **Tauri Projekt Setup**    | Tauri + React                 | -            | ⬜     |
| 4.2   | **Order Dashboard**        | 3-Spalten Layout (Wireframe)  | 2.3          | ⬜     |
| 4.2.1 | - Pickup Spalte            | Abholungen links              | 4.2          | ⬜     |
| 4.2.2 | - Delivery Spalte          | Lieferungen mitte             | 4.2          | ⬜     |
| 4.2.3 | - Ticket Detail            | Ausgewählte Bestellung rechts | 4.2          | ⬜     |
| 4.2.4 | - Farbcodes                | Grün→Gelb→Rot nach Status     | 4.2          | ⬜     |
| 4.2.5 | - Timer                    | Zeit seit Bestellung          | 4.2          | ⬜     |
| 4.2.6 | - Status ändern            | Click to advance status       | 4.2          | ⬜     |
| 4.3   | **Neue Bestellung**        | Manuell erstellen (Walk-in)   | 2.3          | ⬜     |
| 4.4   | **Produkt-Manager**        | CRUD für Produkte             | 2.2          | ⬜     |
| 4.4.1 | - Produkt erstellen        | Name, Preis, Kategorie, Bild  | 4.4          | ⬜     |
| 4.4.2 | - Produkt bearbeiten       | Alle Felder editierbar        | 4.4          | ⬜     |
| 4.4.3 | - Verfügbarkeit            | Toggle "Ausverkauft"          | 4.4          | ⬜     |
| 4.4.4 | - Extras/Toppings          | Zusatzoptionen mit Preisen    | 4.4          | ⬜     |
| 4.5   | **Rechnungs-Übersicht**    | Liste aller Rechnungen        | 2.6          | ⬜     |
| 4.5.1 | - Suche & Filter           | Nach Datum, Betrag, Status    | 4.5          | ⬜     |
| 4.5.2 | - PDF Export               | Rechnung als PDF              | 4.5          | ⬜     |
| 4.5.3 | - Tagesabschluss           | Summary Report                | 4.5          | ⬜     |
| 4.6   | **Einstellungen**          | Restaurant-Konfiguration      | 2.7          | ⬜     |
| 4.6.1 | - Öffnungszeiten           | Pro Wochentag                 | 4.6          | ⬜     |
| 4.6.2 | - Liefergebiet             | Konfigurierbar                | 4.6          | ⬜     |
| 4.6.3 | - Zahlungsmethoden         | Aktivieren/Deaktivieren       | 4.6          | ⬜     |
| 4.7   | **Audio-Benachrichtigung** | Sound bei neuer Bestellung    | 4.2          | ⬜     |

**Definition of Done Phase 4:**

- Restaurant kann komplett autonom arbeiten
- Alle CRUD-Operationen funktionieren
- Keine Entwickler-Hilfe für Änderungen nötig

---

### Phase 5: Integration & Qualität

| #   | Aufgabe               | Beschreibung              | Abhängigkeit | Status |
| --- | --------------------- | ------------------------- | ------------ | ------ |
| 5.1 | **Fehlerbehandlung**  | Global Exception Handler  | Phase 2      | ⬜     |
| 5.2 | **Logging**           | Strukturiertes Logging    | Phase 2      | ⬜     |
| 5.3 | **Integration Tests** | Mindestens Happy Path     | Phase 2,3,4  | ⬜     |
| 5.4 | **Real-time Updates** | WebSocket für Live-Orders | 2.3, 4.2     | ⬜     |

**Definition of Done Phase 5:**

- Fehler werden sauber gehandelt
- E2E Test: Kunde bestellt → Restaurant sieht es → Status-Update

---

### Phase 6: Dokumentation & Abgabe

| #   | Aufgabe                    | Beschreibung                    | Abhängigkeit | Status |
| --- | -------------------------- | ------------------------------- | ------------ | ------ |
| 6.1 | **README finalisieren**    | Vollständige Anleitung          | Alle         | ⬜     |
| 6.2 | **Architektur-Diagramm**   | Grafische Darstellung (PNG/SVG) | -            | ✅     |
| 6.3 | **API Dokumentation**      | Swagger/OpenAPI exportiert      | Phase 2      | ⬜     |
| 6.4 | **DOCUMENTATION.md**       | Reflexion & Entscheidungen      | Alle         | 🔄     |
| 6.5 | **Demo-Video/Screenshots** | Funktionsnachweis               | Phase 3,4    | ⬜     |

**Definition of Done Phase 6:**

- Projekt kann von Dritten gestartet werden
- Alle Entscheidungen sind dokumentiert

---

## 🎁 NICE-TO-HAVES (Optional)

Diese Features sind **nicht notwendig** für die Abgabe, verbessern aber die Qualität.

| #   | Feature                    | Aufwand | Priorität |
| --- | -------------------------- | ------- | --------- |
| N1  | Statistiken & Reports      | Mittel  | ⭐⭐⭐    |
| N2  | Mitarbeiter-Verwaltung     | Mittel  | ⭐⭐      |
| N3  | Drucker-Integration        | Hoch    | ⭐⭐      |
| N4  | Apache Kafka für Events    | Hoch    | ⭐        |
| N5  | Offline-Modus (Client)     | Hoch    | ⭐        |
| N6  | Benutzer-Authentifizierung | Mittel  | ⭐⭐      |
| N7  | E-Mail Benachrichtigungen  | Niedrig | ⭐        |
| N8  | Multi-Language Support     | Niedrig | ⭐        |
| N9  | Dark Mode                  | Niedrig | ⭐        |

---

## 📅 Zeitplan (Vorschlag)

```
Woche 1 (04.12.2025)
├── Tag 1: Projektdefinition & Architektur ✅
├── Tag 2-3: Docker + Backend Setup
└── Tag 4-5: Products & Orders API

Woche 2
├── Cart & Payments API
├── Website Grundstruktur
└── Website Produkte & Warenkorb

Woche 3
├── Website Checkout & Zahlung
├── Restaurant Client Setup (Tauri)
└── KDS Order Dashboard

Woche 4
├── Restaurant Client: Produkt-Manager
├── Restaurant Client: Rechnungen
└── Restaurant Client: Einstellungen

Woche 5
├── Integration & Testing
├── Bug Fixes
└── Dokumentation finalisieren

Woche 5
├── Bug Fixes
├── Dokumentation finalisieren
└── Abgabe vorbereiten
```

---

## ✅ Fortschritts-Tracker

### Gesamtfortschritt

```
Phase 1: Infrastruktur       [██████████] 100%
Phase 2: Backend Server      [███████░░░] 70%
Phase 3: Website             [███████░░░] 70%
Phase 4: Restaurant Client   [░░░░░░░░░░] 0%
Phase 5: Integration         [████░░░░░░] 40%
Phase 6: Dokumentation       [██████░░░░] 60%
─────────────────────────────────────────────
GESAMT                       [█████░░░░░] 55%
```

### Erledigte Aufgaben

- [x] Repository erstellt
- [x] README.md mit Architektur
- [x] DOCUMENTATION.md Template
- [x] PROJECT_PLAN.md erstellt
- [x] Copilot Instructions definiert
- [x] 3-Komponenten-Architektur entschieden
- [x] KDS Wireframe erstellt
- [x] Restaurant Client Features definiert
- [x] Docker Compose mit MySQL, Redis, Kafka, Zookeeper
- [x] Eureka Service Discovery
- [x] API Gateway mit Circuit Breaker
- [x] Product Service (CRUD, Categories)
- [x] Cart Service (Redis-basiert)
- [x] Order Service (Status-Workflow, Kafka)
- [x] Payment Service (Mockup)
- [x] Auth Service (JWT, BCrypt)
- [x] GitHub Actions CI/CD Pipeline
- [x] Unit Tests (102 Tests, alle bestanden)
- [x] ARCHITECTURE.md erstellt
- [x] REQUIREMENTS.md erstellt

---

## 🔴 Risiken & Mitigation

### Update 06.12.2025

- Kunden-Website: Checkout, Profil, Adressen (inkl. gespeicherte Adresse) lauffähig.
- Auth-Flow harmonisiert (`/api/customers/profile`, `X-User-ID`).
- E2E-Tests für Kunden (Gast & registriert mit Lieferadresse) ergänzt.
- Offene Lücke: dedizierter Customer-Order-History-Endpoint (derzeit Fallback).

| Risiko            | Wahrscheinlichkeit | Impact  | Mitigation                          |
| ----------------- | ------------------ | ------- | ----------------------------------- |
| Zeitknappheit     | Hoch               | Hoch    | Scope auf Must-Haves reduzieren     |
| Tauri Komplexität | Niedrig            | Niedrig | Leichter als Electron, React UI     |
| Docker-Probleme   | Niedrig            | Hoch    | Lokale Entwicklung als Fallback     |
| Real-time Updates | Mittel             | Mittel  | WebSocket oder Polling als Fallback |

---

## 📞 Definition of Done (Projekt-Abschluss)

Das Projekt gilt als **abgeschlossen**, wenn:

1. ✅ Alle Must-Have Aufgaben in Phase 1-6 erledigt sind
2. ✅ Ein Kunde kann eine komplette Bestellung durchführen (Website)
3. ✅ Restaurant sieht Bestellung live und kann Status ändern (Client)
4. ✅ Restaurant kann Produkte/Preise selbst ändern (Client)
5. ✅ `docker-compose up` startet das gesamte System
6. ✅ README enthält vollständige Setup-Anleitung
7. ✅ DOCUMENTATION.md enthält Reflexion
8. ✅ Code ist auf GitHub gepusht

---

_Erstellt: 04.12.2025_
_Letzte Aktualisierung: 06.12.2025_
