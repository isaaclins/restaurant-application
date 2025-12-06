import api from './client';
import type { Product, Category } from '../types';

export const productsApi = {
  // Get all products (optionally filter by availability)
  getProducts: async (available?: boolean): Promise<Product[]> => {
    const params = available !== undefined ? { available } : {};
    const response = await api.get('/api/products', { params });
    return response.data.data || response.data;
  },

  // Get single product by ID
  getProduct: async (id: number): Promise<Product> => {
    const response = await api.get(`/api/products/${id}`);
    return response.data.data || response.data;
  },

  // Get products by category
  getProductsByCategory: async (category: string): Promise<Product[]> => {
    const response = await api.get(`/api/products/category/${category}`);
    return response.data.data || response.data;
  },

  // Get all categories
  getCategories: async (): Promise<Category[]> => {
    const response = await api.get('/api/categories');
    return response.data.data || response.data;
  },
};
