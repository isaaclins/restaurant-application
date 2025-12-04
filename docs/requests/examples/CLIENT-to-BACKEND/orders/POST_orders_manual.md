# POST /api/orders (Manual Order)

> Manuelle Bestellung erstellen (Walk-in Kunden)

---

## Request

```http
POST /api/orders HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Request Body

```json
{
  "type": "PICKUP",
  "source": "WALK_IN",
  "customer": {
    "name": "Walk-in Kunde",
    "phone": null
  },
  "items": [
    {
      "productId": 1,
      "quantity": 1,
      "size": "M",
      "extras": [1],
      "notes": "Ohne Oliven"
    },
    {
      "productId": 5,
      "quantity": 2,
      "size": null,
      "extras": [],
      "notes": null
    }
  ],
  "paymentMethod": "CASH",
  "notes": "Wartet am Tresen"
}
```

### Body Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | string | ✅ | `PICKUP` (Walk-in ist immer Abholung) |
| `source` | string | ✅ | `WALK_IN` (unterscheidet von Online) |
| `customer.name` | string | ❌ | Optional für Walk-in |
| `customer.phone` | string | ❌ | Optional für Walk-in |
| `items` | array | ✅ | Bestellte Produkte |
| `paymentMethod` | string | ✅ | `CASH`, `CARD`, `TWINT` |
| `notes` | string | ❌ | Interne Notizen |

### Example

```bash
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{
    "type": "PICKUP",
    "source": "WALK_IN",
    "items": [
      {"productId": 1, "quantity": 1, "size": "M"}
    ],
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
    "id": 1003,
    "ticketNumber": "W-007",
    "type": "PICKUP",
    "source": "WALK_IN",
    "status": "CONFIRMED",
    "customer": {
      "name": "Walk-in Kunde",
      "phone": null
    },
    "items": [
      {
        "productId": 1,
        "productName": "Margherita",
        "quantity": 1,
        "size": "M",
        "unitPrice": 18.50,
        "extras": [
          {
            "name": "Extra Käse",
            "price": 2.50
          }
        ],
        "notes": "Ohne Oliven",
        "itemTotal": 21.00
      },
      {
        "productId": 5,
        "productName": "Coca Cola",
        "quantity": 2,
        "size": null,
        "unitPrice": 4.50,
        "extras": [],
        "notes": null,
        "itemTotal": 9.00
      }
    ],
    "subtotal": 30.00,
    "total": 30.00,
    "paymentMethod": "CASH",
    "paymentStatus": "PENDING",
    "notes": "Wartet am Tresen",
    "createdAt": "2025-12-04T21:00:00Z"
  },
  "message": "Walk-in order created"
}
```

### Error (400 Bad Request) - Produkt nicht verfügbar

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_UNAVAILABLE",
    "message": "Product 'Tiramisu' is currently not available"
  }
}
```

---

## Notes

- Walk-in Bestellungen starten direkt als `CONFIRMED` (kein Warten auf Online-Zahlung)
- `ticketNumber` beginnt mit "W-" für Walk-in
- Keine Lieferadresse erforderlich
- Kundendaten sind optional
- `source: WALK_IN` unterscheidet von Online-Bestellungen (`source: WEBSITE`)
