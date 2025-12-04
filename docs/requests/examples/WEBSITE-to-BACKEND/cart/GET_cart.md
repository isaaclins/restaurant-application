# GET /api/cart/{sessionId}

> Aktuellen Warenkorb abrufen

---

## Request

```http
GET /api/cart/abc123-session-id HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `sessionId` | string | ✅ | Session-ID des Warenkorbs |

### Example

```bash
curl -X GET "http://localhost:8080/api/cart/abc123-session-id"
```

---

## Response

### Success (200 OK) - Warenkorb mit Items

```json
{
  "success": true,
  "data": {
    "sessionId": "abc123-session-id",
    "items": [
      {
        "id": "item-uuid-1",
        "productId": 1,
        "productName": "Margherita",
        "quantity": 2,
        "size": "M",
        "unitPrice": 18.50,
        "extras": [
          {
            "id": 1,
            "name": "Extra Käse",
            "price": 2.50
          }
        ],
        "notes": "Extra knusprig",
        "itemTotal": 42.00
      },
      {
        "id": "item-uuid-2",
        "productId": 5,
        "productName": "Coca Cola",
        "quantity": 1,
        "size": null,
        "unitPrice": 4.50,
        "extras": [],
        "notes": null,
        "itemTotal": 4.50
      }
    ],
    "subtotal": 46.50,
    "deliveryFee": 5.00,
    "total": 51.50,
    "itemCount": 3,
    "createdAt": "2025-12-04T20:00:00Z",
    "expiresAt": "2025-12-04T22:00:00Z"
  }
}
```

### Success (200 OK) - Leerer Warenkorb

```json
{
  "success": true,
  "data": {
    "sessionId": "abc123-session-id",
    "items": [],
    "subtotal": 0,
    "deliveryFee": 0,
    "total": 0,
    "itemCount": 0,
    "createdAt": "2025-12-04T20:00:00Z",
    "expiresAt": "2025-12-04T22:00:00Z"
  }
}
```

### Error (404 Not Found) - Session abgelaufen

```json
{
  "success": false,
  "error": {
    "code": "CART_NOT_FOUND",
    "message": "Cart session has expired or does not exist"
  }
}
```

---

## Notes

- Warenkorb läuft nach 2 Stunden ab
- `deliveryFee` ist 0 bei Abholung oder wenn Mindestbestellwert nicht erreicht
- `itemCount` ist die Summe aller Mengen
