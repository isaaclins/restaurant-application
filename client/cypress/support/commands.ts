/// <reference types="cypress" />

// ***********************************************
// Custom Commands for Restaurant Application E2E Tests
// ***********************************************

const API_URL = Cypress.env('apiUrl') || 'http://localhost:8080';

/**
 * Login by setting up auth state in localStorage
 * This is a programmatic login that doesn't require backend
 */
Cypress.Commands.add('login', (email = 'admin@restaurant.com', password = 'admin123') => {
  // Create mock user data
  const mockUser = {
    id: 1,
    email: email,
    firstName: email.includes('admin') ? 'Admin' : 'Staff',
    lastName: 'User',
    role: email.includes('admin') ? 'RESTAURANT_ADMIN' : 'RESTAURANT_STAFF'
  };
  
  // Create auth state for zustand persist
  const authStorage = {
    state: {
      user: mockUser,
      isAuthenticated: true
    },
    version: 0
  };
  
  const authData = {
    accessToken: 'mock-jwt-token-' + Date.now(),
    refreshToken: 'mock-refresh-token-' + Date.now(),
    authStorage: JSON.stringify(authStorage)
  };
  
  // Store auth data for cy.visit to use
  Cypress.env('_authData', authData);
  
  // Also store the raw data for direct access
  Cypress.env('_mockUser', mockUser);
  Cypress.env('_authStorageRaw', authStorage);
});

/**
 * Login as admin
 */
Cypress.Commands.add('loginAsAdmin', () => {
  cy.login('admin@restaurant.com', 'admin123');
});

/**
 * Login as staff
 */
Cypress.Commands.add('loginAsStaff', () => {
  cy.login('staff@restaurant.com', 'staff123');
});

/**
 * Login via actual API call (for integration tests)
 */
Cypress.Commands.add('loginViaApi', (email = 'admin@restaurant.com', password = 'admin123') => {
  return cy.request({
    method: 'POST',
    url: `${API_URL}/api/auth/login`,
    body: { email, password },
    failOnStatusCode: false,
    timeout: 10000
  }).then((response) => {
    if (response.status === 200 && response.body.accessToken) {
      const authStorage = {
        state: {
          user: response.body.user,
          isAuthenticated: true
        },
        version: 0
      };
      
      Cypress.env('_authData', {
        accessToken: response.body.accessToken,
        refreshToken: response.body.refreshToken,
        authStorage: JSON.stringify(authStorage)
      });
      
      return cy.wrap(response.body);
    } else {
      return cy.wrap(null);
    }
  });
});

// DISABLED: Visit overwrite was causing issues with authenticated pages
// /**
//  * Override cy.visit to apply auth if login was called
//  */
// Cypress.Commands.overwrite('visit', (originalFn: Cypress.CommandOriginalFn<'visit'>, url: string, options?: Partial<Cypress.VisitOptions>) => {
//   const authDataFromEnv = Cypress.env('_authData');
//   const opts = options || {};
//   
//   if (authDataFromEnv) {
//     const originalOnBeforeLoad = opts.onBeforeLoad;
//     opts.onBeforeLoad = (win: Cypress.AUTWindow) => {
//       win.localStorage.setItem('accessToken', authDataFromEnv.accessToken);
//       win.localStorage.setItem('refreshToken', authDataFromEnv.refreshToken);
//       win.localStorage.setItem('auth-storage', authDataFromEnv.authStorage);
//       
//       if (originalOnBeforeLoad) {
//         originalOnBeforeLoad(win);
//       }
//     };
//   }
//   
//   return originalFn(url, opts);
// });

/**
 * Logout - clear all auth state
 */
Cypress.Commands.add('logout', () => {
  Cypress.env('_authData', null);
  cy.window().then((win) => {
    win.localStorage.removeItem('accessToken');
    win.localStorage.removeItem('refreshToken');
    win.localStorage.removeItem('auth-storage');
  });
});

