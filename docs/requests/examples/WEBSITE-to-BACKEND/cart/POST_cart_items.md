# POST /api/cart/{sessionId}/items

> Produkt zum Warenkorb hinzufügen

---

## Request

```http
POST /api/cart/abc123-session-id/items HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
```

### Path Parameters

| Parameter   | Type   | Required | Description                                  |
| ----------- | ------ | -------- | -------------------------------------------- |
| `sessionId` | string | ✅       | Eindeutige Session-ID (vom Client generiert) |

### Request Body

```json
{
  "productId": 1,
  "quantity": 2,
  "size": "M",
  "extras": [1, 2],
  "notes": "Ohne Zwiebeln bitte"
}
```

### Body Parameters

| Field       | Type       | Required | Description                    |
| ----------- | ---------- | -------- | ------------------------------ |
| `productId` | integer    | ✅       | ID des Produkts                |
| `quantity`  | integer    | ✅       | Menge (min: 1, max: 99)        |
| `size`      | string     | ❌       | Grösse (S/M/L) - nur für Pizza |
| `extras`    | array[int] | ❌       | IDs der gewählten Extras       |
| `notes`     | string     | ❌       | Spezielle Wünsche              |

### Example

```bash
curl -X POST "http://localhost:8080/api/cart/abc123-session-id/items" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2,
    "size": "M",
    "extras": [1],
    "notes": "Extra knusprig"
  }'
```

---

## Response

### Success (201 Created)

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
        "unitPrice": 18.5,
        "extras": [
          {
            "id": 1,
            "name": "Extra Käse",
            "price": 2.5
          }
        ],
        "notes": "Extra knusprig",
        "itemTotal": 42.0
      }
    ],
    "subtotal": 42.0,
    "deliveryFee": 5.0,
    "total": 47.0,
    "itemCount": 2,
    "expiresAt": "2025-12-04T23:30:00Z"
  }
}
```

### Error (400 Bad Request) - Produkt nicht verfügbar

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_UNAVAILABLE",
    "message": "Product 'Margherita' is currently not available"
  }
}
```

### Error (400 Bad Request) - Ungültige Grösse

```json
{
  "success": false,
  "error": {
    "code": "INVALID_SIZE",
    "message": "Size 'XL' is not valid for this product. Valid sizes: S, M, L"
  }
}
```

### Error (404 Not Found)

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Product with ID 999 not found"
  }
}
```

---

## Notes

- `sessionId` wird vom Client generiert (UUID empfohlen)
- Warenkorb läuft nach 2 Stunden ab (Redis TTL)
- `itemTotal` = (unitPrice + extras) × quantity
- Wenn gleiches Produkt mit gleichen Optionen hinzugefügt wird, erhöht sich die Menge
