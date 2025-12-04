# GET /api/products

> Alle verfügbaren Produkte abrufen

---

## Request

```http
GET /api/products HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Query Parameters (optional)

| Parameter   | Type    | Description                    |
| ----------- | ------- | ------------------------------ |
| `available` | boolean | Filter nur verfügbare Produkte |
| `category`  | string  | Filter nach Kategorie          |

### Example

```bash
curl -X GET "http://localhost:8080/api/products?available=true"
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
      ]
    },
    {
      "id": 2,
      "name": "Coca Cola",
      "description": "0.5L Flasche",
      "price": 4.5,
      "category": "DRINKS",
      "imageUrl": "/images/cola.jpg",
      "available": true,
      "allergens": [],
      "sizes": null,
      "extras": null
    }
  ],
  "total": 2
}
```

### Error (500 Internal Server Error)

```json
{
  "success": false,
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "Failed to fetch products"
  }
}
```

---

## Notes

- Produkte mit `available: false` werden für Kunden nicht angezeigt
- `sizes` ist nur für Produkte mit Grössenoptionen (Pizza) gefüllt
- `extras` sind optionale Zusätze mit Aufpreis
