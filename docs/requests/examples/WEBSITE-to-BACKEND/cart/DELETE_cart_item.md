# DELETE /api/cart/{sessionId}/items/{itemId}

> Item aus dem Warenkorb entfernen

---

## Request

```http
DELETE /api/cart/abc123-session-id/items/item-uuid-1 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `sessionId` | string | ✅ | Session-ID des Warenkorbs |
| `itemId` | string | ✅ | ID des Items im Warenkorb |

### Example

```bash
curl -X DELETE "http://localhost:8080/api/cart/abc123-session-id/items/item-uuid-1"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "sessionId": "abc123-session-id",
    "items": [
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
    "subtotal": 4.50,
    "deliveryFee": 5.00,
    "total": 9.50,
    "itemCount": 1,
    "expiresAt": "2025-12-04T22:00:00Z"
  },
  "message": "Item removed from cart"
}
```

### Error (404 Not Found) - Item existiert nicht

```json
{
  "success": false,
  "error": {
    "code": "ITEM_NOT_FOUND",
    "message": "Item 'item-uuid-999' not found in cart"
  }
}
```

### Error (404 Not Found) - Warenkorb existiert nicht

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

- Gibt den aktualisierten Warenkorb zurück
- Wenn letztes Item entfernt wird, bleibt Warenkorb mit leerer `items` Liste
