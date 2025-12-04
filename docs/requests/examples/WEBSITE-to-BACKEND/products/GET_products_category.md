# GET /api/products/category/{category}

> Produkte nach Kategorie filtern

---

## Request

```http
GET /api/products/category/PIZZA HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `category` | string | ✅ | Kategorie-Name |

### Valid Categories

| Category | Description |
|----------|-------------|
| `PIZZA` | Pizzen |
| `PASTA` | Pasta-Gerichte |
| `SALAD` | Salate |
| `DESSERT` | Desserts |
| `DRINKS` | Getränke |
| `SIDES` | Beilagen |

### Example

```bash
curl -X GET "http://localhost:8080/api/products/category/PIZZA"
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Margherita",
      "description": "Tomaten, Mozzarella, Basilikum",
      "price": 14.50,
      "category": "PIZZA",
      "imageUrl": "/images/margherita.jpg",
      "available": true,
      "sizes": {
        "S": 14.50,
        "M": 18.50,
        "L": 22.50
      }
    },
    {
      "id": 3,
      "name": "Quattro Formaggi",
      "description": "Vier Käsesorten",
      "price": 18.50,
      "category": "PIZZA",
      "imageUrl": "/images/quattro.jpg",
      "available": true,
      "sizes": {
        "S": 18.50,
        "M": 22.50,
        "L": 26.50
      }
    }
  ],
  "category": "PIZZA",
  "total": 2
}
```

### Error (400 Bad Request)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_CATEGORY",
    "message": "Category 'BURGERS' is not valid. Valid categories: PIZZA, PASTA, SALAD, DESSERT, DRINKS, SIDES"
  }
}
```

---

## Notes

- Nur verfügbare Produkte (`available: true`) werden zurückgegeben
- Kategorie ist case-insensitive
