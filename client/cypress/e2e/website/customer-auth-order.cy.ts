/// <reference types="cypress" />

/**
 * Customer Website E2E - Register + authenticated checkout
 * Flow:
 * - Register new account
 * - Add address in profile
 * - Add two products to cart
 * - Checkout as delivery using saved address button
 */

const apiUrl = Cypress.env('apiUrl') || 'http://localhost:8080';
const websiteUrl = 'http://localhost:3000';

const getProducts = () =>
  cy
    .request(`${apiUrl}/api/products`)
    .its('body')
    .then((body) => body.data ?? body);

const fillInputByLabel = (label: string, value: string) => {
  cy.contains('label', label).find('input').clear().type(value);
};

describe('Customer website - register and checkout with saved address', () => {
  it('registers, saves address, and completes a delivery order', () => {
    const email = `cust+${Date.now()}@example.com`;
    const password = 'Password123!';

    // Register
    cy.visit(`${websiteUrl}/register`);
    fillInputByLabel('First Name', 'Cypress');
    fillInputByLabel('Last Name', 'Customer');
    fillInputByLabel('Email', email);
    fillInputByLabel('Phone', '+41791112233');
    fillInputByLabel('Password', password);
    fillInputByLabel('Confirm Password', password);
    cy.contains('button', 'Create Account').click();

    // Should land on profile
    cy.url({ timeout: 15000 }).should('include', '/profile');
    cy.contains('h1', 'My Profile').should('be.visible');

    // Add address
    cy.contains('button', 'Add Address').click();
    fillInputByLabel('Street', 'Teststrasse 1');
    fillInputByLabel('City', 'Zürich');
    fillInputByLabel('Postal Code', '8000');
    cy.contains('button', 'Add Address').click();
    cy.contains('Teststrasse 1').should('be.visible');

    // Fetch products and add two items
    getProducts().then((products: any[]) => {
      expect(products.length, 'products available').to.be.greaterThan(1);
      const [p1, p2] = products;

      const addProductToCart = (product: any, opts?: { quantity?: number; note?: string }) => {
        cy.visit(`${websiteUrl}/product/${product.id}`);

        cy.get('body').then(($body) => {
          const hasSize = $body.find('input[type="radio"][name="size"]').length > 0;
          if (hasSize) {
            cy.get('input[type="radio"][name="size"]').first().click();
          }
        });

        if ((opts?.quantity ?? 1) > 1) {
          const clicks = (opts?.quantity ?? 1) - 1;
          for (let i = 0; i < clicks; i += 1) {
            cy.contains('button', '+').click();
          }
        }

        if (opts?.note) {
          cy.get('textarea').clear().type(opts.note);
        }

        cy.contains('button', 'Add to Cart').click();
      };

      addProductToCart(p1, { quantity: 1 });
      addProductToCart(p2, { quantity: 2, note: 'light cheese' });

      cy.contains('h1', 'Your Cart').should('be.visible');
      cy.get('ul li').should('have.length.at.least', 2);

      cy.contains('button', 'Proceed to Checkout').click();

      // Delivery checkout
      cy.url().should('include', '/checkout');
      cy.get('input[type="radio"][name="orderType"][value="DELIVERY"]').check();

      // Use saved address button
      cy.contains('Use a saved address:').parent().find('button').first().click();

      // Fill required fields (should be prefilled from user/account)
      fillInputByLabel('Name', 'Cypress Customer');
      fillInputByLabel('Phone', '+41791112233');
      fillInputByLabel('Email', email);

      cy.contains('button', 'Place Order').click();

      cy.url({ timeout: 15000 }).should('match', /\/order\/\d+$/);
      cy.contains('Order Confirmed!', { timeout: 15000 }).should('be.visible');
      cy.contains(/Order #/).should('be.visible');
      cy.contains('Delivery').should('exist');
      cy.contains('Teststrasse 1').should('be.visible');
    });
  });
});
