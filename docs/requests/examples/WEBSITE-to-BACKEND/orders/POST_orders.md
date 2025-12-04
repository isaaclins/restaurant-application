# POST /api/orders

> Neue Bestellung erstellen

---

## Request

```http
POST /api/orders HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
```

### Request Body

```json
{
  "sessionId": "abc123-session-id",
  "type": "DELIVERY",
  "customer": {
    "name": "Max Mustermann",
    "phone": "+41 79 123 45 67",
    "email": "max@example.com"
  },
  "deliveryAddress": {
    "street": "Bahnhofstrasse 10",
    "city": "Zürich",
    "postalCode": "8001",
    "notes": "3. Stock, Klingel Müller"
  },
  "paymentMethod": "CARD",
  "notes": "Bitte schnell liefern"
}
```

### Body Parameters

| Field             | Type   | Required | Description                        |
| ----------------- | ------ | -------- | ---------------------------------- |
| `sessionId`       | string | ✅       | Warenkorb Session-ID               |
| `type`            | string | ✅       | `DELIVERY` oder `PICKUP`           |
| `customer.name`   | string | ✅       | Kundenname                         |
| `customer.phone`  | string | ✅       | Telefonnummer                      |
| `customer.email`  | string | ❌       | E-Mail Adresse                     |
| `deliveryAddress` | object | ✅\*     | Lieferadresse (\*nur bei DELIVERY) |
| `paymentMethod`   | string | ✅       | `CARD`, `TWINT`, `CASH`            |
| `notes`           | string | ❌       | Allgemeine Notizen                 |

### Example

```bash
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "abc123-session-id",
    "type": "PICKUP",
    "customer": {
      "name": "Anna Beispiel",
      "phone": "+41 79 999 88 77"
    },
    "paymentMethod": "CASH"
  }'
```

---

## Response

### Success (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 1001,
    "ticketNumber": "A-042",
    "type": "DELIVERY",
    "status": "NEW",
    "customer": {
      "name": "Max Mustermann",
      "phone": "+41 79 123 45 67",
      "email": "max@example.com"
    },
    "deliveryAddress": {
      "street": "Bahnhofstrasse 10",
      "city": "Zürich",
      "postalCode": "8001",
      "notes": "3. Stock, Klingel Müller"
    },
    "items": [
      {
        "productId": 1,
        "productName": "Margherita",
        "quantity": 2,
        "size": "M",
        "unitPrice": 18.5,
        "extras": [
          {
            "name": "Extra Käse",
            "price": 2.5
          }
        ],
        "itemTotal": 42.0
      }
    ],
    "subtotal": 42.0,
    "deliveryFee": 5.0,
    "total": 47.0,
    "paymentMethod": "CARD",
    "paymentStatus": "PENDING",
    "notes": "Bitte schnell liefern",
    "estimatedTime": "2025-12-04T21:15:00Z",
    "createdAt": "2025-12-04T20:45:00Z"
  }
}
```

### Error (400 Bad Request) - Leerer Warenkorb

```json
{
  "success": false,
  "error": {
    "code": "EMPTY_CART",
    "message": "Cannot create order with empty cart"
  }
}
```

### Error (400 Bad Request) - Mindestbestellwert

```json
{
  "success": false,
  "error": {
    "code": "MINIMUM_ORDER_VALUE",
    "message": "Minimum order value is CHF 20.00. Current: CHF 15.00"
  }
}
```

### Error (400 Bad Request) - Ausserhalb Liefergebiet

```json
{
  "success": false,
  "error": {
    "code": "OUTSIDE_DELIVERY_AREA",
    "message": "Postal code 9999 is outside our delivery area"
  }
}
```

### Error (400 Bad Request) - Restaurant geschlossen

```json
{
  "success": false,
  "error": {
    "code": "RESTAURANT_CLOSED",
    "message": "Restaurant is currently closed. Opening hours: 11:00 - 22:00"
  }
}
```

---

## Notes

- `ticketNumber` ist eine kurze, lesbare Nummer für die Küche (z.B. "A-042")
- `status` startet immer als `NEW`
- `estimatedTime` wird basierend auf aktueller Auslastung berechnet
- Bei `PICKUP` ist `deliveryAddress` nicht erforderlich und `deliveryFee` ist 0
- Warenkorb wird nach erfolgreicher Bestellung geleert
