# 📡 API Request Documentation

> Alle API Endpoints mit Request/Response Beispielen

---

## 📁 Struktur

```
requests/
├── README.md                          # Diese Datei
└── examples/
    ├── WEBSITE-to-BACKEND/            # Kunden-Portal Requests
    │   ├── products/                  # Produkt-bezogen
    │   ├── cart/                      # Warenkorb
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

### Website → Backend (Kunden)

| Methode | Endpoint                           | Beschreibung            |
| ------- | ---------------------------------- | ----------------------- |
| GET     | `/products`                        | Alle Produkte abrufen   |
| GET     | `/products/{id}`                   | Einzelnes Produkt       |
| GET     | `/products/category/{category}`    | Produkte nach Kategorie |
| POST    | `/cart`                            | Warenkorb erstellen     |
| GET     | `/cart/{sessionId}`                | Warenkorb abrufen       |
| POST    | `/cart/{sessionId}/items`          | Item hinzufügen         |
| DELETE  | `/cart/{sessionId}/items/{itemId}` | Item entfernen          |
| POST    | `/orders`                          | Bestellung erstellen    |
| GET     | `/orders/{id}`                     | Bestellstatus abrufen   |
| POST    | `/payments`                        | Zahlung durchführen     |

### Client → Backend (Restaurant)

| Methode | Endpoint                      | Beschreibung              |
| ------- | ----------------------------- | ------------------------- |
| GET     | `/orders`                     | Alle aktiven Bestellungen |
| GET     | `/orders/history`             | Bestellhistorie           |
| PUT     | `/orders/{id}/status`         | Status ändern             |
| POST    | `/products`                   | Produkt erstellen         |
| PUT     | `/products/{id}`              | Produkt bearbeiten        |
| DELETE  | `/products/{id}`              | Produkt löschen           |
| PUT     | `/products/{id}/availability` | Verfügbarkeit togglen     |
| GET     | `/receipts`                   | Alle Rechnungen           |
| GET     | `/receipts/{id}/pdf`          | Rechnung als PDF          |
| GET     | `/settings`                   | Einstellungen abrufen     |
| PUT     | `/settings`                   | Einstellungen ändern      |

---

_Letzte Aktualisierung: 04.12.2025_
