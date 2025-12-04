# POST /auth/register

> Neuen Kunden-Account erstellen (Website)

---

## Request

```http
POST /auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

### Body

```json
{
  "email": "max.mustermann@example.com",
  "password": "SecurePassword123!",
  "firstName": "Max",
  "lastName": "Mustermann",
  "phone": "+41 79 123 45 67",
  "address": {
    "street": "Musterstrasse 42",
    "city": "Zürich",
    "postalCode": "8000"
  }
}
```

### Example

```bash
curl -X POST "http://localhost:8080/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "max.mustermann@example.com",
    "password": "SecurePassword123!",
    "firstName": "Max",
    "lastName": "Mustermann",
    "phone": "+41 79 123 45 67"
  }'
```

---

## Response

### Success (201 Created)

```json
{
  "success": true,
  "data": {
    "id": "cust-456",
    "email": "max.mustermann@example.com",
    "firstName": "Max",
    "lastName": "Mustermann",
    "phone": "+41 79 123 45 67",
    "createdAt": "2025-12-04T12:00:00Z"
  },
  "message": "Account created successfully. Please verify your email."
}
```

### Error (400 Bad Request)

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "email": "Invalid email format",
      "password": "Password must be at least 8 characters"
    }
  }
}
```

### Error (409 Conflict)

```json
{
  "success": false,
  "error": {
    "code": "EMAIL_EXISTS",
    "message": "An account with this email already exists"
  }
}
```

---

## Validation Rules

| Field       | Rules                                        |
| ----------- | -------------------------------------------- |
| `email`     | Required, valid email format, unique         |
| `password`  | Required, min 8 chars, 1 uppercase, 1 number |
| `firstName` | Required, 2-50 chars                         |
| `lastName`  | Required, 2-50 chars                         |
| `phone`     | Optional, valid phone format                 |
| `address`   | Optional, can be added later                 |

---

## Notes

- Password is hashed with bcrypt before storing
- Email verification optional (configurable)
- Address can be added/updated later in profile
