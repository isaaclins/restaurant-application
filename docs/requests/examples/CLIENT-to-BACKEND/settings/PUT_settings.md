# PUT /api/settings

> Restaurant-Einstellungen ändern

---

## Request

```http
PUT /api/settings HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Request Body (Partial Update)

```json
{
  "openingHours": {
    "monday": { "open": "11:30", "close": "21:30" }
  },
  "delivery": {
    "fee": 6.00,
    "minimumOrder": 25.00
  }
}
```

### Full Settings Object

```json
{
  "restaurant": {
    "name": "Pizzeria Bella Italia",
    "address": {
      "street": "Bahnhofstrasse 50",
      "city": "Zürich",
      "postalCode": "8001"
    },
    "phone": "+41 44 123 45 67",
    "email": "info@bellaitalia.ch"
  },
  "openingHours": {
    "monday": { "open": "11:00", "close": "22:00" },
    "tuesday": { "open": "11:00", "close": "22:00" },
    "wednesday": { "open": "11:00", "close": "22:00" },
    "thursday": { "open": "11:00", "close": "22:00" },
    "friday": { "open": "11:00", "close": "23:00" },
    "saturday": { "open": "12:00", "close": "23:00" },
    "sunday": { "open": "12:00", "close": "21:00", "closed": false }
  },
  "delivery": {
    "enabled": true,
    "fee": 5.00,
    "freeDeliveryThreshold": 50.00,
    "minimumOrder": 20.00,
    "estimatedTime": 45,
    "postalCodes": ["8001", "8002", "8003", "8004", "8005"]
  },
  "pickup": {
    "enabled": true,
    "estimatedTime": 20
  },
  "payment": {
    "methods": {
      "CARD": { "enabled": true },
      "TWINT": { "enabled": true },
      "CASH": { "enabled": false }
    }
  },
  "notifications": {
    "soundEnabled": true,
    "volume": 80
  }
}
```

### Example - Öffnungszeiten ändern

```bash
curl -X PUT "http://localhost:8080/api/settings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{
    "openingHours": {
      "sunday": { "closed": true }
    }
  }'
```

### Example - Liefergebiet erweitern

```bash
curl -X PUT "http://localhost:8080/api/settings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{
    "delivery": {
      "postalCodes": ["8001", "8002", "8003", "8004", "8005", "8006", "8008"]
    }
  }'
```

### Example - Zahlungsmethode deaktivieren

```bash
curl -X PUT "http://localhost:8080/api/settings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{
    "payment": {
      "methods": {
        "CASH": { "enabled": false }
      }
    }
  }'
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "openingHours": {
      "monday": { "open": "11:30", "close": "21:30" },
      "tuesday": { "open": "11:00", "close": "22:00" }
    },
    "delivery": {
      "fee": 6.00,
      "minimumOrder": 25.00
    },
    "updatedAt": "2025-12-04T21:45:00Z"
  },
  "message": "Settings updated successfully"
}
```

### Error (400 Bad Request) - Ungültige Zeit

```json
{
  "success": false,
  "error": {
    "code": "INVALID_TIME_FORMAT",
    "message": "Invalid time format for monday.open. Expected HH:MM"
  }
}
```

### Error (400 Bad Request) - Schliesszeit vor Öffnungszeit

```json
{
  "success": false,
  "error": {
    "code": "INVALID_HOURS",
    "message": "Closing time (10:00) cannot be before opening time (11:00)"
  }
}
```

---

## Notes

- Partial Updates möglich - nur übergebene Felder werden geändert
- Deep Merge: Verschachtelte Objekte werden zusammengeführt
- Änderungen sind sofort aktiv
- Website und Client laden Settings bei Änderung neu
- `closed: true` für Tag = Restaurant geschlossen an diesem Tag
