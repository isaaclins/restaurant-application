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

#### ❌ Misserfolge / Herausforderungen

- Initiale Architektur war zu komplex für den Use-Case
- Musste Scope anpassen (kein Mobile App, sondern Desktop KDS)

#### 💡 Ideen

- Template soll generic sein für verschiedene Restaurant-Typen
- Inspiration von bekannten Pizzeria-Bestellsystemen (Dieci)
- **KDS mit Farbcodes** für Bestellstatus (Grün->Gelb->Rot)
- **Volle Restaurant-Autonomie**: Keine Entwickler-Hilfe für Preisänderungen etc.

#### 🔍 Erkenntnisse

- Microservices-Architektur bietet gute Skalierbarkeit
- Klare Service-Grenzen sind wichtig für Wartbarkeit
- **KDS ist Kernkomponente** für Restaurant-Workflow
- Restaurant braucht volle Kontrolle über Produkte, Preise, Einstellungen

#### 📝 Notizen

- Projektabgabe: Mehrere Wochen mit wöchentlichen Updates
- Fokus auf funktionierende Demo, nicht 100% Production-Ready
- Electron für Desktop-App (gleicher Stack wie Website)

---

### Tag 2 – [DATUM]

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

| #   | Meilenstein                         | Zieldatum  | Status | Notizen                     |
| --- | ----------------------------------- | ---------- | ------ | --------------------------- |
| 1   | Projektdefinition & Architektur     | 04.12.2025 | ✅     | README, Plan, Docs erstellt |
| 2   | KDS Wireframe & Feature-Definition  | 04.12.2025 | ✅     | 3-Spalten Layout definiert  |
| 3   | Infrastruktur (Docker, Backend)     | TBD        | ⬜     |                             |
| 4   | Backend API (Orders, Products)      | TBD        | ⬜     |                             |
| 5   | Website (Kunden-Portal)             | TBD        | ⬜     |                             |
| 6   | Restaurant Client (KDS Desktop App) | TBD        | ⬜     |                             |
| 7   | Integration & Testing               | TBD        | ⬜     |                             |
| 8   | Endabgabe                           | TBD        | ⬜     |                             |
| 4   | Warenkorb-Service                   | TBD        | ⬜     |                             |
| 5   | Bestell-Service                     | TBD        | ⬜     |                             |
| 6   | Zahlungs-Service (Mockup)           | TBD        | ⬜     |                             |
| 7   | Frontend (React)                    | TBD        | ⬜     |                             |
| 8   | Kafka Integration                   | TBD        | ⬜     |                             |
| 9   | Circuit Breaker                     | TBD        | ⬜     |                             |
| 10  | Endabgabe                           | TBD        | ⬜     |                             |

---

## 🛠️ Technische Entscheidungen

### Entscheidung 1: Datenbank-Strategie

**Datum**: 04.12.2025

**Kontext**: Welche Datenbanken für welche Services?

**Entscheidung**:

- PostgreSQL für Produktkatalog und Bestellungen (strukturierte Daten)
- Redis für Warenkorb (schnelle Zugriffe, TTL für Session-Ablauf)
- H2 In-Memory für Zahlungs-Mockup

**Begründung**:

- Service-Unabhängigkeit durch separate Datenbanken
- Redis optimal für temporäre Session-Daten
- PostgreSQL bietet ACID-Compliance für kritische Geschäftsdaten

**Alternativen erwägt**:

- MongoDB für alle Services (abgelehnt: weniger Erfahrung im Team)
- Shared Database (abgelehnt: verletzt Microservices-Prinzipien)

---

### Entscheidung 2: 3-Komponenten-Architektur

**Datum**: 04.12.2025

**Kontext**: Ursprünglich war eine reine Microservices-Architektur mit vielen einzelnen Services geplant. Nach Diskussion wurde klar, dass wir zwei unterschiedliche Clients brauchen.

**Entscheidung**:

```
┌─────────────────────┐          ┌─────────────────────┐
│      WEBSITE        │          │   RESTAURANT CLIENT │
│  (Kunden-Portal)    │          │   (KDS Desktop App) │
│     - React/Web     │          │   - Electron        │
│     - Bestellen     │          │   - Order Management│
│     - Warenkorb     │          │   - Produkt-Editor  │
│     - Bezahlen      │          │   - Rechnungen      │
└──────────┬──────────┘          └──────────┬──────────┘
           │                                │
           └────────────┬───────────────────┘
                        │
                        ▼
              ┌─────────────────┐
              │  BACKEND SERVER │
              │  (Spring Boot)  │
              └─────────────────┘
```

**Begründung**:

- Website für Kunden (Bestellungen)
- Desktop App für Restaurant (Küchen-Display, Verwaltung)
- Ein Backend bedient beide Clients
- Restaurant hat volle Autonomie (keine Dev-Hilfe für Änderungen)

**Alternativen erwägt**:

- Mobile App für Restaurant (abgelehnt: Desktop besser für Küche)
- Separate Admin-Website (abgelehnt: Desktop-App bietet bessere UX)

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

### Entscheidung 4: [TITEL]

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
