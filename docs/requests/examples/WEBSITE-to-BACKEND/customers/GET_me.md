# GET /customers/me

> Eigenes Kundenprofil abrufen

---

## Request

```http
GET /api/customers/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
```

### Example

```bash
curl -X GET "http://localhost:8080/api/customers/me" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "id": "cust-456",
    "email": "max.mustermann@example.com",
    "firstName": "Max",
    "lastName": "Mustermann",
    "phone": "+41 79 123 45 67",
    "addresses": [
      {
        "id": "addr-1",
        "label": "Zuhause",
        "street": "Musterstrasse 42",
        "city": "Zürich",
        "postalCode": "8000",
        "isDefault": true
      },
      {
        "id": "addr-2",
        "label": "Arbeit",
        "street": "Bahnhofstrasse 10",
        "city": "Zürich",
        "postalCode": "8001",
        "isDefault": false
      }
    ],
    "createdAt": "2025-12-04T12:00:00Z"
  }
}
```

### Error (401 Unauthorized)

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token"
  }
}
```

---

## Notes

- Requires valid JWT with `CUSTOMER` role
- Returns all saved addresses for checkout convenience
