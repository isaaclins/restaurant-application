import api from './client';
import type { Cart, AddToCartRequest } from '../types';

export const cartApi = {
  // Get cart (sessionId sent via X-Session-ID header)
  getCart: async (sessionId: string): Promise<Cart> => {
    const response = await api.get('/api/cart', {
      headers: { 'X-Session-ID': sessionId },
    });
    return response.data;
  },

  // Add item to cart (with optional size and notes)
  addItem: async (sessionId: string, item: AddToCartRequest): Promise<Cart> => {
    const response = await api.post('/api/cart/items', item, {
      headers: { 'X-Session-ID': sessionId },
    });
    return response.data;
  },

  // Remove item from cart by itemId
  removeItem: async (sessionId: string, itemId: string): Promise<Cart> => {
    const response = await api.delete(`/api/cart/items/${itemId}`, {
      headers: { 'X-Session-ID': sessionId },
    });
    return response.data;
  },

  // Update item quantity by itemId
  updateItemQuantity: async (
    sessionId: string,
    itemId: string,
    quantity: number
  ): Promise<Cart> => {
    const response = await api.put(
      `/api/cart/items/${itemId}/quantity`,
      null,
      {
        headers: { 'X-Session-ID': sessionId },
        params: { quantity },
      }
    );
    return response.data;
  },

  // Clear entire cart
  clearCart: async (sessionId: string): Promise<void> => {
    await api.delete('/api/cart', {
      headers: { 'X-Session-ID': sessionId },
    });
  },
};
