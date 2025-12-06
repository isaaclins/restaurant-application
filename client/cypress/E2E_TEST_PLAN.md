# E2E Test Plan - Restaurant Application

## Overview

This test suite provides **comprehensive end-to-end testing** for the restaurant application. Tests are designed to run against **real services** (not mocks) to validate actual user workflows.

## Test Structure

```
cypress/e2e/
├── auth.cy.ts              # Login page tests (24 tests)
├── security.cy.ts          # Security vulnerability tests (15 tests)
└── real-e2e/               # Real user journey tests
    ├── full-user-journey.cy.ts    # Complete workflow test
    ├── order-flow.cy.ts           # Order creation & tracking
    ├── product-management.cy.ts   # Product CRUD operations
    └── settings-demo-data.cy.ts   # Settings & demo data
```

## Running Tests

### Full Test Suite (Recommended)

```bash
# From project root - starts everything and runs all tests
./start.sh --test
```

This will:

1. Run backend unit tests
2. Start backend services (MySQL, Redis, Kafka, microservices)
3. Start frontend dev server
4. Run ALL E2E tests:
   - Authentication tests (24 tests)
   - Security tests (15 tests)
   - Real E2E user journey tests
5. Clean up and report results

### Individual Test Suites

```bash
cd client

# Auth tests only (no backend needed)
npm run test:e2e:auth

# Security tests only
npm run test:e2e:security

# Real E2E tests (requires running backend)
npm run test:e2e:real

# Interactive mode
npm run test:e2e:real:open
```

---

## Test Categories

### 1. Authentication Tests (`auth.cy.ts`) - 24 Tests

Tests the login page and authentication flow:

| Test ID  | Description                                  |
| -------- | -------------------------------------------- |
| AUTH-001 | Login page renders correctly                 |
| AUTH-002 | Login form has required fields               |
| AUTH-003 | Login form submission with valid credentials |
| AUTH-004 | Invalid credentials show error message       |
| AUTH-005 | Empty email validation                       |
| AUTH-006 | Empty password validation                    |
| AUTH-007 | Invalid email format validation              |
| AUTH-008 | Wrong password shows error                   |
| AUTH-009 | Non-existent user shows error                |
| AUTH-010 | Password field is type password              |
| AUTH-011 | Tab navigation works                         |
| AUTH-012 | Enter key submits form                       |
| AUTH-013 | Remember me checkbox                         |
| AUTH-014 | Token storage after login                    |
| AUTH-015 | Session persistence                          |
| AUTH-016 | Logout clears session                        |
| AUTH-017 | Protected route redirects                    |
| AUTH-018 | Password not visible in URL                  |
| AUTH-019 | Max length validation                        |
| AUTH-020 | XSS prevention in fields                     |
| AUTH-021 | SQL injection prevention                     |
| AUTH-022 | Rate limiting                                |
| AUTH-023 | HTTPS redirect                               |
| AUTH-024 | Session timeout                              |

### 2. Security Tests (`security.cy.ts`) - 15 Tests

Tests security vulnerabilities at API level:

| Test ID | Description                       |
| ------- | --------------------------------- |
| SEC-001 | SQL injection in login            |
| SEC-002 | SQL injection in product search   |
| SEC-003 | SQL injection in order creation   |
| SEC-004 | XSS in product name               |
| SEC-005 | XSS in customer name              |
| SEC-006 | XSS in review comments            |
| SEC-007 | Token manipulation detection      |
| SEC-008 | Expired token rejection           |
| SEC-009 | Protected routes without token    |
| SEC-010 | Password not in URL params        |
| SEC-011 | HTML injection prevention         |
| SEC-012 | Script tag sanitization           |
| SEC-013 | Mass assignment protection        |
| SEC-014 | JSON injection prevention         |
| SEC-015 | Information disclosure prevention |

### 3. Real E2E User Journey Tests (`real-e2e/`)

These tests validate **actual user workflows** against the running system.

#### Full User Journey (`full-user-journey.cy.ts`)

Complete workflow: Login → Create Order → Track → Complete → View Receipt

```
1. Login with admin credentials
2. Verify products exist (populate demo data if empty)
3. Create new order with customer name
4. Track order status in KDS
5. Update order: PENDING → PREPARING → READY → COMPLETED
6. Verify order appears in receipts
7. Test navigation between all pages
8. Test logout functionality
```

#### Order Flow (`order-flow.cy.ts`)

| Test           | Description                                     |
| -------------- | ----------------------------------------------- |
| Order Creation | Login → KDS → New Order → Fill details → Submit |
| Order Tracking | Verify order appears in KDS with correct status |
| Status Updates | Move order through PENDING → PREPARING → READY  |
| Order History  | Verify completed orders appear in receipts      |

#### Product Management (`product-management.cy.ts`)

| Test                | Description                                |
| ------------------- | ------------------------------------------ |
| View Products       | Login → Navigate to products page          |
| Create Product      | Add new product with name, price, category |
| Search Products     | Use search functionality                   |
| Toggle Availability | Enable/disable product availability        |

#### Settings & Demo Data (`settings-demo-data.cy.ts`)

| Test               | Description                                 |
| ------------------ | ------------------------------------------- |
| View Settings      | Navigate to settings page                   |
| Populate Demo Data | Use developer section to populate test data |
| Update Settings    | Modify and save restaurant settings         |

---

## Prerequisites

### For Full Test Suite

```bash
# Ensure you have:
- Docker & Docker Compose (for MySQL, Redis, Kafka)
- Java 17+ (for Spring Boot services)
- Node.js 18+ (for frontend)
- Chrome browser (for Cypress)
```

### Test Credentials

```
Admin User:
- Email: admin@restaurant.com
- Password: admin123

Staff User:
- Email: staff@restaurant.com
- Password: staff123
```

---

## CI/CD Integration

The `./start.sh --test` command is designed for CI/CD pipelines:

```yaml
# GitHub Actions example
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Full Test Suite
        run: ./start.sh --test
```

Exit codes:

- `0` - All tests passed
- `1` - One or more tests failed

---

## Test Data

Tests use real data from the running system. The test suite:

1. Logs in with real credentials
2. Creates real orders with unique customer names
3. Interacts with actual API endpoints
4. Verifies data persistence in the database

No mocking - tests validate the **actual system behavior**.

---

## Troubleshooting

### Tests fail with "Backend not ready"

```bash
# Ensure backend services are healthy
curl http://localhost:8080/actuator/health
curl http://localhost:8761  # Eureka dashboard
```

### Tests fail with blank page

```bash
# Ensure frontend is running
curl http://localhost:1420
```

### Tests fail with authentication errors

```bash
# Verify default user exists
# The DataLoader creates admin@restaurant.com on startup
./start.sh --full  # Full reset with fresh database
```

---

## Adding New Tests

1. **User journey tests**: Add to `cypress/e2e/real-e2e/`
2. **Security tests**: Add to `cypress/e2e/security.cy.ts`
3. **Auth tests**: Add to `cypress/e2e/auth.cy.ts`

Follow the existing patterns:

- Use real credentials (no mocking)
- Generate unique test data (use `Date.now()` for names)
- Add proper assertions
- Include logging (`cy.log()`) for debugging
