# GET /api/products/{id}

> Einzelnes Produkt mit Details abrufen

---

## Request

```http
GET /api/products/1 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Path Parameters

| Parameter | Type    | Required | Description |
| --------- | ------- | -------- | ----------- |
| `id`      | integer | ✅       | Produkt ID  |

### Example

```bash
curl -X GET "http://localhost:8080/api/products/1"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Margherita",
    "description": "Tomaten, Mozzarella, Basilikum",
    "price": 14.5,
    "category": "PIZZA",
    "imageUrl": "/images/margherita.jpg",
    "available": true,
    "allergens": ["GLUTEN", "DAIRY"],
    "sizes": {
      "S": 14.5,
      "M": 18.5,
      "L": 22.5
    },
    "extras": [
      {
        "id": 1,
        "name": "Extra Käse",
        "price": 2.5
      },
      {
        "id": 2,
        "name": "Schinken",
        "price": 3.0
      }
    ],
    "createdAt": "2025-12-01T10:00:00Z",
    "updatedAt": "2025-12-04T15:30:00Z"
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

- Gibt detaillierte Produktinformationen zurück
- Inkludiert `createdAt` und `updatedAt` Timestamps
