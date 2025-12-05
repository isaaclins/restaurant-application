/// <reference types="cypress" />

/**
 * Navigation & Layout Tests
 * Tests for sidebar navigation and general layout
 */
describe('Navigation & Layout', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('NAV-001: Sidebar Menu Items', () => {
    it('should display all navigation menu items', () => {
      cy.visit('/');
      
      // Check all main navigation items exist
      cy.get('nav, aside, [data-testid="sidebar"]').within(() => {
        cy.contains('KDS').should('be.visible');
        cy.contains('Orders').should('be.visible');
        cy.contains('Products').should('be.visible');
        cy.contains('Receipts').should('be.visible');
        cy.contains('Statistics').should('be.visible');
        cy.contains('Settings').should('be.visible');
      });
    });
  });

  describe('NAV-002: Navigate to KDS', () => {
    it('should navigate to KDS page when clicking KDS link', () => {
      cy.visit('/');
      cy.contains('KDS').click();
      cy.url().should('include', '/kds');
      cy.contains('Kitchen Display').should('be.visible');
    });
  });

  describe('NAV-003: Navigate to Orders', () => {
    it('should navigate to Orders page when clicking Orders link', () => {
      cy.visit('/');
      cy.contains('Orders').click();
      cy.url().should('include', '/orders');
      cy.contains('Orders').should('be.visible');
    });
  });

  describe('NAV-004: Navigate to Products', () => {
    it('should navigate to Products page when clicking Products link', () => {
      cy.visit('/');
      cy.contains('Products').click();
      cy.url().should('include', '/products');
      // Should show products or categories tab
      cy.get('body').should('contain.text', 'Products').or('contain.text', 'Categories');
    });
  });

  describe('NAV-005: Active Menu Highlighting', () => {
    it('should highlight the active menu item', () => {
      cy.visit('/kds');
      
      // The KDS link should have active styling (e.g., different background)
      cy.contains('KDS').parent().should('have.class', 'bg-orange-50')
        .or('have.class', 'bg-orange-100')
        .or('have.class', 'active');
    });
  });

  describe('Layout Responsiveness', () => {
    it('should display properly on different screen sizes', () => {
      cy.visit('/');
      
      // Desktop
      cy.viewport(1280, 720);
      cy.get('nav, aside, [data-testid="sidebar"]').should('be.visible');
      
      // Tablet
      cy.viewport(768, 1024);
      cy.get('body').should('be.visible');
      
      // Mobile (sidebar might be collapsed)
      cy.viewport(375, 667);
      cy.get('body').should('be.visible');
    });
  });
});
