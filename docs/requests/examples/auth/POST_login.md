# POST /auth/login

> Restaurant Client Login

---

## Request

```http
POST /auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

### Body

```json
{
  "username": "kitchen-user",
  "password": "secure-password-123"
}
```

### Example

```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "kitchen-user", "password": "secure-password-123"}'
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLTEyMyIsInJvbGUiOiJSRVNUQVVSQU5UX0FETUlOIiwicmVzdGF1cmFudF9pZCI6InJlc3QtMTIzIiwiaWF0IjoxNzAxNzM0NDAwLCJleHAiOjE3MDE3MzgwMDB9.abc123",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "user": {
      "id": "user-123",
      "username": "kitchen-user",
      "role": "RESTAURANT_ADMIN",
      "restaurantId": "rest-123"
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
    "message": "Invalid username or password"
  }
}
```

### Error (423 Locked)

```json
{
  "success": false,
  "error": {
    "code": "ACCOUNT_LOCKED",
    "message": "Account locked due to too many failed attempts. Try again in 15 minutes."
  }
}
```

---

## Notes

- Access Token expires after 1 hour
- Refresh Token expires after 7 days
- Store tokens securely in Tauri (keychain/credential store)
- Rate limit: 5 attempts per minute
