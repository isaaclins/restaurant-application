import api from './client';
import { Product, Category, CreateProductRequest, UpdateProductRequest } from '../types';

export interface CreateCategoryRequest {
  name: string;
  displayOrder?: number;
  isActive?: boolean;
}

export interface UpdateCategoryRequest {
  name?: string;
  displayOrder?: number;
  isActive?: boolean;
}

export const productsApi = {
  // Normalize backend product shape to frontend expectations
  normalize(product: any): Product {
    const available = product.available ?? product.isAvailable ?? true;
    const active = product.active ?? product.isActive ?? true;
    return {
      ...product,
      available,
      active,
      isAvailable: available,
      isActive: active,
    };
  },

  // Get all products
  getProducts: async (): Promise<Product[]> => {
    const response = await api.get('/api/products');
    return response.data.map(productsApi.normalize);
  },

  // Get products by category
  getProductsByCategory: async (categoryId: number): Promise<Product[]> => {
    const response = await api.get(`/api/products/category/${categoryId}`);
    return response.data.map(productsApi.normalize);
  },

  // Get single product
  getProduct: async (id: number): Promise<Product> => {
    const response = await api.get(`/api/products/${id}`);
    return productsApi.normalize(response.data);
  },

  // Create product
  createProduct: async (product: CreateProductRequest): Promise<Product> => {
    const response = await api.post('/api/products', product);
    return productsApi.normalize(response.data);
  },

  // Update product
  updateProduct: async (id: number, product: UpdateProductRequest): Promise<Product> => {
    const response = await api.put(`/api/products/${id}`, product);
    return productsApi.normalize(response.data);
  },

  // Toggle availability
  toggleAvailability: async (id: number, available: boolean): Promise<Product> => {
    const response = await api.put(`/api/products/${id}/availability`, { available });
    return productsApi.normalize(response.data);
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

  createCategory: async (data: CreateCategoryRequest): Promise<Category> => {
    const response = await api.post('/api/categories', data);
    return response.data;
  },

  updateCategory: async (id: number, data: UpdateCategoryRequest): Promise<Category> => {
    const response = await api.put(`/api/categories/${id}`, data);
    return response.data;
  },

  deleteCategory: async (id: number): Promise<void> => {
    await api.delete(`/api/categories/${id}`);
  },

  toggleCategoryActive: async (id: number): Promise<Category> => {
    const response = await api.patch(`/api/categories/${id}/toggle-active`);
    return response.data;
  },

  reorderCategories: async (categoryIds: number[]): Promise<Category[]> => {
    const response = await api.put('/api/categories/reorder', categoryIds);
    return response.data;
  },
};
