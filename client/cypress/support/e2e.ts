// ***********************************************************
// This file is processed and loaded automatically before test files.
// ***********************************************************

import './commands';

// Prevent TypeScript from reading file as legacy script
export {};

// Hide fetch/XHR requests in command log for cleaner output
const app = window.top;
if (app && !app.document.head.querySelector('[data-hide-command-log-request]')) {
  const style = app.document.createElement('style');
  style.innerHTML = '.command-name-request, .command-name-xhr { display: none }';
  style.setAttribute('data-hide-command-log-request', '');
  app.document.head.appendChild(style);
}

// Global before each hook
beforeEach(() => {
  // Intercept common API calls
  cy.intercept('GET', '**/api/orders*').as('getOrders');
  cy.intercept('POST', '**/api/orders*').as('createOrder');
  cy.intercept('PUT', '**/api/orders/**').as('updateOrder');
  cy.intercept('DELETE', '**/api/orders/**').as('deleteOrder');
  
  cy.intercept('GET', '**/api/products*').as('getProducts');
  cy.intercept('POST', '**/api/products*').as('createProduct');
  cy.intercept('PUT', '**/api/products/**').as('updateProduct');
  cy.intercept('DELETE', '**/api/products/**').as('deleteProduct');
  
  cy.intercept('GET', '**/api/categories*').as('getCategories');
  cy.intercept('POST', '**/api/categories*').as('createCategory');
  cy.intercept('PUT', '**/api/categories/**').as('updateCategory');
  cy.intercept('DELETE', '**/api/categories/**').as('deleteCategory');
  
  cy.intercept('GET', '**/api/receipts*').as('getReceipts');
  cy.intercept('GET', '**/api/receipts/*/pdf').as('downloadPdf');
  cy.intercept('GET', '**/api/receipts/report*').as('getDailyReport');
  
  cy.intercept('GET', '**/api/settings*').as('getSettings');
  cy.intercept('PUT', '**/api/settings*').as('updateSettings');
});
