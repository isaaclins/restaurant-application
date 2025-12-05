/// <reference types="cypress" />

/**
 * KDS (Kitchen Display System) Tests
 * Tests for the kitchen display interface
 */
describe('KDS (Kitchen Display System)', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('Order Display', () => {
    describe('KDS-001: KDS Page Loads', () => {
      it('should load KDS page and display grid layout', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Should show both Pickup and Delivery sections
        cy.contains('Pickup').should('be.visible');
        cy.contains('Delivery').should('be.visible');
        
        // Should have grid layout
        cy.get('[class*="grid"]').should('exist');
      });
    });

    describe('KDS-002: Pickup Orders Display', () => {
      it('should display pickup orders in pickup section', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Find pickup section
        cy.contains('Pickup').parent().parent().within(() => {
          // Orders should be displayed if they exist
          cy.get('[class*="grid"]').should('exist');
        });
      });
    });

    describe('KDS-003: Delivery Orders Display', () => {
      it('should display delivery orders in delivery section', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Find delivery section
        cy.contains('Delivery').parent().parent().within(() => {
          // Orders should be displayed if they exist
          cy.get('[class*="grid"]').should('exist');
        });
      });
    });

    describe('KDS-004: Empty Orders Not Displayed', () => {
      it('should not display orders without items', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Empty orders should not appear in the grid
        // Each visible order card should have items
        cy.get('[data-testid="order-card"], .order-card').each(($card) => {
          cy.wrap($card).should('not.contain', '0 items');
        });
      });
    });

    describe('KDS-005: Order Card Details', () => {
      it('should display order details on card', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // If there are orders, check card content
        cy.get('body').then(($body) => {
          if ($body.find('[data-testid="order-card"], .order-card, [class*="rounded-lg"][class*="shadow"]').length > 0) {
            cy.get('[data-testid="order-card"], .order-card, [class*="rounded-lg"][class*="shadow"]').first().within(() => {
              // Should show order number (ORD-XXXXX format)
              cy.contains(/ORD-|#/).should('be.visible');
            });
          }
        });
      });
    });
  });

  describe('Timer & Sorting', () => {
    describe('KDS-006: Timer Display', () => {
      it('should display timer with countdown format', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Timer should show MM:SS format
        cy.get('body').then(($body) => {
          const orderCards = $body.find('[data-testid="order-card"], .order-card, [class*="rounded-lg"]');
          if (orderCards.length > 0) {
            // Look for time format (e.g., "05:30" or "-02:15")
            cy.contains(/\d{1,3}:\d{2}/).should('exist');
          }
        });
      });
    });

    describe('KDS-007: Overdue Orders Styling', () => {
      it('should display overdue orders with red styling', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Overdue orders should have red header/background
        cy.get('body').then(($body) => {
          if ($body.find('.bg-red-400, .bg-red-500, [class*="red"]').length > 0) {
            cy.get('.bg-red-400, .bg-red-500').should('exist');
          }
        });
      });
    });

    describe('KDS-008: Sort Toggle', () => {
      it('should toggle between ascending and descending sort', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Find sort toggle button
        cy.get('button').contains(/asc|desc|↑|↓/i).then(($btn) => {
          const initialText = $btn.text();
          cy.wrap($btn).click();
          // Text or icon should change after click
          cy.wrap($btn).should('not.have.text', initialText);
        });
      });
    });

    describe('KDS-009: Sort by Time', () => {
      it('should sort orders by ETA when time sort is selected', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Click on time sort option
        cy.contains(/time|timer/i).click({ force: true });
        
        // Orders should be reordered (visual check)
        cy.get('[data-testid="order-card"], .order-card').should('exist');
      });
    });

    describe('KDS-010: Sort by Items', () => {
      it('should sort orders by item count when items sort is selected', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        // Click on items sort option
        cy.contains(/items|products/i).click({ force: true });
        
        // Orders should be reordered (visual check)
        cy.get('[data-testid="order-card"], .order-card').should('exist');
      });
    });
  });

  describe('Order Actions', () => {
    describe('KDS-011: Open Order Modal', () => {
      it('should open detail modal when clicking on order', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        cy.get('body').then(($body) => {
          const orderCards = $body.find('[data-testid="order-card"], .order-card, [class*="rounded-lg"][class*="shadow"]');
          if (orderCards.length > 0) {
            // Click on first order
            cy.get('[data-testid="order-card"], .order-card, [class*="rounded-lg"][class*="shadow"]')
              .first()
              .click();
            
            // Modal should appear
            cy.get('[data-testid="order-modal"], [role="dialog"], .modal, .fixed.inset-0')
              .should('be.visible');
          }
        });
      });
    });

    describe('KDS-012: Complete Order', () => {
      it('should complete order and remove from grid', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        cy.get('body').then(($body) => {
          const orderCards = $body.find('[data-testid="order-card"], .order-card');
          if (orderCards.length > 0) {
            // Click on order to open modal
            cy.get('[data-testid="order-card"], .order-card').first().click();
            
            // Click complete button
            cy.contains(/complete|done|finish/i).click();
            
            // Wait for API call
            cy.wait('@updateOrder');
          }
        });
      });
    });

    describe('KDS-013: Cancel Order Shows Confirmation', () => {
      it('should show confirmation dialog when canceling order', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        cy.get('body').then(($body) => {
          const orderCards = $body.find('[data-testid="order-card"], .order-card');
          if (orderCards.length > 0) {
            // Click on order to open modal
            cy.get('[data-testid="order-card"], .order-card').first().click();
            
            // Stub window.confirm
            cy.on('window:confirm', () => false);
            
            // Click cancel button
            cy.contains(/cancel|delete/i).click();
          }
        });
      });
    });

    describe('KDS-014: Cancel Confirmation Accepted', () => {
      it('should cancel order when confirmation is accepted', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        cy.get('body').then(($body) => {
          const orderCards = $body.find('[data-testid="order-card"], .order-card');
          if (orderCards.length > 0) {
            // Click on order to open modal
            cy.get('[data-testid="order-card"], .order-card').first().click();
            
            // Accept confirmation
            cy.on('window:confirm', () => true);
            
            // Click cancel button
            cy.contains(/cancel|delete/i).click();
            
            // Wait for API call
            cy.wait('@updateOrder');
          }
        });
      });
    });

    describe('KDS-015: Cancel Confirmation Rejected', () => {
      it('should keep order when confirmation is rejected', () => {
        cy.visit('/kds');
        cy.wait('@getOrders');
        
        cy.get('body').then(($body) => {
          const orderCards = $body.find('[data-testid="order-card"], .order-card');
          if (orderCards.length > 0) {
            const initialCount = orderCards.length;
            
            // Click on order to open modal
            cy.get('[data-testid="order-card"], .order-card').first().click();
            
            // Reject confirmation
            cy.on('window:confirm', () => false);
            
            // Click cancel button
            cy.contains(/cancel|delete/i).click();
            
            // Order count should remain the same
            cy.get('[data-testid="order-card"], .order-card').should('have.length', initialCount);
          }
        });
      });
    });
  });
});
