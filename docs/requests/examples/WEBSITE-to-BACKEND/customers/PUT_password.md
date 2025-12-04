# PUT /customers/me/password

> Passwort ändern (Website-Kunde)

---

## Request

```http
PUT /api/customers/me/password HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
Content-Type: application/json
```

### Body

```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewSecurePassword456!",
  "confirmPassword": "NewSecurePassword456!"
}
```

### Example

```bash
curl -X PUT "http://localhost:8080/api/customers/me/password" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "OldPassword123!",
    "newPassword": "NewSecurePassword456!",
    "confirmPassword": "NewSecurePassword456!"
  }'
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

### Error (400 Bad Request)

```json
{
  "success": false,
  "error": {
    "code": "PASSWORD_MISMATCH",
    "message": "New password and confirmation do not match"
  }
}
```

### Error (401 Unauthorized)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_CURRENT_PASSWORD",
    "message": "Current password is incorrect"
  }
}
```

### Error (422 Unprocessable Entity)

```json
{
  "success": false,
  "error": {
    "code": "WEAK_PASSWORD",
    "message": "Password does not meet requirements",
    "details": {
      "requirements": [
        "Minimum 8 characters",
        "At least 1 uppercase letter",
        "At least 1 number"
      ]
    }
  }
}
```

---

## Notes

- Requires valid JWT with `CUSTOMER` role
- Current password must be verified before change
- All existing refresh tokens are invalidated after password change
- User must re-login after password change
