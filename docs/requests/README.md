# 📡 API Request Documentation

> Alle API Endpoints mit Request/Response Beispielen

---

## 📁 Struktur

```
requests/
├── README.md                          # Diese Datei
└── examples/
    ├── auth/                          # Authentifizierung
    │   ├── POST_register.md           # Kunden-Registrierung
    │   ├── POST_login.md              # Restaurant-Login
    │   ├── POST_login_customer.md     # Kunden-Login
    │   ├── POST_refresh.md
    │   ├── POST_logout.md
    │   └── PUT_password.md            # Passwort ändern
    │
    ├── WEBSITE-to-BACKEND/            # Kunden-Portal Requests
    │   ├── products/                  # Produkt-bezogen
    │   ├── cart/                      # Warenkorb
    │   ├── customers/                 # Kundenprofil & Adressen
    │   ├── orders/                    # Bestellungen
    │   └── payments/                  # Zahlungen
    │
    └── CLIENT-to-BACKEND/             # Restaurant KDS Requests
        ├── orders/                    # Bestellmanagement
        ├── products/                  # Produktverwaltung
        ├── receipts/                  # Rechnungen
        └── settings/                  # Einstellungen
```

---

## 🔗 Base URL

```
Development: http://localhost:8080/api
Production:  https://api.restaurant.com/api
```

---

## 📋 Endpoint Übersicht

### 🔐 Authentifizierung

| Methode | Endpoint               | Beschreibung                 | Auth |
| ------- | ---------------------- | ---------------------------- | ---- |
| POST    | `/auth/register`       | Kunden-Registrierung         | ❌   |
| POST    | `/auth/login/customer` | Kunden-Login (Website)       | ❌   |
| POST    | `/auth/login`          | Restaurant-Login (Client)    | ❌   |
| POST    | `/auth/refresh`        | Token erneuern               | ❌   |
| POST    | `/auth/logout`         | Logout                       | ✅   |
| PUT     | `/auth/password`       | Passwort ändern (Restaurant) | ✅   |

### Website → Backend (Kunden)

| Methode | Endpoint                           | Beschreibung            | Auth |
| ------- | ---------------------------------- | ----------------------- | ---- |
| GET     | `/products`                        | Alle Produkte abrufen   | ❌   |
| GET     | `/products/{id}`                   | Einzelnes Produkt       | ❌   |
| GET     | `/products/category/{category}`    | Produkte nach Kategorie | ❌   |
| POST    | `/cart`                            | Warenkorb erstellen     | ❌   |
| GET     | `/cart/{sessionId}`                | Warenkorb abrufen       | ❌   |
| POST    | `/cart/{sessionId}/items`          | Item hinzufügen         | ❌   |
| DELETE  | `/cart/{sessionId}/items/{itemId}` | Item entfernen          | ❌   |
| GET     | `/customers/me`                    | Eigenes Profil abrufen  | ✅   |
| PUT     | `/customers/me`                    | Profil aktualisieren    | ✅   |
| PUT     | `/customers/me/password`           | Passwort ändern         | ✅   |
| POST    | `/customers/me/addresses`          | Adresse hinzufügen      | ✅   |
| GET     | `/customers/me/orders`             | Eigene Bestellhistorie  | ✅   |
| POST    | `/orders`                          | Bestellung erstellen    | ❌\* |
| GET     | `/orders/{id}`                     | Bestellstatus abrufen   | ❌   |
| POST    | `/payments`                        | Zahlung durchführen     | ❌   |

\*Bestellung auch als Gast möglich, aber eingeloggte Kunden bekommen Bestellung im Profil gespeichert

### Client → Backend (Restaurant) 🔒

| Methode | Endpoint                      | Beschreibung              | Rolle        |
| ------- | ----------------------------- | ------------------------- | ------------ |
| GET     | `/orders`                     | Alle aktiven Bestellungen | STAFF, ADMIN |
| GET     | `/orders/history`             | Bestellhistorie           | STAFF, ADMIN |
| PUT     | `/orders/{id}/status`         | Status ändern             | STAFF, ADMIN |
| POST    | `/orders`                     | Manuelle Bestellung       | STAFF, ADMIN |
| POST    | `/products`                   | Produkt erstellen         | ADMIN        |
| PUT     | `/products/{id}`              | Produkt bearbeiten        | ADMIN        |
| DELETE  | `/products/{id}`              | Produkt löschen           | ADMIN        |
| PUT     | `/products/{id}/availability` | Verfügbarkeit togglen     | ADMIN        |
| GET     | `/receipts`                   | Alle Rechnungen           | ADMIN        |
| GET     | `/receipts/{id}/pdf`          | Rechnung als PDF          | ADMIN        |
| GET     | `/receipts/daily-report`      | Tagesbericht              | ADMIN        |
| GET     | `/settings`                   | Einstellungen abrufen     | ADMIN        |
| PUT     | `/settings`                   | Einstellungen ändern      | ADMIN        |

---

## 🔑 Authentifizierung

### Öffentliche Endpoints (Website)

Keine Authentifizierung nötig – Kunden können ohne Login bestellen.

### Geschützte Endpoints (Restaurant Client)

Erfordern JWT Token im Header:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### Rollen

| Rolle              | Beschreibung                                      |
| ------------------ | ------------------------------------------------- |
| `RESTAURANT_ADMIN` | Vollzugriff: Orders, Products, Settings, Receipts |
| `RESTAURANT_STAFF` | Orders lesen & Status ändern                      |

---

_Letzte Aktualisierung: 04.12.2025_
