# PUT /api/products/{id}/availability

> Produktverfügbarkeit schnell umschalten

---

## Request

```http
PUT /api/products/15/availability HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | ✅ | Produkt-ID |

### Request Body

```json
{
  "available": false
}
```

### Example - Ausverkauft setzen

```bash
curl -X PUT "http://localhost:8080/api/products/15/availability" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{"available": false}'
```

### Example - Wieder verfügbar

```bash
curl -X PUT "http://localhost:8080/api/products/15/availability" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{"available": true}'
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 15,
    "name": "Pizza Diavola",
    "available": false,
    "updatedAt": "2025-12-04T21:35:00Z"
  },
  "message": "Product marked as unavailable"
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

- Schneller Toggle für "Ausverkauft" Funktion im KDS
- Produkte mit `available: false` werden auf der Website nicht angezeigt
- Laufende Bestellungen mit diesem Produkt sind nicht betroffen
