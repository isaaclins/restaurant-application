/// <reference types="cypress" />

// Custom command declarations
declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Login to the application
       * @param username - The username to login with
       * @param password - The password to login with
       */
      login(username?: string, password?: string): Chainable<void>;

      /**
       * Logout from the application
       */
      logout(): Chainable<void>;

      /**
       * Create an order via API
       * @param orderData - The order data to create
       */
      createOrder(orderData: Partial<OrderData>): Chainable<any>;

      /**
       * Create a product via API
       * @param productData - The product data to create
       */
      createProduct(productData: Partial<ProductData>): Chainable<any>;

      /**
       * Create a category via API
       * @param categoryData - The category data to create
       */
      createCategory(categoryData: Partial<CategoryData>): Chainable<any>;

      /**
       * Populate demo data via Settings page
       */
      populateDemoData(): Chainable<void>;

      /**
       * Clear all data (orders, products, categories)
       */
      clearAllData(): Chainable<void>;

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
    }
  }
}

interface OrderData {
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  orderType: 'PICKUP' | 'DELIVERY';
  paymentMethod: string;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
  }>;
  totalPrice: number;
  estimatedDelivery?: string;
  deliveryStreet?: string;
  deliveryCity?: string;
  deliveryPostalCode?: string;
}

interface ProductData {
  name: string;
  description: string;
  price: number;
  categoryId: number;
  available: boolean;
  preparationTime?: number;
}

interface CategoryData {
  name: string;
  displayOrder: number;
  isActive: boolean;
}

export {};
