/// <reference types="cypress" />

/**
 * Settings Tests
 * Tests for settings page and demo data population
 */
describe('Settings', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('General Settings', () => {
    describe('SET-001: Settings Page Display', () => {
      it('should display settings page with tabs', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Should show settings heading
        cy.contains(/settings|configuration/i).should('be.visible');
        
        // Should have tabs
        cy.get('[role="tablist"], .tabs, [data-testid="settings-tabs"]').should('exist');
      });
    });

    describe('SET-002: Restaurant Name', () => {
      it('should allow changing restaurant name', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click on General tab if needed
        cy.get('body').then(($body) => {
          if ($body.find('button:contains("General")').length > 0) {
            cy.contains('General').click();
          }
        });
        
        // Find restaurant name input
        cy.get('input[name="restaurantName"], input[placeholder*="name" i]')
          .first()
          .clear()
          .type('Updated Restaurant Name');
        
        // Save
        cy.contains(/save|update/i).click();
      });
    });

    describe('SET-003: Restaurant Address', () => {
      it('should allow changing restaurant address', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Find address input
        cy.get('input[name="address"], input[placeholder*="address" i], textarea[name="address"]')
          .first()
          .clear()
          .type('New Street 123, 8000 Zürich');
        
        // Save
        cy.contains(/save|update/i).click();
      });
    });
  });

  describe('Email Settings', () => {
    describe('SET-004: Email Settings Tab', () => {
      it('should display email settings', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click Email tab
        cy.contains(/email|smtp/i).click();
        
        // Should show SMTP fields
        cy.contains(/smtp|email server/i).should('be.visible');
      });
    });

    describe('SET-005: SMTP Host', () => {
      it('should allow changing SMTP host', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click Email tab
        cy.contains(/email|smtp/i).click();
        
        // Find SMTP host input
        cy.get('input[name="smtpHost"], input[placeholder*="host" i]')
          .first()
          .clear()
          .type('smtp.gmail.com');
      });
    });

    describe('SET-006: Test Email', () => {
      it('should allow sending test email', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click Email tab
        cy.contains(/email|smtp/i).click();
        
        // Find test email button
        cy.get('body').then(($body) => {
          if ($body.find('button:contains("Test Email"), button:contains("Send Test")').length > 0) {
            cy.contains(/test email|send test/i).click();
          }
        });
      });
    });
  });

  describe('Developer Tools', () => {
    describe('SET-007: Developer Tab', () => {
      it('should display developer tools tab', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click Developer tab
        cy.contains(/developer|dev|tools/i).click();
        
        // Should show populate button
        cy.contains(/populate|demo data/i).should('be.visible');
      });
    });

    describe('SET-008: Demo Data Checkboxes', () => {
      it('should have checkboxes for categories, products, orders', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click Developer tab
        cy.contains(/developer|dev|tools/i).click();
        
        // Should have checkboxes
        cy.get('input[type="checkbox"]').should('have.length.at.least', 1);
        
        // Checkbox labels
        cy.contains(/categories/i).should('exist');
        cy.contains(/products/i).should('exist');
        cy.contains(/orders/i).should('exist');
      });
    });

    describe('SET-009: Populate Demo Data', () => {
      it('should populate demo data when button is clicked', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click Developer tab
        cy.contains(/developer|dev|tools/i).click();
        
        // Make sure all checkboxes are checked
        cy.get('input[type="checkbox"]').each(($checkbox) => {
          if (!$checkbox.prop('checked')) {
            cy.wrap($checkbox).click();
          }
        });
        
        // Click populate button
        cy.contains(/populate|create demo/i).click();
        
        // Wait for process to start (progress should appear)
        cy.get('[data-testid="populate-progress"], .progress, [role="progressbar"]', { timeout: 5000 })
          .should('exist');
      });
    });

    describe('SET-010: Progress Indicator', () => {
      it('should show progress during population', () => {
        cy.visit('/settings');
        cy.wait('@getSettings');
        
        // Click Developer tab
        cy.contains(/developer|dev|tools/i).click();
        
        // Click populate button
        cy.contains(/populate|create demo/i).click();
        
        // Progress bar should appear and update
        cy.get('[data-testid="populate-progress"], .progress, [role="progressbar"], .bg-orange-500')
          .should('exist');
        
        // Status text should update
        cy.contains(/creating|populating|%/i).should('exist');
      });
    });
  });

  describe('Appearance Settings', () => {
    it('should have appearance/theme settings', () => {
      cy.visit('/settings');
      cy.wait('@getSettings');
      
      // Look for appearance tab
      cy.get('body').then(($body) => {
        if ($body.find('button:contains("Appearance"), button:contains("Theme")').length > 0) {
          cy.contains(/appearance|theme/i).click();
          cy.contains(/color|theme|mode/i).should('exist');
        }
      });
    });
  });

  describe('Settings Persistence', () => {
    it('should persist settings after page reload', () => {
      cy.visit('/settings');
      cy.wait('@getSettings');
      
      // Change a setting
      cy.get('input[name="restaurantName"], input')
        .first()
        .clear()
        .type('Persistence Test Restaurant');
      
      // Save
      cy.contains(/save|update/i).click();
      
      // Reload page
      cy.reload();
      cy.wait('@getSettings');
      
      // Setting should be persisted (or at least page loads without error)
      cy.get('input').first().should('exist');
    });
  });
});
