import api from './client';
import { Product, Category, CreateProductRequest, UpdateProductRequest } from '../types';

export const productsApi = {
  // Get all products
  getProducts: async (): Promise<Product[]> => {
    const response = await api.get('/api/products');
    return response.data;
  },

  // Get products by category
  getProductsByCategory: async (categoryId: number): Promise<Product[]> => {
    const response = await api.get(`/api/products/category/${categoryId}`);
    return response.data;
  },

  // Get single product
  getProduct: async (id: number): Promise<Product> => {
    const response = await api.get(`/api/products/${id}`);
    return response.data;
  },

  // Create product
  createProduct: async (product: CreateProductRequest): Promise<Product> => {
    const response = await api.post('/api/products', product);
    return response.data;
  },

  // Update product
  updateProduct: async (id: number, product: UpdateProductRequest): Promise<Product> => {
    const response = await api.put(`/api/products/${id}`, product);
    return response.data;
  },

  // Toggle availability
  toggleAvailability: async (id: number): Promise<Product> => {
    const response = await api.put(`/api/products/${id}/availability`);
    return response.data;
  },

  // Delete product
  deleteProduct: async (id: number): Promise<void> => {
    await api.delete(`/api/products/${id}`);
  },

  // Categories
  getCategories: async (): Promise<Category[]> => {
    const response = await api.get('/api/categories');
    return response.data;
  },

  createCategory: async (name: string): Promise<Category> => {
    const response = await api.post('/api/categories', { name });
    return response.data;
  },

  updateCategory: async (id: number, name: string): Promise<Category> => {
    const response = await api.put(`/api/categories/${id}`, { name });
    return response.data;
  },

  deleteCategory: async (id: number): Promise<void> => {
    await api.delete(`/api/categories/${id}`);
  },
};
