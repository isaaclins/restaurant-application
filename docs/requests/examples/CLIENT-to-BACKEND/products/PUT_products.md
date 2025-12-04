# PUT /api/products/{id}

> Produkt bearbeiten

---

## Request

```http
PUT /api/products/15 HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Path Parameters

| Parameter | Type    | Required | Description |
| --------- | ------- | -------- | ----------- |
| `id`      | integer | ✅       | Produkt-ID  |

### Request Body

```json
{
  "name": "Pizza Diavola",
  "description": "Scharfe Salami, Peperoncini, Mozzarella, Chili-Öl",
  "price": 20.5,
  "category": "PIZZA",
  "available": true,
  "allergens": ["GLUTEN", "DAIRY"],
  "sizes": {
    "S": 20.5,
    "M": 24.5,
    "L": 28.5
  },
  "extras": [
    {
      "id": 10,
      "name": "Extra Salami",
      "price": 4.0
    },
    {
      "name": "Burrata",
      "price": 5.0
    }
  ]
}
```

### Body Parameters

Alle Felder sind optional - nur übergebene Felder werden aktualisiert.

| Field         | Type          | Description                             |
| ------------- | ------------- | --------------------------------------- |
| `name`        | string        | Produktname                             |
| `description` | string        | Beschreibung                            |
| `price`       | decimal       | Basispreis                              |
| `category`    | string        | Kategorie                               |
| `available`   | boolean       | Verfügbarkeit                           |
| `allergens`   | array[string] | Allergene                               |
| `sizes`       | object        | Grössen mit Preisen                     |
| `extras`      | array         | Extras (mit ID = update, ohne ID = neu) |

### Example - Nur Preis ändern

```bash
curl -X PUT "http://localhost:8080/api/products/15" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{"price": 21.00}'
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
    "description": "Scharfe Salami, Peperoncini, Mozzarella, Chili-Öl",
    "price": 20.5,
    "category": "PIZZA",
    "imageUrl": "/images/diavola.jpg",
    "available": true,
    "allergens": ["GLUTEN", "DAIRY"],
    "sizes": {
      "S": 20.5,
      "M": 24.5,
      "L": 28.5
    },
    "extras": [
      {
        "id": 10,
        "name": "Extra Salami",
        "price": 4.0
      },
      {
        "id": 12,
        "name": "Burrata",
        "price": 5.0
      }
    ],
    "createdAt": "2025-12-04T21:10:00Z",
    "updatedAt": "2025-12-04T21:30:00Z"
  },
  "message": "Product updated successfully"
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

- Partial Updates möglich - nur übergebene Felder werden geändert
- Extras mit vorhandener `id` werden aktualisiert
- Extras ohne `id` werden neu erstellt
- Um Extras zu löschen, separaten DELETE Endpoint nutzen
