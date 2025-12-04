# POST /customers/me/addresses

> Neue Lieferadresse hinzufügen

---

## Request

```http
POST /api/customers/me/addresses HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
Content-Type: application/json
```

### Body

```json
{
  "label": "Arbeit",
  "street": "Bahnhofstrasse 10",
  "city": "Zürich",
  "postalCode": "8001",
  "isDefault": false
}
```

### Example

```bash
curl -X POST "http://localhost:8080/api/customers/me/addresses" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Arbeit",
    "street": "Bahnhofstrasse 10",
    "city": "Zürich",
    "postalCode": "8001"
  }'
```

---

## Response

### Success (201 Created)

```json
{
  "success": true,
  "data": {
    "id": "addr-2",
    "label": "Arbeit",
    "street": "Bahnhofstrasse 10",
    "city": "Zürich",
    "postalCode": "8001",
    "isDefault": false
  }
}
```

### Error (400 Bad Request)

```json
{
  "success": false,
  "error": {
    "code": "OUTSIDE_DELIVERY_AREA",
    "message": "This address is outside our delivery area"
  }
}
```

---

## Notes

- Max 5 addresses per customer
- Validates against restaurant's delivery area (postal codes)
- If `isDefault: true`, previous default is set to false
