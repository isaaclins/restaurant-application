# 🧪 E2E Test Plan - Restaurant Application

> Vollständige Definition aller End-to-End Tests mit Cypress

---

## 📋 Übersicht

| Bereich               | Anzahl Tests | Priorität  |
| --------------------- | ------------ | ---------- |
| Authentication        | 8            | 🔴 Hoch    |
| KDS (Kitchen Display) | 15           | 🔴 Hoch    |
| Orders Management     | 12           | 🔴 Hoch    |
| Products & Categories | 10           | 🟡 Mittel  |
| Receipts              | 8            | 🟡 Mittel  |
| Statistics            | 6            | 🟡 Mittel  |
| Settings              | 10           | 🟢 Niedrig |
| Navigation & Layout   | 5            | 🟢 Niedrig |
| **Total**             | **74**       | -          |

---

## 1️⃣ Authentication Tests (`auth.cy.ts`)

### Login Flow

| Test ID  | Beschreibung                                       | Precondition   |
| -------- | -------------------------------------------------- | -------------- |
| AUTH-001 | Login-Seite wird korrekt angezeigt                 | -              |
| AUTH-002 | Login mit gültigen Credentials                     | User existiert |
| AUTH-003 | Login mit ungültigen Credentials zeigt Fehler      | -              |
| AUTH-004 | Login mit leerem Passwort zeigt Validierungsfehler | -              |
| AUTH-005 | Login mit leerem Username zeigt Validierungsfehler | -              |

### Session Management

| Test ID  | Beschreibung                                     | Precondition    |
| -------- | ------------------------------------------------ | --------------- |
| AUTH-006 | Nach Login wird Token gespeichert                | -               |
| AUTH-007 | Logout entfernt Token und redirected zu Login    | User eingeloggt |
| AUTH-008 | Geschützte Routen redirecten zu Login ohne Token | -               |

---

## 2️⃣ KDS (Kitchen Display System) Tests (`kds.cy.ts`)

### Order Display

| Test ID | Beschreibung                                          | Precondition               |
| ------- | ----------------------------------------------------- | -------------------------- |
| KDS-001 | KDS-Seite lädt und zeigt Grid                         | User eingeloggt            |
| KDS-002 | Pickup-Orders werden in Pickup-Grid angezeigt         | Orders existieren          |
| KDS-003 | Delivery-Orders werden in Delivery-Grid angezeigt     | Orders existieren          |
| KDS-004 | Leere Orders werden NICHT angezeigt                   | Order ohne Items existiert |
| KDS-005 | Order-Karte zeigt alle Details (Nummer, Kunde, Items) | Order existiert            |

### Timer & Sorting

| Test ID | Beschreibung                                  | Precondition                  |
| ------- | --------------------------------------------- | ----------------------------- |
| KDS-006 | Timer zeigt korrekte Zeit bis ETA             | Order mit estimatedDelivery   |
| KDS-007 | Überfällige Orders zeigen negative Zeit (rot) | Order mit vergangener ETA     |
| KDS-008 | Sort-Toggle wechselt zwischen Asc/Desc        | Orders existieren             |
| KDS-009 | "Sort by Time" sortiert nach ETA              | Orders mit verschiedenen ETAs |
| KDS-010 | "Sort by Items" sortiert nach Item-Anzahl     | Orders mit Items              |

### Order Actions

| Test ID | Beschreibung                                         | Precondition            |
| ------- | ---------------------------------------------------- | ----------------------- |
| KDS-011 | Klick auf Order öffnet Detail-Modal                  | Order existiert         |
| KDS-012 | "Complete Order" ändert Status und entfernt aus Grid | Order in Modal geöffnet |
| KDS-013 | "Cancel Order" zeigt Bestätigungs-Dialog             | Order in Modal geöffnet |
| KDS-014 | Cancel-Bestätigung setzt Status auf CANCELLED        | Dialog bestätigt        |
| KDS-015 | Cancel-Abbrechen schliesst Dialog ohne Änderung      | Dialog abgebrochen      |

---

## 3️⃣ Orders Management Tests (`orders.cy.ts`)

### Order List

| Test ID | Beschreibung                                        | Precondition              |
| ------- | --------------------------------------------------- | ------------------------- |
| ORD-001 | Orders-Seite zeigt alle Orders in Tabelle           | Orders existieren         |
| ORD-002 | Suche filtert Orders nach Nummer/Kunde              | Orders existieren         |
| ORD-003 | Status-Filter zeigt nur Orders mit gewähltem Status | Orders mit versch. Status |
| ORD-004 | Datum-Filter zeigt nur Orders vom gewählten Tag     | Orders mit versch. Daten  |

