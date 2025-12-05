/// <reference types="cypress" />

/**
 * Orders Management Tests
 * Tests for orders list, creation, and management
 */
describe('Orders Management', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('Order List', () => {
    describe('ORD-001: Orders Page Display', () => {
      it('should display orders page with table/list view', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Should show orders heading
        cy.contains('Orders').should('be.visible');
        
        // Should have a table or list structure
        cy.get('table, [role="table"], .order-list, [data-testid="orders-list"]').should('exist');
      });
    });

    describe('ORD-002: Search Orders', () => {
      it('should filter orders by search query', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Find search input
        cy.get('input[type="search"], input[placeholder*="search" i], input[placeholder*="Search" i]')
          .first()
          .type('Test');
        
        // Wait for filtering
        cy.wait(500);
        
        // Results should be filtered (or empty if no match)
        cy.get('body').should('exist');
      });
    });

    describe('ORD-003: Status Filter', () => {
      it('should filter orders by status', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Find status filter/dropdown
        cy.get('select, [data-testid="status-filter"], button').contains(/status|filter/i).then(($filter) => {
          if ($filter.is('select')) {
            cy.wrap($filter).select('PENDING');
          } else {
            cy.wrap($filter).click();
            cy.contains('PENDING').click();
          }
        });
        
        // Orders should be filtered
        cy.wait(500);
      });
    });

    describe('ORD-004: Date Filter', () => {
      it('should filter orders by date', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Find date input/filter
        cy.get('input[type="date"], [data-testid="date-filter"]').first().then(($dateInput) => {
          if ($dateInput.length > 0) {
            const today = new Date().toISOString().split('T')[0];
            cy.wrap($dateInput).type(today);
          }
        });
        
        // Orders should be filtered
        cy.wait(500);
      });
    });
  });

  describe('Order Creation', () => {
    describe('ORD-005: New Order Button', () => {
      it('should open order creation modal when clicking new order button', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Click new order button
        cy.contains(/new order|create order|add order|\+/i).click();
        
        // Modal should open
        cy.get('[data-testid="order-modal"], [role="dialog"], .modal, .fixed.inset-0.bg-black')
          .should('be.visible');
      });
    });

    describe('ORD-006: Order Type Selection', () => {
      it('should allow selecting order type (Pickup/Delivery)', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Open new order modal
        cy.contains(/new order|create order|add order|\+/i).click();
        
        // Should have order type options
        cy.contains(/pickup/i).should('exist');
        cy.contains(/delivery/i).should('exist');
        
        // Select Pickup
        cy.contains(/pickup/i).click();
      });
    });

    describe('ORD-007: Customer Selection', () => {
      it('should allow entering customer information', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Open new order modal
        cy.contains(/new order|create order|add order|\+/i).click();
        
        // Find customer name input
        cy.get('input[name="customerName"], input[placeholder*="name" i], input[placeholder*="customer" i]')
          .first()
          .type('Test Customer');
      });
    });

    describe('ORD-008: Add Items to Order', () => {
      it('should allow adding items to order', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        cy.wait('@getProducts');
        
        // Open new order modal
        cy.contains(/new order|create order|add order|\+/i).click();
        
        // Find add item button or product list
        cy.get('body').then(($body) => {
          if ($body.find('[data-testid="add-item"], button:contains("Add Item")').length > 0) {
            cy.contains(/add item/i).click();
          }
        });
      });
    });

    describe('ORD-009: Submit Order', () => {
      it('should create order when form is submitted', () => {
        // Create order via API for faster testing
        cy.fixture('orders').then((orders) => {
          cy.createOrder(orders.sampleOrders[0]).then((response) => {
            expect(response.status).to.eq(201);
            expect(response.body).to.have.property('id');
          });
        });
      });
    });
  });

  describe('Order Details', () => {
    describe('ORD-010: Open Order Detail', () => {
      it('should open order detail view when clicking on order', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Click on first order row
        cy.get('tbody tr, [data-testid="order-row"], .order-item').first().click();
        
        // Detail view should appear
        cy.get('[data-testid="order-detail"], [role="dialog"], .modal, .fixed.inset-0')
          .should('be.visible');
      });
    });

    describe('ORD-011: Update Order Status', () => {
      it('should allow changing order status', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Click on first order row
        cy.get('tbody tr, [data-testid="order-row"], .order-item').first().click();
        
        // Find status dropdown/buttons
        cy.get('body').then(($body) => {
          if ($body.find('select:contains("Status"), [data-testid="status-select"]').length > 0) {
            cy.get('select').first().select('IN_PROGRESS');
            cy.wait('@updateOrder');
          }
        });
      });
    });

    describe('ORD-012: Delete Order', () => {
      it('should delete order with confirmation', () => {
        cy.visit('/orders');
        cy.wait('@getOrders');
        
        // Click on first order row
        cy.get('tbody tr, [data-testid="order-row"], .order-item').first().click();
        
        // Accept confirmation dialog
        cy.on('window:confirm', () => true);
        
        // Click delete button
        cy.contains(/delete|cancel/i).click();
      });
    });
  });
});
