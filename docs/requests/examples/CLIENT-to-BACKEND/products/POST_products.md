# POST /api/products

> Neues Produkt erstellen

---

## Request

```http
POST /api/products HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
Authorization: Bearer <restaurant-token>
```

### Request Body

```json
{
  "name": "Pizza Diavola",
  "description": "Scharfe Salami, Peperoncini, Mozzarella",
  "price": 19.5,
  "category": "PIZZA",
  "available": true,
  "allergens": ["GLUTEN", "DAIRY"],
  "sizes": {
    "S": 19.5,
    "M": 23.5,
    "L": 27.5
  },
  "extras": [
    {
      "name": "Extra Salami",
      "price": 3.5
    },
    {
      "name": "Jalapeños",
      "price": 2.0
    }
  ]
}
```

### Body Parameters

| Field         | Type          | Required | Description                     |
| ------------- | ------------- | -------- | ------------------------------- |
| `name`        | string        | ✅       | Produktname                     |
| `description` | string        | ✅       | Beschreibung                    |
| `price`       | decimal       | ✅       | Basispreis                      |
| `category`    | string        | ✅       | Kategorie (PIZZA, PASTA, etc.)  |
| `available`   | boolean       | ❌       | Standard: true                  |
| `allergens`   | array[string] | ❌       | Liste der Allergene             |
| `sizes`       | object        | ❌       | Grössen mit Preisen (nur Pizza) |
| `extras`      | array         | ❌       | Optionale Extras                |

### Valid Categories

`PIZZA`, `PASTA`, `SALAD`, `DESSERT`, `DRINKS`, `SIDES`

### Valid Allergens

`GLUTEN`, `DAIRY`, `EGGS`, `FISH`, `SHELLFISH`, `NUTS`, `PEANUTS`, `SOY`, `SESAME`

### Example

```bash
curl -X POST "http://localhost:8080/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer restaurant-token-123" \
  -d '{
    "name": "Tiramisu",
    "description": "Hausgemachtes italienisches Dessert",
    "price": 8.50,
    "category": "DESSERT",
    "allergens": ["GLUTEN", "DAIRY", "EGGS"]
  }'
```

---

## Response

### Success (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 15,
    "name": "Pizza Diavola",
    "description": "Scharfe Salami, Peperoncini, Mozzarella",
    "price": 19.5,
    "category": "PIZZA",
    "imageUrl": null,
    "available": true,
    "allergens": ["GLUTEN", "DAIRY"],
    "sizes": {
      "S": 19.5,
      "M": 23.5,
      "L": 27.5
    },
    "extras": [
      {
        "id": 10,
        "name": "Extra Salami",
        "price": 3.5
      },
      {
        "id": 11,
        "name": "Jalapeños",
        "price": 2.0
      }
    ],
    "createdAt": "2025-12-04T21:10:00Z",
    "updatedAt": "2025-12-04T21:10:00Z"
  },
  "message": "Product created successfully"
}
```

### Error (400 Bad Request) - Doppelter Name

```json
{
  "success": false,
  "error": {
    "code": "DUPLICATE_PRODUCT",
    "message": "Product with name 'Pizza Diavola' already exists"
  }
}
```

### Error (400 Bad Request) - Ungültige Kategorie

```json
{
  "success": false,
  "error": {
    "code": "INVALID_CATEGORY",
    "message": "Category 'BURGERS' is not valid"
  }
}
```

---

## Notes

- `imageUrl` wird separat über Bild-Upload gesetzt
- Bei Pizza sollten `sizes` definiert werden
- `extras` bekommen automatisch IDs zugewiesen
