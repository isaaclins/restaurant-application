# GET /api/orders/{id}

> Bestellstatus für Kunde abrufen

---

## Request

```http
GET /api/orders/1001 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | ✅ | Bestell-ID |

### Example

```bash
curl -X GET "http://localhost:8080/api/orders/1001"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 1001,
    "ticketNumber": "A-042",
    "type": "DELIVERY",
    "status": "PREPARING",
    "statusHistory": [
      {
        "status": "NEW",
        "timestamp": "2025-12-04T20:45:00Z"
      },
      {
        "status": "CONFIRMED",
        "timestamp": "2025-12-04T20:46:30Z"
      },
      {
        "status": "PREPARING",
        "timestamp": "2025-12-04T20:50:00Z"
      }
    ],
    "items": [
      {
        "productName": "Margherita",
        "quantity": 2,
        "size": "M"
      }
    ],
    "total": 47.00,
    "paymentMethod": "CARD",
    "paymentStatus": "PAID",
    "estimatedTime": "2025-12-04T21:15:00Z",
    "createdAt": "2025-12-04T20:45:00Z"
  }
}
```

### Order Status Flow

```
NEW → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED
                                   └→ PICKED_UP (bei Abholung)
     └→ CANCELLED (jederzeit vor READY)
```

| Status | Description |
|--------|-------------|
| `NEW` | Bestellung eingegangen, wartet auf Bestätigung |
| `CONFIRMED` | Restaurant hat Bestellung bestätigt |
| `PREPARING` | Küche bereitet zu |
| `READY` | Bestellung fertig |
| `OUT_FOR_DELIVERY` | Fahrer unterwegs (nur Lieferung) |
| `DELIVERED` | Geliefert |
| `PICKED_UP` | Abgeholt (nur Abholung) |
| `CANCELLED` | Storniert |

### Error (404 Not Found)

```json
{
  "success": false,
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "Order with ID 9999 not found"
  }
}
```

---

## Notes

- Kunden sehen nur öffentliche Infos (keine Adresse anderer Kunden)
- `statusHistory` zeigt den kompletten Verlauf
- `estimatedTime` wird bei Verzögerungen aktualisiert
