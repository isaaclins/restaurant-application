# GET /api/orders

> Alle aktiven Bestellungen für KDS abrufen

---

## Request

```http
GET /api/orders HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Query Parameters (optional)

| Parameter | Type     | Description                                |
| --------- | -------- | ------------------------------------------ |
| `status`  | string   | Filter nach Status (z.B. `NEW,PREPARING`)  |
| `type`    | string   | Filter nach Typ (`DELIVERY` oder `PICKUP`) |
| `from`    | datetime | Bestellungen ab Datum                      |
| `to`      | datetime | Bestellungen bis Datum                     |

### Example

```bash
curl -X GET "http://localhost:8080/api/orders?status=NEW,PREPARING,READY" \
  -H "Authorization: Bearer restaurant-token-123"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "pickup": [
      {
        "id": 1001,
        "ticketNumber": "A-042",
        "status": "PREPARING",
        "customer": {
          "name": "Max Mustermann",
          "phone": "+41 79 123 45 67"
        },
        "items": [
          {
            "productName": "Margherita",
            "quantity": 2,
            "size": "M",
            "extras": ["Extra Käse"],
            "notes": "Extra knusprig",
            "completed": false
          },
          {
            "productName": "Coca Cola",
            "quantity": 1,
            "size": null,
            "extras": [],
            "notes": null,
            "completed": true
          }
        ],
        "total": 47.0,
        "paymentMethod": "CARD",
        "paymentStatus": "PAID",
        "createdAt": "2025-12-04T20:45:00Z",
        "elapsedMinutes": 12
      }
    ],
    "delivery": [
      {
        "id": 1002,
        "ticketNumber": "B-015",
        "status": "NEW",
        "customer": {
          "name": "Anna Beispiel",
          "phone": "+41 79 999 88 77"
        },
        "deliveryAddress": {
          "street": "Hauptstrasse 5",
          "city": "Winterthur",
          "postalCode": "8400",
          "notes": "Hintereingang"
        },
        "items": [
          {
            "productName": "Quattro Formaggi",
            "quantity": 1,
            "size": "L",
            "extras": [],
            "notes": null,
            "completed": false
          }
        ],
        "total": 31.5,
        "paymentMethod": "CASH",
        "paymentStatus": "PENDING",
        "createdAt": "2025-12-04T20:55:00Z",
        "elapsedMinutes": 2
      }
    ],
    "counts": {
      "new": 1,
      "preparing": 1,
      "ready": 0,
      "total": 2
    }
  }
}
```

---

## Notes

- Bestellungen werden nach `pickup` und `delivery` getrennt
- `elapsedMinutes` zeigt Zeit seit Bestellung (für Farbcodes: Grün < 10, Gelb < 20, Rot >= 20)
- `completed` auf Item-Ebene zeigt, welche Items fertig sind (für Checkboxen)
- Diese Route ist nur für authentifizierte Restaurant-Clients
