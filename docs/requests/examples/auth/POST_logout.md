# POST /auth/logout

> Logout and invalidate tokens

---

## Request

```http
POST /auth/logout HTTP/1.1
Host: localhost:8080
Authorization: Bearer <access-token>
Content-Type: application/json
```

### Body (optional)

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

### Example

```bash
curl -X POST "http://localhost:8080/auth/logout" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."}'
```

---

## Response

### Success (200 OK)

```json
{
  "success": true,
  "message": "Successfully logged out"
}
```

---

## Notes

- Invalidates refresh token in database
- Access token remains valid until expiry (stateless JWT)
- Client should delete stored tokens
