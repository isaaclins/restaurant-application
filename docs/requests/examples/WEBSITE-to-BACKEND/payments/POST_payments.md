# POST /api/payments

> Zahlung durchführen (Mockup)

---

## Request

```http
POST /api/payments HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
```

### Request Body

```json
{
  "orderId": 1001,
  "method": "CARD",
  "cardDetails": {
    "number": "4242424242424242",
    "expiry": "12/26",
    "cvv": "123",
    "holderName": "Max Mustermann"
  }
}
```

### Body Parameters

| Field         | Type    | Required | Description                     |
| ------------- | ------- | -------- | ------------------------------- |
| `orderId`     | integer | ✅       | Bestell-ID                      |
| `method`      | string  | ✅       | `CARD`, `TWINT`, `CASH`         |
| `cardDetails` | object  | ✅\*     | Kartendaten (\*nur bei CARD)    |
| `twintPhone`  | string  | ✅\*     | Telefonnummer (\*nur bei TWINT) |

### Example - Kartenzahlung

```bash
curl -X POST "http://localhost:8080/api/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1001,
    "method": "CARD",
    "cardDetails": {
      "number": "4242424242424242",
      "expiry": "12/26",
      "cvv": "123",
      "holderName": "Max Mustermann"
    }
  }'
```

### Example - TWINT

```bash
curl -X POST "http://localhost:8080/api/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1001,
    "method": "TWINT",
    "twintPhone": "+41 79 123 45 67"
  }'
```

### Example - Barzahlung

```bash
curl -X POST "http://localhost:8080/api/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1001,
    "method": "CASH"
  }'
```

---

## Response

### Success (200 OK) - Kartenzahlung

```json
{
  "success": true,
  "data": {
    "paymentId": "pay_abc123xyz",
    "orderId": 1001,
    "method": "CARD",
    "amount": 47.0,
    "currency": "CHF",
    "status": "COMPLETED",
    "transactionId": "txn_mock_12345",
    "processedAt": "2025-12-04T20:46:00Z"
  },
  "message": "Payment successful"
}
```

### Success (200 OK) - TWINT (Mockup)

```json
{
  "success": true,
  "data": {
    "paymentId": "pay_twint456",
    "orderId": 1001,
    "method": "TWINT",
    "amount": 47.0,
    "currency": "CHF",
    "status": "COMPLETED",
    "transactionId": "twint_mock_67890",
    "processedAt": "2025-12-04T20:46:00Z"
  },
  "message": "TWINT payment successful"
}
```

### Success (200 OK) - Barzahlung

```json
{
  "success": true,
  "data": {
    "paymentId": "pay_cash789",
    "orderId": 1001,
    "method": "CASH",
    "amount": 47.0,
    "currency": "CHF",
    "status": "PENDING",
    "processedAt": null
  },
  "message": "Cash payment will be collected on delivery/pickup"
}
```

### Error (400 Bad Request) - Bereits bezahlt

```json
{
  "success": false,
  "error": {
    "code": "ALREADY_PAID",
    "message": "Order 1001 has already been paid"
  }
}
```

### Error (400 Bad Request) - Ungültige Karte (Mockup)

```json
{
  "success": false,
  "error": {
    "code": "CARD_DECLINED",
    "message": "Card was declined. Please try a different payment method."
  }
}
```

### Error (404 Not Found)

```json
{
  "success": false,
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "Order with ID 9999 not found"
  }
}
```

---

## Mockup Test Cards

| Card Number        | Result                |
| ------------------ | --------------------- |
| `4242424242424242` | ✅ Success            |
| `4000000000000002` | ❌ Declined           |
| `4000000000009995` | ❌ Insufficient funds |

---

## Notes

- Dies ist ein **Mockup** - keine echte Zahlungsabwicklung
- Bei `CASH` wird der Status auf `PENDING` gesetzt bis Lieferung/Abholung
- Order Status wird automatisch auf `CONFIRMED` gesetzt nach erfolgreicher Zahlung
- In Produktion würde hier Stripe/PayPal/TWINT-API integriert
