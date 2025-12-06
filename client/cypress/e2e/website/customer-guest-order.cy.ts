/// <reference types="cypress" />

/**
 * Customer Website E2E - Guest checkout
 * Flow:
 * - Fetch products from API
 * - Add two items to cart (with notes/size when available)
 * - Remove one item in cart, adjust quantity
 * - Checkout as pickup and place order
 */

const apiUrl = Cypress.env('apiUrl') || 'http://localhost:8080';
const websiteUrl = 'http://localhost:3000';

const getProducts = () =>
  cy
    .request(`${apiUrl}/api/products`)
    .its('body')
    .then((body) => body.data ?? body);

describe('Customer website - guest order', () => {
  it('guest can add multiple items, remove one, and checkout', () => {
    getProducts().then((products: any[]) => {
      expect(products.length, 'products available').to.be.greaterThan(1);

      const [p1, p2] = products;

      const addProductToCart = (product: any, opts?: { quantity?: number; note?: string }) => {
        cy.visit(`${websiteUrl}/product/${product.id}`);

        // Optional: pick a size if present
        cy.get('body').then(($body) => {
          const hasSize = $body.find('input[type="radio"][name="size"]').length > 0;
          if (hasSize) {
            cy.get('input[type="radio"][name="size"]').first().click();
          }
        });

        // Quantity (default 1). If need more, click +
        if ((opts?.quantity ?? 1) > 1) {
          const clicks = (opts?.quantity ?? 1) - 1;
          for (let i = 0; i < clicks; i += 1) {
            cy.contains('button', '+').click();
          }
        }

        // Notes
        if (opts?.note) {
          cy.get('textarea').clear().type(opts.note);
        }

        cy.contains('button', 'Add to Cart').click();
      };

      // Add two different products
      addProductToCart(p1, { quantity: 2, note: 'extra crispy' });
      addProductToCart(p2, { quantity: 1 });

      // In cart: expect at least 2 items, remove one, adjust quantity
      cy.contains('h1', 'Your Cart').should('be.visible');
      cy.get('ul li').should('have.length.at.least', 2);

      // Remove first item
      cy.get('ul li').first().within(() => {
        cy.contains('button', 'Remove').click();
      });

      // For remaining items, increment quantity by 1
      cy.get('ul li').first().within(() => {
        cy.contains('button', '+').click();
      });

      cy.contains('button', 'Proceed to Checkout').click();

      // Checkout as pickup
      cy.url().should('include', '/checkout');
      cy.contains('h1', 'Checkout').should('be.visible');

      cy.contains('label', 'Name').find('input').clear().type('Guest Tester');
      cy.contains('label', 'Phone').find('input').clear().type('+41790000000');
      cy.contains('label', 'Email').find('input').clear().type('guest@example.com');

      // Ensure pickup is selected
      cy.get('input[type="radio"][name="orderType"][value="PICKUP"]').check();

      cy.contains('button', 'Place Order').click();

      // Confirmation page
      cy.url({ timeout: 15000 }).should('match', /\/order\/\d+$/);
      cy.contains('Order Confirmed!', { timeout: 15000 }).should('be.visible');
      cy.contains(/Order #/).should('be.visible');
    });
  });
});
