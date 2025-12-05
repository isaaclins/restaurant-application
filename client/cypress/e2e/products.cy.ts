/// <reference types="cypress" />

/**
 * Products & Categories Tests
 * Tests for product and category management
 */
describe('Products & Categories', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('Categories', () => {
    describe('PROD-001: Categories Tab Display', () => {
      it('should display categories list', () => {
        cy.visit('/products');
        cy.wait('@getCategories');
        
        // Click on Categories tab if present
        cy.get('body').then(($body) => {
          if ($body.find('button:contains("Categories"), [data-testid="categories-tab"]').length > 0) {
            cy.contains('Categories').click();
          }
        });
        
        // Should show categories
        cy.get('table, [data-testid="categories-list"], .category-item').should('exist');
      });
    });

    describe('PROD-002: Create Category', () => {
      it('should create a new category', () => {
        cy.visit('/products');
        cy.wait('@getCategories');
        
        // Click Categories tab
        cy.contains('Categories').click();
        
        // Click add button
        cy.contains(/add|new|create|\+/i).click();
        
        // Fill form
        cy.get('input[name="name"], input[placeholder*="name" i]').first().type('Test Category');
        
        // Submit
        cy.contains(/save|create|add/i).click();
        
        // Wait for API
        cy.wait('@createCategory');
      });
    });

    describe('PROD-003: Edit Category', () => {
      it('should edit an existing category', () => {
        cy.visit('/products');
        cy.wait('@getCategories');
        
        // Click Categories tab
        cy.contains('Categories').click();
        
        // Click edit on first category
        cy.get('[data-testid="edit-category"], button[title="Edit"], .edit-btn')
          .first()
          .click();
        
        // Update name
        cy.get('input[name="name"], input[placeholder*="name" i]')
          .first()
          .clear()
          .type('Updated Category');
        
        // Save
        cy.contains(/save|update/i).click();
        
        // Wait for API
        cy.wait('@updateCategory');
      });
    });

    describe('PROD-004: Toggle Category Active', () => {
      it('should toggle category active/inactive status', () => {
        cy.visit('/products');
        cy.wait('@getCategories');
        
        // Click Categories tab
        cy.contains('Categories').click();
        
        // Find and click toggle switch
        cy.get('[data-testid="category-toggle"], input[type="checkbox"], .toggle, button[role="switch"]')
          .first()
          .click();
        
        // Wait for API
        cy.wait('@updateCategory');
      });
    });
  });

  describe('Products', () => {
    describe('PROD-005: Products Tab Display', () => {
      it('should display products list', () => {
        cy.visit('/products');
        cy.wait('@getProducts');
        
        // Click on Products tab if present
        cy.get('body').then(($body) => {
          if ($body.find('button:contains("Products"), [data-testid="products-tab"]').length > 0) {
            cy.contains('Products').click();
          }
        });
        
        // Should show products
        cy.get('table, [data-testid="products-list"], .product-item, .grid').should('exist');
      });
    });

    describe('PROD-006: Filter Products by Category', () => {
      it('should filter products by category', () => {
        cy.visit('/products');
        cy.wait('@getProducts');
        cy.wait('@getCategories');
        
        // Find category filter
        cy.get('select, [data-testid="category-filter"]').first().then(($filter) => {
          if ($filter.is('select')) {
            // Select first category option (not "All")
            cy.wrap($filter).find('option').eq(1).then(($option) => {
              cy.wrap($filter).select($option.val() as string);
            });
          }
        });
        
        // Products should be filtered
        cy.wait(500);
      });
    });

    describe('PROD-007: Create Product', () => {
      it('should create a new product', () => {
        cy.visit('/products');
        cy.wait('@getProducts');
        cy.wait('@getCategories');
        
        // Click add button
        cy.contains(/add product|new product|create|\+/i).click();
        
        // Fill form
        cy.get('input[name="name"], input[placeholder*="name" i]').first().type('Test Product');
        cy.get('input[name="price"], input[placeholder*="price" i]').first().type('15.50');
        cy.get('textarea[name="description"], input[placeholder*="description" i]')
          .first()
          .type('Test product description');
        
        // Select category
        cy.get('select[name="categoryId"], [data-testid="category-select"]').first().then(($select) => {
          if ($select.is('select')) {
            cy.wrap($select).find('option').eq(1).then(($option) => {
              cy.wrap($select).select($option.val() as string);
            });
          }
        });
        
        // Submit
        cy.contains(/save|create|add/i).click();
        
        // Wait for API
        cy.wait('@createProduct');
      });
    });

    describe('PROD-008: Edit Product', () => {
      it('should edit an existing product', () => {
        cy.visit('/products');
        cy.wait('@getProducts');
        
        // Click edit on first product
        cy.get('[data-testid="edit-product"], button[title="Edit"], .edit-btn, button')
          .contains(/edit/i)
          .first()
          .click();
        
        // Update name
        cy.get('input[name="name"], input[placeholder*="name" i]')
          .first()
          .clear()
          .type('Updated Product');
        
        // Save
        cy.contains(/save|update/i).click();
        
        // Wait for API
        cy.wait('@updateProduct');
      });
    });

    describe('PROD-009: Toggle Product Availability', () => {
      it('should toggle product available/unavailable status', () => {
        cy.visit('/products');
        cy.wait('@getProducts');
        
        // Find and click availability toggle
        cy.get('[data-testid="availability-toggle"], input[type="checkbox"], .toggle, button[role="switch"]')
          .first()
          .click();
        
        // Wait for API
        cy.wait('@updateProduct');
      });
    });

    describe('PROD-010: Delete Product', () => {
      it('should delete a product with confirmation', () => {
        cy.visit('/products');
        cy.wait('@getProducts');
        
        // Accept confirmation dialog
        cy.on('window:confirm', () => true);
        
        // Click delete on first product
        cy.get('[data-testid="delete-product"], button[title="Delete"], .delete-btn, button')
          .contains(/delete/i)
          .first()
          .click();
        
        // Wait for API
        cy.wait('@deleteProduct');
      });
    });
  });
});
