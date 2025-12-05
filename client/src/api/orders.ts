import api from './client';
import { Order, OrderStatus } from '../types';

export const ordersApi = {
  // Get all orders with optional filters
  getOrders: async (params?: { status?: OrderStatus; date?: string }): Promise<Order[]> => {
    const response = await api.get('/api/orders', { params });
    return response.data;
  },

  // Get single order by ID
  getOrder: async (id: number): Promise<Order> => {
    const response = await api.get(`/api/orders/${id}`);
    return response.data;
  },

  // Update order status
  updateStatus: async (id: number, status: OrderStatus): Promise<Order> => {
    const response = await api.put(`/api/orders/${id}/status`, { status });
    return response.data;
  },

  // Delete order
  deleteOrder: async (id: number): Promise<void> => {
    await api.delete(`/api/orders/${id}`);
  },

  // Create manual order (walk-in customer)
  createManualOrder: async (order: Partial<Order>): Promise<Order> => {
    const response = await api.post('/api/orders/manual', order);
    return response.data;
  },
};
