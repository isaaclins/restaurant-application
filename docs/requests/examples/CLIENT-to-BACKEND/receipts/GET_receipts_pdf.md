# GET /api/receipts/{id}/pdf

> Rechnung als PDF herunterladen

---

## Request

```http
GET /api/receipts/5001/pdf HTTP/1.1
Host: localhost:8080
Accept: application/pdf
Authorization: Bearer <restaurant-token>
```

### Path Parameters

| Parameter | Type    | Required | Description  |
| --------- | ------- | -------- | ------------ |
| `id`      | integer | ✅       | Rechnungs-ID |

### Example

```bash
curl -X GET "http://localhost:8080/api/receipts/5001/pdf" \
  -H "Authorization: Bearer restaurant-token-123" \
  -o rechnung-5001.pdf
```

---

## Response

### Success (200 OK)

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="receipt-5001.pdf"

[PDF Binary Data]
```

### PDF Inhalt

```
┌─────────────────────────────────────┐
│         RESTAURANT NAME             │
│      Strasse 123, 8000 Zürich       │
│         Tel: 044 123 45 67          │
├─────────────────────────────────────┤
│  Rechnung Nr: 5001                  │
│  Datum: 04.12.2025 20:45            │
│  Ticket: A-042                      │
├─────────────────────────────────────┤
│  Kunde: Max Mustermann              │
│  Tel: +41 79 123 45 67              │
│  Lieferung: Bahnhofstrasse 10       │
│             8001 Zürich             │
├─────────────────────────────────────┤
│  2x Margherita (M)      CHF  37.00  │
│     + Extra Käse        CHF   5.00  │
├─────────────────────────────────────┤
│  Zwischensumme          CHF  42.00  │
│  Liefergebühr           CHF   5.00  │
│  ─────────────────────────────────  │
│  TOTAL                  CHF  47.00  │
├─────────────────────────────────────┤
│  Zahlungsart: Kreditkarte           │
│  Status: Bezahlt                    │
│                                     │
│  Vielen Dank für Ihre Bestellung!   │
└─────────────────────────────────────┘
```

### Error (404 Not Found)

```json
{
  "success": false,
  "error": {
    "code": "RECEIPT_NOT_FOUND",
    "message": "Receipt with ID 9999 not found"
  }
}
```

---

## Notes

- PDF wird dynamisch generiert
- Enthält Restaurant-Branding (konfigurierbar in Settings)
- Kann direkt gedruckt werden
- MWST-Details optional (je nach Konfiguration)
