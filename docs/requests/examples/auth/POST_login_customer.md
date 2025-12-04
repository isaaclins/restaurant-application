# POST /auth/login/customer

> Kunden-Login (Website)

---

## Request

```http
POST /auth/login/customer HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

### Body

```json
{
  "email": "max.mustermann@example.com",
  "password": "SecurePassword123!"
}
```

### Example

```bash
curl -X POST "http://localhost:8080/auth/login/customer" \
  -H "Content-Type: application/json" \
  -d '{"email": "max.mustermann@example.com", "password": "SecurePassword123!"}'
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "user": {
      "id": "cust-456",
      "email": "max.mustermann@example.com",
      "firstName": "Max",
      "lastName": "Mustermann",
      "phone": "+41 79 123 45 67",
      "role": "CUSTOMER",
      "addresses": [
        {
          "id": "addr-1",
          "street": "Musterstrasse 42",
          "city": "Zürich",
          "postalCode": "8000",
          "isDefault": true
        }
      ]
    }
  }
}
```

### Error (401 Unauthorized)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password"
  }
}
```

---

## Notes

- Unterschiedlicher Endpoint als Restaurant-Login (`/auth/login` vs `/auth/login/customer`)
- Oder alternativ: Gleicher Endpoint, Rolle wird aus DB gelesen
- Token enthält `role: "CUSTOMER"` für Frontend-Logik
