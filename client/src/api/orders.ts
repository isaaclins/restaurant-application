import api from './client';
import { Order, OrderStatus } from '../types';

const normalizeOrder = (order: any): Order => {
  const deliveryStreet = order.deliveryStreet ?? order.deliveryAddress?.street;
  const deliveryCity = order.deliveryCity ?? order.deliveryAddress?.city;
  const deliveryPostalCode = order.deliveryPostalCode ?? order.deliveryAddress?.postalCode;
  return {
    ...order,
    deliveryStreet,
    deliveryCity,
    deliveryPostalCode,
    customerAddress:
      order.customerAddress ??
      (deliveryStreet ? `${deliveryStreet}${deliveryPostalCode ? `, ${deliveryPostalCode}` : ''}${deliveryCity ? ` ${deliveryCity}` : ''}` : undefined),
  };
};

export const ordersApi = {
  // Get all orders with optional filters
  getOrders: async (params?: { status?: OrderStatus; date?: string }): Promise<Order[]> => {
    const response = await api.get('/api/orders', { params });
    return response.data.map(normalizeOrder);
  },

  // Get single order by ID
  getOrder: async (id: number): Promise<Order> => {
    const response = await api.get(`/api/orders/${id}`);
    return normalizeOrder(response.data);
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
  createManualOrder: async (order: any): Promise<Order> => {
    // Manual (WALK_IN) orders are created via the standard /api/orders endpoint
    const response = await api.post('/api/orders', order);
    return response.data;
  },
};
