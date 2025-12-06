import api from './client';
import type { Customer, LoginRequest, RegisterRequest, AuthResponse } from '../types';

export const authApi = {
  // Register new customer
  register: async (data: RegisterRequest): Promise<Customer> => {
    const response = await api.post('/api/auth/register', data);
    return response.data.data || response.data;
  },

  // Customer login
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/login/customer', data);
    return response.data.data || response.data;
  },

  // Logout
  logout: async (): Promise<void> => {
    await api.post('/api/auth/logout');
  },

  // Get current customer profile
  getProfile: async (): Promise<Customer> => {
    const response = await api.get('/api/customers/me');
    return response.data.data || response.data;
  },

  // Update customer profile
  updateProfile: async (data: Partial<Customer>): Promise<Customer> => {
    const response = await api.put('/api/customers/me', data);
    return response.data.data || response.data;
  },

  // Change password
  changePassword: async (currentPassword: string, newPassword: string): Promise<void> => {
    await api.put('/api/customers/me/password', { currentPassword, newPassword });
  },

  // Add new address
  addAddress: async (address: {
    street: string;
    city: string;
    postalCode: string;
    isDefault?: boolean;
  }): Promise<Customer> => {
    const response = await api.post('/api/customers/me/addresses', address);
    return response.data.data || response.data;
  },
};
