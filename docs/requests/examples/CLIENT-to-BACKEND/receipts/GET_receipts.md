# GET /api/receipts

> Alle Rechnungen abrufen

---

## Request

```http
GET /api/receipts HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `from` | date | Ab Datum (YYYY-MM-DD) |
| `to` | date | Bis Datum (YYYY-MM-DD) |
| `status` | string | Filter: `PAID`, `PENDING`, `REFUNDED` |
| `minAmount` | decimal | Mindestbetrag |
| `maxAmount` | decimal | Maximalbetrag |
| `page` | integer | Seitennummer (default: 1) |
| `limit` | integer | Einträge pro Seite (default: 50) |

### Example - Heutige Rechnungen

```bash
curl -X GET "http://localhost:8080/api/receipts?from=2025-12-04&to=2025-12-04" \
  -H "Authorization: Bearer restaurant-token-123"
```

### Example - Letzte Woche, nur bezahlt

```bash
curl -X GET "http://localhost:8080/api/receipts?from=2025-11-27&to=2025-12-04&status=PAID" \
  -H "Authorization: Bearer restaurant-token-123"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "receipts": [
      {
        "id": 5001,
        "orderId": 1001,
        "ticketNumber": "A-042",
        "type": "DELIVERY",
        "customer": {
          "name": "Max Mustermann",
          "phone": "+41 79 123 45 67"
        },
        "items": [
          {
            "name": "Margherita (M)",
            "quantity": 2,
            "unitPrice": 18.50,
            "extras": "Extra Käse (+2.50)",
            "total": 42.00
          }
        ],
        "subtotal": 42.00,
        "deliveryFee": 5.00,
        "total": 47.00,
        "paymentMethod": "CARD",
        "paymentStatus": "PAID",
        "paidAt": "2025-12-04T20:46:00Z",
        "createdAt": "2025-12-04T20:45:00Z"
      },
      {
        "id": 5002,
        "orderId": 1002,
        "ticketNumber": "B-015",
        "type": "PICKUP",
        "customer": {
          "name": "Anna Beispiel"
        },
        "items": [
          {
            "name": "Quattro Formaggi (L)",
            "quantity": 1,
            "unitPrice": 26.50,
            "extras": null,
            "total": 26.50
          }
        ],
        "subtotal": 26.50,
        "deliveryFee": 0,
        "total": 26.50,
        "paymentMethod": "CASH",
        "paymentStatus": "PAID",
        "paidAt": "2025-12-04T21:10:00Z",
        "createdAt": "2025-12-04T20:55:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 50,
      "totalPages": 1,
      "totalItems": 2
    },
    "summary": {
      "totalRevenue": 73.50,
      "totalOrders": 2,
      "averageOrder": 36.75,
      "paymentMethods": {
        "CARD": 47.00,
        "CASH": 26.50,
        "TWINT": 0
      }
    }
  }
}
```

---

## Notes

- `summary` gibt aggregierte Daten für den gewählten Zeitraum
- Standardmässig werden die letzten 50 Rechnungen zurückgegeben
- Für Tagesabschluss: `from` und `to` auf gleiches Datum setzen
