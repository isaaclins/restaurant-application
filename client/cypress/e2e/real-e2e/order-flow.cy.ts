/// <reference types="cypress" />

/**
 * REAL E2E Tests - Order Flow
 * 
 * Prerequisites:
 * 1. Backend must be running: ./start.sh --backend
 * 2. Frontend must be running: cd client && npm run dev
 * 
 * These tests use REAL data and test actual user journeys.
 */

describe('Order Flow - Real E2E', () => {
  
  const testUser = {
    email: 'admin@restaurant.com',
    password: 'admin123'
  };

  // Generate unique customer name for this test run
  const testCustomer = `Test Customer ${Date.now()}`;

  beforeEach(() => {
    // Clear cookies and localStorage before each test
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  describe('User Story: Create and Track an Order', () => {
    
    it('should complete the full order creation flow', () => {
      // ==========================================
      // STEP 1: Login with real credentials
      // ==========================================
      cy.visit('/login');
      cy.get('#root', { timeout: 10000 }).should('not.be.empty');
      
      cy.get('input#email').type(testUser.email);
      cy.get('input#password').type(testUser.password);
      cy.get('button[type="submit"]').click();
      
      // Wait for redirect to KDS (indicates successful login)
      cy.url({ timeout: 15000 }).should('include', '/kds');
      cy.log('✅ Step 1: Logged in successfully');
      
      // ==========================================
      // STEP 2: Navigate to KDS and verify it loaded
      // ==========================================
      cy.contains('Restaurant KDS', { timeout: 10000 }).should('be.visible');
      cy.log('✅ Step 2: KDS page loaded');
      
      // ==========================================
      // STEP 3: Click "Create Order" button
      // ==========================================
      cy.contains('button', 'CREATE NEW ORDER').click();
      
      // Modal should open - wait for it to be fully visible
      cy.get('.fixed.inset-0', { timeout: 5000 }).should('be.visible');
      cy.log('✅ Step 3: Order creation modal opened');
      
      // ==========================================
      // STEP 4: Fill in order details (within modal)
      // ==========================================
      cy.get('.fixed.inset-0').within(() => {
        // Customer name - find the first input
        cy.get('input').first().clear().type(testCustomer);
        
        // Select order type (if available)
        cy.get('select').then($select => {
          if ($select.length) {
            cy.wrap($select).first().select(1); // Select first non-default option
          }
        });
        
        cy.log('✅ Step 4: Order details filled');
        
        // ==========================================
        // STEP 5: Submit the order
        // ==========================================
        cy.contains('button', /Create|Submit|Save/i).click();
      });
      
      // Wait for modal to close
      cy.get('.fixed.inset-0').should('not.exist');
      cy.log('✅ Step 5: Order submitted');
      
      // ==========================================
      // STEP 6: Verify order appears in KDS
      // ==========================================
      cy.contains(testCustomer, { timeout: 10000 }).should('be.visible');
      cy.log('✅ Step 6: Order visible in KDS');
      
      // ==========================================
      // STEP 7: Verify order status
      // ==========================================
      cy.contains(testCustomer)
        .parents('[class*="card"], .bg-white')
        .should('contain.text', 'PENDING');
      cy.log('✅ Step 7: Order status is PENDING');
    });

    it('should update order status through the workflow', () => {
      // Login first
      cy.visit('/login');
      cy.get('input#email').type(testUser.email);
      cy.get('input#password').type(testUser.password);
      cy.get('button[type="submit"]').click();
      cy.url({ timeout: 15000 }).should('include', '/kds');
      
      // Find any pending order
      cy.get('body').then($body => {
        const hasPendingOrders = $body.find(':contains("PENDING")').length > 0;
        
        if (hasPendingOrders) {
          // Click on first pending order to start preparing
          cy.contains('PENDING')
            .parents('[class*="card"], .bg-white')
            .first()
            .find('button')
            .first()
            .click();
          
          // Status should change
          cy.contains('PREPARING', { timeout: 5000 }).should('exist');
          cy.log('✅ Order status updated to PREPARING');
        } else {
          cy.log('⚠️ No pending orders found - create one first');
        }
      });
    });
  });

  describe('User Story: View Order History', () => {
    
    it('should view receipts/order history', () => {
      // Login
      cy.visit('/login');
      cy.get('input#email').type(testUser.email);
      cy.get('input#password').type(testUser.password);
      cy.get('button[type="submit"]').click();
      cy.url({ timeout: 15000 }).should('include', '/kds');
      
      // Navigate to receipts
      cy.get('a[href="/receipts"], button').contains('Receipts').click();
      cy.url().should('include', '/receipts');
      
      // Page should load
      cy.contains('Receipts', { timeout: 10000 }).should('be.visible');
      cy.log('✅ Receipts page loaded');
    });
  });
});
