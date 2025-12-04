# GET /api/receipts/daily-report

> Tagesabschluss-Report generieren

---

## Request

```http
GET /api/receipts/daily-report HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Query Parameters

| Parameter | Type | Required | Description                        |
| --------- | ---- | -------- | ---------------------------------- |
| `date`    | date | ❌       | Datum (YYYY-MM-DD), default: heute |

### Example - Heute

```bash
curl -X GET "http://localhost:8080/api/receipts/daily-report" \
  -H "Authorization: Bearer restaurant-token-123"
```

### Example - Bestimmtes Datum

```bash
curl -X GET "http://localhost:8080/api/receipts/daily-report?date=2025-12-03" \
  -H "Authorization: Bearer restaurant-token-123"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "date": "2025-12-04",
    "summary": {
      "totalOrders": 45,
      "totalRevenue": 1847.5,
      "averageOrderValue": 41.06,
      "deliveryOrders": 28,
      "pickupOrders": 17,
      "walkInOrders": 8
    },
    "revenue": {
      "subtotal": 1697.5,
      "deliveryFees": 150.0,
      "total": 1847.5
    },
    "paymentBreakdown": {
      "CARD": {
        "count": 25,
        "amount": 1050.0
      },
      "TWINT": {
        "count": 12,
        "amount": 497.5
      },
      "CASH": {
        "count": 8,
        "amount": 300.0
      }
    },
    "hourlyBreakdown": [
      { "hour": "11:00", "orders": 3, "revenue": 125.0 },
      { "hour": "12:00", "orders": 8, "revenue": 340.0 },
      { "hour": "13:00", "orders": 5, "revenue": 210.0 },
      { "hour": "18:00", "orders": 10, "revenue": 420.0 },
      { "hour": "19:00", "orders": 12, "revenue": 495.0 },
      { "hour": "20:00", "orders": 7, "revenue": 257.5 }
    ],
    "topProducts": [
      { "name": "Margherita", "quantity": 32, "revenue": 544.0 },
      { "name": "Quattro Formaggi", "quantity": 18, "revenue": 423.0 },
      { "name": "Coca Cola", "quantity": 45, "revenue": 202.5 },
      { "name": "Pizza Diavola", "quantity": 12, "revenue": 246.0 },
      { "name": "Tiramisu", "quantity": 15, "revenue": 127.5 }
    ],
    "cancelledOrders": {
      "count": 2,
      "lostRevenue": 65.0,
      "reasons": ["Kunde storniert", "Produkt ausverkauft"]
    },
    "generatedAt": "2025-12-04T22:00:00Z"
  }
}
```

---

## Notes

- Zeigt komplette Tagesübersicht
- `hourlyBreakdown` hilft bei Personalplanung
- `topProducts` zeigt Bestseller
- Nützlich für Buchhaltung und Analyse
- Kann auch als PDF exportiert werden (separater Endpoint)
