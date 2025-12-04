# GET /customers/me/orders

> Eigene Bestellhistorie abrufen

---

## Request

```http
GET /api/customers/me/orders HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
```

### Query Parameters (optional)

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | int | Seite (default: 0) |
| `size` | int | Anzahl pro Seite (default: 10, max: 50) |
| `status` | string | Filter nach Status |

### Example

```bash
curl -X GET "http://localhost:8080/api/customers/me/orders?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "orders": [
      {
        "id": "order-789",
        "ticketNumber": "A-042",
        "status": "DELIVERED",
        "orderType": "DELIVERY",
        "items": [
          {
            "productName": "Margherita",
            "size": "L",
            "quantity": 2,
            "totalPrice": 45.00
          },
          {
            "productName": "Cola",
            "quantity": 2,
            "totalPrice": 8.00
          }
        ],
        "totalAmount": 53.00,
        "createdAt": "2025-12-03T19:30:00Z",
        "deliveredAt": "2025-12-03T20:15:00Z"
      },
      {
        "id": "order-654",
        "ticketNumber": "A-038",
        "status": "DELIVERED",
        "orderType": "PICKUP",
        "items": [
          {
            "productName": "Quattro Stagioni",
            "size": "M",
            "quantity": 1,
            "totalPrice": 22.50
          }
        ],
        "totalAmount": 22.50,
        "createdAt": "2025-11-28T12:00:00Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 15,
      "totalPages": 2
    }
  }
}
```

---

## Notes

- Ordered by `createdAt` descending (newest first)
- Useful for "Order Again" feature
- Only shows own orders (validated by JWT)
