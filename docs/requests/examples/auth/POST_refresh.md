# POST /auth/refresh

> Refresh Access Token

---

## Request

```http
POST /auth/refresh HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

### Body

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

### Example

```bash
curl -X POST "http://localhost:8080/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."}'
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.newtoken...",
    "refreshToken": "bmV3IHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

### Error (401 Unauthorized)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_REFRESH_TOKEN",
    "message": "Refresh token is invalid or expired"
  }
}
```

---

## Notes

- Old refresh token is invalidated after use (rotation)
- If refresh fails, user must re-login
