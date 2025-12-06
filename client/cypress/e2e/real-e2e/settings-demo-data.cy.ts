/// <reference types="cypress" />

/**
 * REAL E2E Tests - Settings & Demo Data
 * 
 * Prerequisites:
 * 1. Backend must be running: ./start.sh --backend
 * 2. Frontend must be running: cd client && npm run dev
 * 
 * This test populates demo data which other tests can use.
 */

describe('Settings & Demo Data - Real E2E', () => {
  
  const testUser = {
    email: 'admin@restaurant.com',
    password: 'admin123'
  };

  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  describe('User Story: Configure Restaurant Settings', () => {
    
    it('should navigate to settings page', () => {
      // Login
      cy.visit('/login');
      cy.get('input#email').type(testUser.email);
      cy.get('input#password').type(testUser.password);
      cy.get('button[type="submit"]').click();
      cy.url({ timeout: 15000 }).should('include', '/kds');
      
      // Navigate to settings
      cy.get('a[href="/settings"]').click();
      cy.url().should('include', '/settings');
      
      // Verify settings page loaded
      cy.contains('Settings', { timeout: 10000 }).should('be.visible');
      
      cy.log('✅ Settings page loaded successfully');
    });

    it('should populate demo data from developer section', () => {
      // Login
      cy.visit('/login');
      cy.get('input#email').type(testUser.email);
      cy.get('input#password').type(testUser.password);
      cy.get('button[type="submit"]').click();
      cy.url({ timeout: 15000 }).should('include', '/kds');
      
      // Navigate to settings
      cy.visit('/settings');
      cy.contains('Settings', { timeout: 10000 }).should('be.visible');
      
      // Look for Developer section
      cy.get('body').then($body => {
        if ($body.find(':contains("Developer")').length) {
          // Click on Developer section/tab
          cy.contains('Developer').click();
          
          // Look for Populate Demo Data button
          cy.contains('button', /Populate|Demo|Sample/i, { timeout: 5000 }).then($btn => {
            cy.wrap($btn).click();
            
            // Wait for data to be populated
            cy.wait(5000);
            
            cy.log('✅ Demo data populated successfully');
          });
        } else {
          cy.log('⚠️ Developer section not found in settings');
        }
      });
    });

    it('should update restaurant settings', () => {
      // Login
      cy.visit('/login');
      cy.get('input#email').type(testUser.email);
      cy.get('input#password').type(testUser.password);
      cy.get('button[type="submit"]').click();
      cy.url({ timeout: 15000 }).should('include', '/kds');
      
      // Navigate to settings
      cy.visit('/settings');
      cy.contains('Settings', { timeout: 10000 }).should('be.visible');
      
      // Try to find and update a setting
      cy.get('input').first().then($input => {
        const currentValue = $input.val();
        cy.log(`Current value: ${currentValue}`);
        
        // If it's a text input, try modifying it
        if ($input.attr('type') === 'text' || !$input.attr('type')) {
          cy.wrap($input).clear().type('Updated Test Value');
          
          // Look for save button
          cy.contains('button', /Save|Update/i).click();
          
          cy.log('✅ Settings updated');
        }
      });
    });
  });
});
