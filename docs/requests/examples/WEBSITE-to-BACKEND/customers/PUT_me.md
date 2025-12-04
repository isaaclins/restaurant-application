# PUT /customers/me

> Eigenes Kundenprofil aktualisieren

---

## Request

```http
PUT /api/customers/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
Content-Type: application/json
```

### Body

```json
{
  "firstName": "Maximilian",
  "lastName": "Mustermann",
  "phone": "+41 79 999 88 77"
}
```

### Example

```bash
curl -X PUT "http://localhost:8080/api/customers/me" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Maximilian", "phone": "+41 79 999 88 77"}'
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
    "firstName": "Maximilian",
    "lastName": "Mustermann",
    "phone": "+41 79 999 88 77",
    "updatedAt": "2025-12-04T14:30:00Z"
  }
}
```

---

## Notes

- Email cannot be changed (use separate endpoint for email change with verification)
- Password change requires separate endpoint with current password verification