/**
 * Create order via API
 */
Cypress.Commands.add('createOrder', (orderData = {}) => {
  const authData = Cypress.env('_authData');
  const defaultOrder = {
    customerName: 'Test Customer',
    customerEmail: 'test@example.com',
    customerPhone: '+41 79 123 4567',
    orderType: 'PICKUP',
    paymentMethod: 'CASH',
    items: [
      {
        productId: 1,
        productName: 'Test Product',
        quantity: 1,
        unitPrice: 10.00,
        totalPrice: 10.00
      }
    ],
    totalPrice: 10.00,
    estimatedDelivery: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
  };

  return cy.request({
    method: 'POST',
    url: `${API_URL}/api/orders`,
    body: { ...defaultOrder, ...orderData },
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authData ? `Bearer ${authData.accessToken}` : ''
    },
    failOnStatusCode: false,
  });
});

/**
 * Create product via API
 */
Cypress.Commands.add('createProduct', (productData) => {
  const authData = Cypress.env('_authData');
  return cy.request({
    method: 'POST',
    url: `${API_URL}/api/products`,
    body: productData,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authData ? `Bearer ${authData.accessToken}` : ''
    },
    failOnStatusCode: false,
  });
});

/**
 * Create category via API
 */
Cypress.Commands.add('createCategory', (categoryData) => {
  const authData = Cypress.env('_authData');
  return cy.request({
    method: 'POST',
    url: `${API_URL}/api/categories`,
    body: categoryData,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authData ? `Bearer ${authData.accessToken}` : ''
    },
    failOnStatusCode: false,
  });
});

/**
 * Clear all test data from backend
 */
Cypress.Commands.add('clearTestData', () => {
  cy.request({ 
    url: `${API_URL}/api/orders`, 
    failOnStatusCode: false 
  }).then((response) => {
    if (response.status === 200 && Array.isArray(response.body)) {
      response.body.forEach((order: any) => {
        cy.request({
          method: 'DELETE',
          url: `${API_URL}/api/orders/${order.id}`,
          failOnStatusCode: false,
        });
      });
    }
  });
});

/**
 * Populate demo data via Settings page
 */
Cypress.Commands.add('populateDemoData', () => {
  cy.visit('/settings');
  cy.contains('Developer').click();
  cy.contains('Populate Demo Data').click();
  cy.get('[data-testid="populate-progress"]', { timeout: 30000 }).should('not.exist');
});

/**
 * Wait for API call
 */
Cypress.Commands.add('waitForApi', (alias: string) => {
  return cy.wait(`@${alias}`);
});

/**
 * Check toast notification
 */
Cypress.Commands.add('checkToast', (message: string, type: 'success' | 'error' = 'success') => {
  const selector = type === 'success' 
    ? '.bg-green-500, .bg-green-50' 
    : '.bg-red-500, .bg-red-50';
  cy.get(selector).should('contain', message);
});

/**
 * Should show error message
 */
Cypress.Commands.add('shouldShowError', (message: string) => {
  cy.get('.bg-red-50, .text-red-600, .text-red-500, [role="alert"]')
    .should('be.visible')
    .and('contain', message);
});

/**
 * Should show success message
 */
Cypress.Commands.add('shouldShowSuccess', (message: string) => {
  cy.get('.bg-green-50, .text-green-600, .text-green-500, [role="status"]')
    .should('be.visible')
    .and('contain', message);
});

/**
 * Should be on specific page
 */
Cypress.Commands.add('shouldBeOnPage', (path: string) => {
  cy.url().should('include', path);
});

/**
 * Test SQL injection on a field
 */
Cypress.Commands.add('testSQLInjection', (selector: string, callback: (payload: string) => void) => {
  const payloads = [
    "' OR '1'='1",
    "'; DROP TABLE users;--",
    "' UNION SELECT * FROM users--"
  ];
  
  payloads.forEach(payload => {
    cy.get(selector).clear().type(payload, { parseSpecialCharSequences: false });
    callback(payload);
  });
});

