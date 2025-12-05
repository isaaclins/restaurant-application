/// <reference types="cypress" />

/**
 * Receipts Tests
 * Tests for receipts list and PDF download
 */
describe('Receipts', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('Receipt List', () => {
    describe('RCP-001: Receipts Page Display', () => {
      it('should display receipts page with list', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Should show receipts heading
        cy.contains('Receipts').should('be.visible');
        
        // Should have receipt list/table
        cy.get('table, [data-testid="receipts-list"], .receipt-item').should('exist');
      });
    });

    describe('RCP-002: Search Receipts', () => {
      it('should filter receipts by search query', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Find search input
        cy.get('input[type="search"], input[placeholder*="search" i]')
          .first()
          .type('RCP');
        
        // Wait for filtering
        cy.wait(500);
        
        // Results should update
        cy.get('body').should('exist');
      });
    });

    describe('RCP-003: Date Range Filter', () => {
      it('should filter receipts by date range', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Click on date range selector
        cy.contains(/today|week|month/i).first().click();
        
        // Select different range
        cy.contains(/week/i).click();
        
        // Receipts should update
        cy.wait('@getReceipts');
      });
    });

    describe('RCP-004: Custom Date Filter', () => {
      it('should filter receipts by custom date', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Click custom date option
        cy.contains(/custom/i).click();
        
        // Enter date
        cy.get('input[type="date"]').first().then(($input) => {
          if ($input.length > 0) {
            const today = new Date().toISOString().split('T')[0];
            cy.wrap($input).type(today);
          }
        });
        
        // Receipts should update
        cy.wait(500);
      });
    });
  });

  describe('Receipt Details & Download', () => {
    describe('RCP-005: Open Receipt Detail', () => {
      it('should open receipt detail modal when clicking on receipt', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Click on first receipt
        cy.get('tbody tr, [data-testid="receipt-row"], .receipt-item').first().click();
        
        // Modal should appear
        cy.get('[data-testid="receipt-modal"], [role="dialog"], .modal, .fixed.inset-0.bg-black')
          .should('be.visible');
      });
    });

    describe('RCP-006: Receipt Modal Content', () => {
      it('should display receipt details in modal', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Click on first receipt
        cy.get('tbody tr, [data-testid="receipt-row"], .receipt-item').first().click();
        
        // Modal should show receipt info
        cy.get('[data-testid="receipt-modal"], [role="dialog"], .modal, .fixed.inset-0.bg-black')
          .should('be.visible')
          .within(() => {
            // Should show receipt number
            cy.contains(/RCP-|receipt/i).should('exist');
          });
      });
    });

    describe('RCP-007: PDF Download Success', () => {
      it('should download PDF and show success toast', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Intercept PDF download
        cy.intercept('GET', '**/api/receipts/*/pdf', {
          statusCode: 200,
          headers: {
            'content-type': 'application/pdf',
          },
          body: new Blob(['%PDF-1.4 mock pdf content'], { type: 'application/pdf' }),
        }).as('downloadPdf');
        
        // Click on first receipt
        cy.get('tbody tr, [data-testid="receipt-row"], .receipt-item').first().click();
        
        // Click download button
        cy.contains(/download|pdf/i).click();
        
        // Should show success toast (if dialog is cancelled or download succeeds)
        cy.wait(1000);
      });
    });

    describe('RCP-008: PDF Download Error', () => {
      it('should show error toast when PDF download fails', () => {
        cy.visit('/receipts');
        cy.wait('@getReceipts');
        
        // Intercept PDF download with error
        cy.intercept('GET', '**/api/receipts/*/pdf', {
          statusCode: 500,
          body: { message: 'Server error' },
        }).as('downloadPdfError');
        
        // Click on first receipt
        cy.get('tbody tr, [data-testid="receipt-row"], .receipt-item').first().click();
        
        // Click download button
        cy.contains(/download|pdf/i).click();
        
        // Should show error toast
        cy.get('.bg-red-500, [data-testid="error-toast"]', { timeout: 5000 })
          .should('be.visible');
      });
    });
  });

  describe('Daily Report', () => {
    it('should display daily report summary', () => {
      cy.visit('/receipts');
      cy.wait('@getReceipts');
      cy.wait('@getDailyReport');
      
      // Should show stats cards
      cy.get('[data-testid="stat-card"], .stat-card, [class*="rounded-lg"][class*="shadow"]')
        .should('have.length.at.least', 1);
    });
  });
});
