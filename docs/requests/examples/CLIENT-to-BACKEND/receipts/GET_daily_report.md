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

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `date` | date | ❌ | Datum (YYYY-MM-DD), default: heute |

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
      "totalRevenue": 1847.50,
      "averageOrderValue": 41.06,
      "deliveryOrders": 28,
      "pickupOrders": 17,
      "walkInOrders": 8
    },
    "revenue": {
      "subtotal": 1697.50,
      "deliveryFees": 150.00,
      "total": 1847.50
    },
    "paymentBreakdown": {
      "CARD": {
        "count": 25,
        "amount": 1050.00
      },
      "TWINT": {
        "count": 12,
        "amount": 497.50
      },
      "CASH": {
        "count": 8,
        "amount": 300.00
      }
    },
    "hourlyBreakdown": [
      { "hour": "11:00", "orders": 3, "revenue": 125.00 },
      { "hour": "12:00", "orders": 8, "revenue": 340.00 },
      { "hour": "13:00", "orders": 5, "revenue": 210.00 },
      { "hour": "18:00", "orders": 10, "revenue": 420.00 },
      { "hour": "19:00", "orders": 12, "revenue": 495.00 },
      { "hour": "20:00", "orders": 7, "revenue": 257.50 }
    ],
    "topProducts": [
      { "name": "Margherita", "quantity": 32, "revenue": 544.00 },
      { "name": "Quattro Formaggi", "quantity": 18, "revenue": 423.00 },
      { "name": "Coca Cola", "quantity": 45, "revenue": 202.50 },
      { "name": "Pizza Diavola", "quantity": 12, "revenue": 246.00 },
      { "name": "Tiramisu", "quantity": 15, "revenue": 127.50 }
    ],
    "cancelledOrders": {
      "count": 2,
      "lostRevenue": 65.00,
      "reasons": [
        "Kunde storniert",
        "Produkt ausverkauft"
      ]
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
