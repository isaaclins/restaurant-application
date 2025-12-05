/// <reference types="cypress" />

// ***********************************************
// Custom Commands for Restaurant Application E2E Tests
// ***********************************************

const API_URL = Cypress.env('apiUrl') || 'http://localhost:8080';

// Login command - bypasses UI for faster tests
Cypress.Commands.add('login', (username = 'admin', password = 'admin123') => {
  // For now, we're not using real auth, so just set a mock token
  // When auth is fully implemented, use the real login endpoint
  cy.window().then((win) => {
    win.localStorage.setItem('accessToken', 'mock-jwt-token');
    win.localStorage.setItem('refreshToken', 'mock-refresh-token');
    win.localStorage.setItem('user', JSON.stringify({
      id: 1,
      username: username,
      email: `${username}@restaurant.local`,
      role: 'ADMIN'
    }));
  });
});

// Logout command
Cypress.Commands.add('logout', () => {
  cy.window().then((win) => {
    win.localStorage.removeItem('accessToken');
    win.localStorage.removeItem('refreshToken');
    win.localStorage.removeItem('user');
  });
  cy.visit('/login');
});

// Create order via API
Cypress.Commands.add('createOrder', (orderData) => {
  const defaultOrder = {
    customerName: 'Test Customer',
    customerEmail: 'test@example.com',
    customerPhone: '+41 79 123 4567',
    orderType: 'PICKUP',
    paymentMethod: 'CASH',
    items: [],
    totalPrice: 0,
    estimatedDelivery: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
  };

  return cy.request({
    method: 'POST',
    url: `${API_URL}/api/orders`,
    body: { ...defaultOrder, ...orderData },
    headers: {
      'Content-Type': 'application/json',
    },
  });
});

// Create product via API
Cypress.Commands.add('createProduct', (productData) => {
  return cy.request({
    method: 'POST',
    url: `${API_URL}/api/products`,
    body: productData,
    headers: {
      'Content-Type': 'application/json',
    },
  });
});

// Create category via API
Cypress.Commands.add('createCategory', (categoryData) => {
  return cy.request({
    method: 'POST',
    url: `${API_URL}/api/categories`,
    body: categoryData,
    headers: {
      'Content-Type': 'application/json',
    },
  });
});

// Populate demo data
Cypress.Commands.add('populateDemoData', () => {
  cy.visit('/settings');
  cy.contains('Developer').click();
  cy.contains('Populate Demo Data').click();
  // Wait for progress to complete
  cy.get('[data-testid="populate-progress"]', { timeout: 30000 }).should('not.exist');
});

// Clear all data
Cypress.Commands.add('clearAllData', () => {
  // Delete all orders
  cy.request({ url: `${API_URL}/api/orders`, failOnStatusCode: false }).then((response) => {
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

  // Delete all products
  cy.request({ url: `${API_URL}/api/products`, failOnStatusCode: false }).then((response) => {
    if (response.status === 200 && Array.isArray(response.body)) {
      response.body.forEach((product: any) => {
        cy.request({
          method: 'DELETE',
          url: `${API_URL}/api/products/${product.id}`,
          failOnStatusCode: false,
        });
      });
    }
  });

  // Delete all categories
  cy.request({ url: `${API_URL}/api/categories`, failOnStatusCode: false }).then((response) => {
    if (response.status === 200 && Array.isArray(response.body)) {
      response.body.forEach((category: any) => {
        cy.request({
          method: 'DELETE',
          url: `${API_URL}/api/categories/${category.id}`,
          failOnStatusCode: false,
        });
      });
    }
  });
});

// Wait for API call
Cypress.Commands.add('waitForApi', (alias) => {
  return cy.wait(`@${alias}`);
});

// Check toast notification
Cypress.Commands.add('checkToast', (message, type = 'success') => {
  const bgColor = type === 'success' ? 'bg-green-500' : 'bg-red-500';
  cy.get(`.${bgColor}`).should('contain', message);
});
