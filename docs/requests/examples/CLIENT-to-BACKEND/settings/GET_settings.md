# GET /api/settings

> Restaurant-Einstellungen abrufen

---

## Request

```http
GET /api/settings HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Example

```bash
curl -X GET "http://localhost:8080/api/settings" \
  -H "Authorization: Bearer restaurant-token-123"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "restaurant": {
      "name": "Pizzeria Bella Italia",
      "address": {
        "street": "Bahnhofstrasse 50",
        "city": "Zürich",
        "postalCode": "8001"
      },
      "phone": "+41 44 123 45 67",
      "email": "info@bellaitalia.ch",
      "logo": "/images/logo.png"
    },
    "openingHours": {
      "monday": { "open": "11:00", "close": "22:00" },
      "tuesday": { "open": "11:00", "close": "22:00" },
      "wednesday": { "open": "11:00", "close": "22:00" },
      "thursday": { "open": "11:00", "close": "22:00" },
      "friday": { "open": "11:00", "close": "23:00" },
      "saturday": { "open": "12:00", "close": "23:00" },
      "sunday": { "open": "12:00", "close": "21:00" }
    },
    "delivery": {
      "enabled": true,
      "fee": 5.0,
      "freeDeliveryThreshold": 50.0,
      "minimumOrder": 20.0,
      "estimatedTime": 45,
      "radius": 5,
      "postalCodes": ["8001", "8002", "8003", "8004", "8005"]
    },
    "pickup": {
      "enabled": true,
      "estimatedTime": 20
    },
    "payment": {
      "methods": {
        "CARD": { "enabled": true, "label": "Kreditkarte" },
        "TWINT": { "enabled": true, "label": "TWINT" },
        "CASH": { "enabled": true, "label": "Barzahlung" }
      },
      "currency": "CHF"
    },
    "notifications": {
      "soundEnabled": true,
      "soundFile": "new-order.mp3",
      "volume": 80
    },
    "categories": [
      { "id": "PIZZA", "name": "Pizzen", "order": 1 },
      { "id": "PASTA", "name": "Pasta", "order": 2 },
      { "id": "SALAD", "name": "Salate", "order": 3 },
      { "id": "DESSERT", "name": "Desserts", "order": 4 },
      { "id": "DRINKS", "name": "Getränke", "order": 5 },
      { "id": "SIDES", "name": "Beilagen", "order": 6 }
    ],
    "updatedAt": "2025-12-04T15:00:00Z"
  }
}
```

---

## Notes

- Alle konfigurierbaren Restaurant-Einstellungen
- Wird beim Start des Clients geladen
- Änderungen über PUT /api/settings
