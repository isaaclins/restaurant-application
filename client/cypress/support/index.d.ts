/// <reference types="cypress" />

// Custom command declarations
declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Login via API - gets real tokens from backend, falls back to mock if unavailable
       * @param email - The email to login with
       * @param password - The password to login with
       */
      login(email?: string, password?: string): Chainable<void>;

      /**
       * Login as admin user
       */
      loginAsAdmin(): Chainable<void>;

      /**
       * Login as staff user
       */
      loginAsStaff(): Chainable<void>;

      /**
       * Login via actual API call (for integration tests)
       * Returns null if backend is unavailable
       */
      loginViaApi(email?: string, password?: string): Chainable<any>;

      /**
       * Logout from the application
       */
      logout(): Chainable<void>;

      /**
       * Create an order via API
       * @param orderData - The order data to create
       */
      createOrder(orderData?: Partial<OrderData>): Chainable<Cypress.Response<any>>;

      /**
       * Create a product via API
       * @param productData - The product data to create
       */
      createProduct(productData: Partial<ProductData>): Chainable<Cypress.Response<any>>;

      /**
       * Create a category via API
       * @param categoryData - The category data to create
       */
      createCategory(categoryData: Partial<CategoryData>): Chainable<Cypress.Response<any>>;

      /**
       * Clear all test data
       */
      clearTestData(): Chainable<void>;

      /**
       * Populate demo data via Settings page
       */
      populateDemoData(): Chainable<void>;

      /**
       * Wait for API call to complete
       * @param alias - The alias of the intercepted call
       */
      waitForApi(alias: string): Chainable<any>;

      /**
       * Check if toast with message is visible
       * @param message - The toast message to check
       * @param type - The toast type (success or error)
       */
      checkToast(message: string, type?: 'success' | 'error'): Chainable<void>;

      /**
       * Assert error message is shown
       */
      shouldShowError(message: string): Chainable<JQuery<HTMLElement>>;

      /**
       * Assert success message is shown
       */
      shouldShowSuccess(message: string): Chainable<JQuery<HTMLElement>>;

      /**
       * Assert current page URL contains path
       */
      shouldBeOnPage(path: string): Chainable<string>;

      /**
       * Test SQL injection on a form field
       */
      testSQLInjection(selector: string, callback: (payload: string) => void): Chainable<void>;

      /**
       * Test XSS on a form field
       */
      testXSS(selector: string): Chainable<void>;

      /**
       * Verify no malicious scripts have executed
       */
      checkNoScriptExecution(): Chainable<void>;

      /**
       * Set up default API mocks for authenticated pages
       */
      setupApiMocks(): Chainable<void>;

      /**
       * Visit a protected page with auth and API mocks in one call
       */
      visitAuthenticated(path: string, apiMocks?: {
        products?: any[];
        categories?: any[];
        orders?: any[];
        receipts?: any[];
        settings?: any;
      }): Chainable<void>;
    }
  }
}

interface OrderData {
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  orderType: 'PICKUP' | 'DELIVERY' | 'DINE_IN';
  paymentMethod: string;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
    notes?: string;
  }>;
  totalPrice: number;
  estimatedDelivery?: string;
  deliveryStreet?: string;
  deliveryCity?: string;
  deliveryPostalCode?: string;
  notes?: string;
}

interface ProductData {
  name: string;
  description?: string;
  price: number;
  categoryId: number;
  isAvailable?: boolean;
  preparationTime?: number;
  imageUrl?: string;
}

interface CategoryData {
  name: string;
  displayOrder?: number;
  isActive?: boolean;
}

export {};
