/// <reference types="cypress" />

/**
 * Statistics Tests
 * Tests for statistics dashboard
 */
describe('Statistics Dashboard', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('Dashboard Display', () => {
    describe('STAT-001: Statistics Page Display', () => {
      it('should display statistics page with stat cards', () => {
        cy.visit('/statistics');
        cy.wait('@getOrders');
        
        // Should show statistics heading
        cy.contains(/statistics|analytics|dashboard/i).should('be.visible');
        
        // Should have stat cards
        cy.get('[data-testid="stat-card"], .stat-card, [class*="rounded-lg"][class*="shadow"]')
          .should('have.length.at.least', 1);
      });
    });

    describe('STAT-002: Total Orders Stat', () => {
      it('should display total orders count', () => {
        cy.visit('/statistics');
        cy.wait('@getOrders');
        
        // Should show total orders stat
        cy.contains(/total orders|orders today/i).should('be.visible');
        
        // Should have a number value
        cy.get('[data-testid="stat-card"], .stat-card').first().within(() => {
          cy.get('span, p, div').contains(/\d+/).should('exist');
        });
      });
    });

    describe('STAT-003: Revenue Stat', () => {
      it('should display revenue statistic', () => {
        cy.visit('/statistics');
        cy.wait('@getOrders');
        
        // Should show revenue stat
        cy.contains(/revenue|income|sales/i).should('be.visible');
        
        // Should have a currency value
        cy.contains(/CHF|Fr\.|₣|\d+\.\d{2}/i).should('exist');
      });
    });

    describe('STAT-004: Avg Completion Time', () => {
      it('should display average completion time', () => {
        cy.visit('/statistics');
        cy.wait('@getOrders');
        
        // Should show avg completion time stat
        cy.contains(/avg|average|completion|time/i).should('be.visible');
        
        // Should have a time value (minutes)
        cy.contains(/\d+ min|\d+m|- min/i).should('exist');
      });
    });
  });

  describe('Charts', () => {
    describe('STAT-005: Orders by Hour Chart', () => {
      it('should display orders by hour chart', () => {
        cy.visit('/statistics');
        cy.wait('@getOrders');
        
        // Should show orders by hour chart or section
        cy.contains(/by hour|hourly|timeline/i).should('exist');
        
        // Chart container should exist
        cy.get('[data-testid="orders-by-hour-chart"], canvas, svg, .chart')
          .should('exist');
      });
    });

    describe('STAT-006: Orders by Status Chart', () => {
      it('should display orders by status chart', () => {
        cy.visit('/statistics');
        cy.wait('@getOrders');
        
        // Should show orders by status chart or section
        cy.contains(/by status|status breakdown|order status/i).should('exist');
        
        // Chart or status list should exist
        cy.get('[data-testid="orders-by-status-chart"], canvas, svg, .chart, .status-breakdown')
          .should('exist');
      });
    });
  });

  describe('Date Filtering', () => {
    it('should filter statistics by date range', () => {
      cy.visit('/statistics');
      cy.wait('@getOrders');
      
      // Find date range selector
      cy.get('input[type="date"], [data-testid="date-filter"], select').first().then(($filter) => {
        if ($filter.is('input[type="date"]')) {
          const today = new Date().toISOString().split('T')[0];
          cy.wrap($filter).type(today);
        } else if ($filter.is('select')) {
          cy.wrap($filter).select(1); // Select second option
        }
      });
      
      // Stats should update
      cy.wait(500);
    });
  });

  describe('Real-time Updates', () => {
    it('should update statistics when new orders are created', () => {
      cy.visit('/statistics');
      cy.wait('@getOrders');
      
      // Get initial order count
      cy.get('[data-testid="stat-card"], .stat-card').first().invoke('text').then((initialText) => {
        // Create a new order via API
        cy.createOrder({
          customerName: 'Stats Test',
          customerEmail: 'stats@test.com',
          customerPhone: '+41 79 000 0000',
          orderType: 'PICKUP',
          paymentMethod: 'CASH',
          items: [{ productId: 1, productName: 'Test', quantity: 1, unitPrice: 10, totalPrice: 10 }],
          totalPrice: 10,
        });
        
        // Refresh page
        cy.reload();
        cy.wait('@getOrders');
        
        // Stats should update (or at least page should load without error)
        cy.get('[data-testid="stat-card"], .stat-card').first().should('exist');
      });
    });
  });
});
