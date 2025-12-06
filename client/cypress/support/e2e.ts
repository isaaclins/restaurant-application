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

// Handle uncaught exceptions
Cypress.on('uncaught:exception', (err, runnable) => {
  // Returning false prevents Cypress from failing the test on uncaught exceptions
  // Common errors to ignore
  if (err.message.includes('ResizeObserver') || 
      err.message.includes('Script error') ||
      err.message.includes('Non-Error promise rejection')) {
    return false;
  }
  return true;
});
