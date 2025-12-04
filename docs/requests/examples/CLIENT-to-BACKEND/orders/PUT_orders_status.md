# PUT /api/orders/{id}/status

> Bestellstatus ändern (KDS)

---

## Request

```http
PUT /api/orders/1001/status HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Path Parameters

| Parameter | Type    | Required | Description |
| --------- | ------- | -------- | ----------- |
| `id`      | integer | ✅       | Bestell-ID  |

### Request Body

```json
{
  "status": "PREPARING"
}
```

### Valid Status Transitions

```
NEW → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED
                                   └→ PICKED_UP
     └→ CANCELLED (vor READY möglich)
```

| From               | To (allowed)                    |
| ------------------ | ------------------------------- |
| `NEW`              | `CONFIRMED`, `CANCELLED`        |
| `CONFIRMED`        | `PREPARING`, `CANCELLED`        |
| `PREPARING`        | `READY`, `CANCELLED`            |
| `READY`            | `OUT_FOR_DELIVERY`, `PICKED_UP` |
| `OUT_FOR_DELIVERY` | `DELIVERED`                     |

### Example

```bash
curl -X PUT "http://localhost:8080/api/orders/1001/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{"status": "PREPARING"}'
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
    "previousStatus": "CONFIRMED",
    "status": "PREPARING",
    "updatedAt": "2025-12-04T20:50:00Z"
  },
  "message": "Order status updated to PREPARING"
}
```

### Error (400 Bad Request) - Ungültiger Übergang

```json
{
  "success": false,
  "error": {
    "code": "INVALID_STATUS_TRANSITION",
    "message": "Cannot transition from 'DELIVERED' to 'PREPARING'"
  }
}
```

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

- Status-Änderungen werden im `statusHistory` der Bestellung gespeichert
- Bei Übergang zu `CANCELLED` sollte optional ein `reason` Feld mitgegeben werden
- WebSocket-Notification wird an Kunden gesendet (wenn implementiert)