### Order Creation

| Test ID | Beschreibung                                    | Precondition           |
| ------- | ----------------------------------------------- | ---------------------- |
| ORD-005 | "New Order" Button öffnet Erstellungs-Modal     | -                      |
| ORD-006 | Order-Typ kann gewählt werden (Pickup/Delivery) | Modal offen            |
| ORD-007 | Kunde kann ausgewählt werden                    | Modal offen            |
| ORD-008 | Items können hinzugefügt werden                 | Products existieren    |
| ORD-009 | Order-Erstellung speichert und zeigt neue Order | Alle Felder ausgefüllt |

### Order Details

| Test ID | Beschreibung                               | Precondition       |
| ------- | ------------------------------------------ | ------------------ |
| ORD-010 | Klick auf Order öffnet Detail-Ansicht      | Order existiert    |
| ORD-011 | Status kann geändert werden                | Order-Detail offen |
| ORD-012 | Order kann gelöscht werden mit Bestätigung | Order-Detail offen |

---

## 4️⃣ Products & Categories Tests (`products.cy.ts`)

### Categories

| Test ID  | Beschreibung                               | Precondition          |
| -------- | ------------------------------------------ | --------------------- |
| PROD-001 | Categories-Tab zeigt alle Kategorien       | Categories existieren |
| PROD-002 | Neue Kategorie kann erstellt werden        | -                     |
| PROD-003 | Kategorie kann bearbeitet werden           | Kategorie existiert   |
| PROD-004 | Kategorie-Aktivierung/Deaktivierung toggle | Kategorie existiert   |

### Products

| Test ID  | Beschreibung                                    | Precondition          |
| -------- | ----------------------------------------------- | --------------------- |
| PROD-005 | Products-Tab zeigt alle Produkte                | Products existieren   |
| PROD-006 | Produkte können nach Kategorie gefiltert werden | Products & Categories |
| PROD-007 | Neues Produkt kann erstellt werden              | Category existiert    |
| PROD-008 | Produkt kann bearbeitet werden                  | Product existiert     |
| PROD-009 | Produkt-Verfügbarkeit kann getoggelt werden     | Product existiert     |
| PROD-010 | Produkt kann gelöscht werden                    | Product existiert     |

---

## 5️⃣ Receipts Tests (`receipts.cy.ts`)

### Receipt List

| Test ID | Beschreibung                                   | Precondition               |
| ------- | ---------------------------------------------- | -------------------------- |
| RCP-001 | Receipts-Seite zeigt alle Receipts             | Receipts existieren        |
| RCP-002 | Suche filtert Receipts nach Nummer/Kunde       | Receipts existieren        |
| RCP-003 | Datum-Filter funktioniert (Today, Week, Month) | Receipts mit versch. Daten |
| RCP-004 | Custom-Datum-Filter funktioniert               | Receipts existieren        |

### Receipt Details & Download

| Test ID | Beschreibung                                 | Precondition             |
| ------- | -------------------------------------------- | ------------------------ |
| RCP-005 | Klick auf Receipt öffnet Detail-Modal        | Receipt existiert        |
| RCP-006 | Detail-Modal zeigt alle Receipt-Infos        | Modal offen              |
| RCP-007 | PDF-Download startet und zeigt Success-Toast | Receipt existiert        |
| RCP-008 | PDF-Download-Fehler zeigt Error-Toast        | Backend nicht erreichbar |

---

## 6️⃣ Statistics Tests (`statistics.cy.ts`)

### Dashboard

| Test ID  | Beschreibung                              | Precondition       |
| -------- | ----------------------------------------- | ------------------ |
| STAT-001 | Statistics-Seite zeigt alle Stat-Cards    | Orders existieren  |
| STAT-002 | "Total Orders" zeigt korrekte Anzahl      | Orders existieren  |
| STAT-003 | "Revenue" zeigt korrekte Summe            | Orders mit Preisen |
| STAT-004 | "Avg Completion Time" zeigt korrekte Zeit | Completed Orders   |

### Charts

| Test ID  | Beschreibung                            | Precondition      |
| -------- | --------------------------------------- | ----------------- |
| STAT-005 | "Orders by Hour" Chart wird angezeigt   | Orders existieren |
| STAT-006 | "Orders by Status" Chart wird angezeigt | Orders mit Status |

---

## 7️⃣ Settings Tests (`settings.cy.ts`)

### General Settings

| Test ID | Beschreibung                            | Precondition    |
| ------- | --------------------------------------- | --------------- |
| SET-001 | Settings-Seite zeigt alle Tabs          | User eingeloggt |
| SET-002 | Restaurant-Name kann geändert werden    | -               |
| SET-003 | Restaurant-Adresse kann geändert werden | -               |

