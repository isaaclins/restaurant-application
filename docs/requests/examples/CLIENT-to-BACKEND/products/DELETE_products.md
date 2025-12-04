# DELETE /api/products/{id}

> Produkt löschen

---

## Request

```http
DELETE /api/products/15 HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Path Parameters

| Parameter | Type    | Required | Description |
| --------- | ------- | -------- | ----------- |
| `id`      | integer | ✅       | Produkt-ID  |

### Example

```bash
curl -X DELETE "http://localhost:8080/api/products/15" \
  -H "Authorization: Bearer restaurant-token-123"
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
    "deletedAt": "2025-12-04T21:40:00Z"
  },
  "message": "Product deleted successfully"
}
```

### Error (400 Bad Request) - Produkt in aktiven Bestellungen

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_IN_USE",
    "message": "Cannot delete product. It is referenced in 3 active orders. Consider marking as unavailable instead."
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

- Produkte mit aktiven Bestellungen können nicht gelöscht werden
- Alternative: `PUT /products/{id}/availability` mit `available: false`
- Soft-Delete empfohlen: Produkt wird archiviert statt permanent gelöscht
- Historische Bestellungen behalten Produkt-Snapshot