/**
 * Test XSS on a field
 */
Cypress.Commands.add('testXSS', (selector: string) => {
  const payloads = [
    "<script>alert('XSS')</script>",
    "<img src=x onerror=alert('XSS')>",
    "<svg onload=alert('XSS')>"
  ];
  
  cy.window().then((win) => {
    cy.spy(win, 'alert').as('alertSpy');
  });
  
  payloads.forEach(payload => {
    cy.get(selector).clear().type(payload, { parseSpecialCharSequences: false });
    cy.get('@alertSpy').should('not.have.been.called');
  });
});

/**
 * Verify no script execution on page
 */
Cypress.Commands.add('checkNoScriptExecution', () => {
  cy.window().then((win) => {
    const alertSpy = cy.spy(win, 'alert');
    const confirmSpy = cy.spy(win, 'confirm');
    const promptSpy = cy.spy(win, 'prompt');
    
    cy.wait(1000).then(() => {
      expect(alertSpy).not.to.have.been.called;
      expect(confirmSpy).not.to.have.been.called;
      expect(promptSpy).not.to.have.been.called;
    });
  });
});

/**
 * Visit a protected page with auth - combines login + visit + API mocks
 * This is the recommended way to test authenticated pages
 */
Cypress.Commands.add('visitAuthenticated', (path: string, apiMocks?: Record<string, any>) => {
  // Set up default API mocks
  cy.intercept('GET', '**/api/products*', { body: apiMocks?.products || [] }).as('getProducts');
  cy.intercept('GET', '**/api/categories*', { body: apiMocks?.categories || [] }).as('getCategories');
  cy.intercept('GET', '**/api/orders*', { body: apiMocks?.orders || [] }).as('getOrders');
  cy.intercept('GET', '**/api/receipts*', { body: apiMocks?.receipts || [] }).as('getReceipts');
  cy.intercept('GET', '**/api/settings*', { body: apiMocks?.settings || {} }).as('getSettings');
  
  // Visit with auth directly injected
  cy.visit(path, {
    onBeforeLoad: (win) => {
      win.localStorage.setItem('accessToken', 'mock-jwt-token-' + Date.now());
      win.localStorage.setItem('refreshToken', 'mock-refresh-token-' + Date.now());
      win.localStorage.setItem('auth-storage', JSON.stringify({
        state: {
          user: { id: 1, email: 'admin@restaurant.com', role: 'ADMIN' },
          isAuthenticated: true
        },
        version: 0
      }));
    }
  });
});

/**
 * Set up default API mocks for authenticated pages
 * Call this before visiting protected routes
 */
Cypress.Commands.add('setupApiMocks', () => {
  cy.intercept('GET', '**/api/products*', (req) => {
    req.reply({ statusCode: 200, body: [] });
  }).as('getProducts');
  
  cy.intercept('GET', '**/api/categories*', (req) => {
    req.reply({ statusCode: 200, body: [] });
  }).as('getCategories');
  
  cy.intercept('GET', '**/api/orders*', (req) => {
    req.reply({ statusCode: 200, body: [] });
  }).as('getOrders');
  
  cy.intercept('GET', '**/api/receipts*', (req) => {
    req.reply({ statusCode: 200, body: [] });
  }).as('getReceipts');
  
  cy.intercept('GET', '**/api/receipts/report*', (req) => {
    req.reply({ 
      statusCode: 200, 
      body: { totalReceipts: 0, totalRevenue: 0, averageOrderValue: 0 }
    });
  }).as('getReceiptsReport');
  
  cy.intercept('GET', '**/api/settings*', (req) => {
    req.reply({
      statusCode: 200,
      body: {
        restaurantName: 'Test Restaurant',
        address: '123 Test Street',
        phone: '+41 79 123 4567',
        email: 'test@restaurant.com'
      }
    });
  }).as('getSettings');
});