### Email Settings

| Test ID | Beschreibung                                | Precondition      |
| ------- | ------------------------------------------- | ----------------- |
| SET-004 | Email-Settings-Tab zeigt SMTP-Konfiguration | -                 |
| SET-005 | SMTP-Host kann geändert werden              | -                 |
| SET-006 | Test-Email kann gesendet werden             | SMTP konfiguriert |

### Developer Tools

| Test ID | Beschreibung                                            | Precondition   |
| ------- | ------------------------------------------------------- | -------------- |
| SET-007 | Developer-Tab zeigt "Populate Demo Data" Button         | -              |
| SET-008 | Checkboxen für Categories/Products/Orders funktionieren | -              |
| SET-009 | "Populate Demo Data" erstellt Demo-Daten                | Backend läuft  |
| SET-010 | Progress-Bar zeigt Fortschritt an                       | Populate läuft |

---

## 8️⃣ Navigation & Layout Tests (`navigation.cy.ts`)

### Sidebar Navigation

| Test ID | Beschreibung                                | Precondition         |
| ------- | ------------------------------------------- | -------------------- |
| NAV-001 | Sidebar zeigt alle Menüpunkte               | User eingeloggt      |
| NAV-002 | Klick auf "KDS" navigiert zu /kds           | User eingeloggt      |
| NAV-003 | Klick auf "Orders" navigiert zu /orders     | User eingeloggt      |
| NAV-004 | Klick auf "Products" navigiert zu /products | User eingeloggt      |
| NAV-005 | Aktiver Menüpunkt ist hervorgehoben         | User auf einer Seite |

---

## 🔧 Test-Infrastruktur

### Fixtures (Test-Daten)

```
cypress/fixtures/
├── users.json          # Test-User für Login
├── orders.json         # Sample Orders
├── products.json       # Sample Products
├── categories.json     # Sample Categories
├── receipts.json       # Sample Receipts
└── settings.json       # Sample Settings
```

### Custom Commands

```typescript
// cypress/support/commands.ts
Cypress.Commands.add('login', (username, password) => {...})
Cypress.Commands.add('createOrder', (orderData) => {...})
Cypress.Commands.add('createProduct', (productData) => {...})
Cypress.Commands.add('populateDemoData', () => {...})
Cypress.Commands.add('clearAllData', () => {...})
Cypress.Commands.add('waitForApi', (alias) => {...})
```

### API Interception

```typescript
// Intercept Backend-Calls
cy.intercept("GET", "/api/orders").as("getOrders");
cy.intercept("POST", "/api/orders").as("createOrder");
cy.intercept("GET", "/api/products").as("getProducts");
cy.intercept("GET", "/api/receipts").as("getReceipts");
cy.intercept("GET", "/api/receipts/*/pdf").as("downloadPdf");
```

---

## 📁 Dateistruktur

```
cypress/
├── e2e/
│   ├── auth.cy.ts           # Authentication Tests
│   ├── kds.cy.ts            # KDS Tests
│   ├── orders.cy.ts         # Orders Tests
│   ├── products.cy.ts       # Products & Categories Tests
│   ├── receipts.cy.ts       # Receipts Tests
│   ├── statistics.cy.ts     # Statistics Tests
│   ├── settings.cy.ts       # Settings Tests
│   └── navigation.cy.ts     # Navigation Tests
├── fixtures/
│   ├── users.json
│   ├── orders.json
│   ├── products.json
│   ├── categories.json
│   └── receipts.json
├── support/
│   ├── commands.ts          # Custom Commands
│   ├── e2e.ts               # E2E Support
│   └── index.d.ts           # TypeScript Definitions
└── E2E_TEST_PLAN.md         # Diese Datei
```

---

## 🚀 Ausführung

```bash
# Interaktiver Modus (mit Browser)
npm run cypress:open

# Headless (CI/CD)
npm run cypress:run

# Nur bestimmte Tests
npm run cypress:run -- --spec "cypress/e2e/kds.cy.ts"

# Mit spezifischem Browser
npm run cypress:run -- --browser chrome
```

---

## ✅ Akzeptanzkriterien

- [ ] Alle 74 Tests sind implementiert
- [ ] Tests laufen in < 5 Minuten
- [ ] Tests sind unabhängig voneinander
- [ ] Tests räumen nach sich auf (Cleanup)
- [ ] Tests funktionieren headless (CI/CD)
- [ ] Code Coverage > 80%

---

_Erstellt: 06.12.2025_
