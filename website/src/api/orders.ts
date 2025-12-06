import api from './client';
import type { Order, CreateOrderRequest } from '../types';

export const ordersApi = {
  // Create a new order
  createOrder: async (order: CreateOrderRequest): Promise<Order> => {
    const response = await api.post('/api/orders', order);
    return response.data.data || response.data;
  },

  // Get order by ID (for tracking)
  getOrder: async (id: number): Promise<Order> => {
    const response = await api.get(`/api/orders/${id}`);
    return response.data.data || response.data;
  },

  // Get customer's order history (requires auth)
  getMyOrders: async (): Promise<Order[]> => {
    const response = await api.get('/api/customers/me/orders');
    return response.data.data || response.data;
  },
};
