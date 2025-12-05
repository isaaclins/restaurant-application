import api from './client';
import { RestaurantSettings, OpeningHours, DeliveryArea } from '../types';

export const settingsApi = {
  // Get restaurant settings
  getSettings: async (): Promise<RestaurantSettings> => {
    const response = await api.get('/api/settings');
    return response.data;
  },

  // Update restaurant settings
  updateSettings: async (settings: Partial<RestaurantSettings>): Promise<RestaurantSettings> => {
    const response = await api.put('/api/settings', settings);
    return response.data;
  },

  // Get open status
  getOpenStatus: async (): Promise<{ isOpen: boolean; nextOpenTime?: string }> => {
    const response = await api.get('/api/settings/status');
    return response.data;
  },

  // Opening Hours
  getOpeningHours: async (): Promise<OpeningHours[]> => {
    const response = await api.get('/api/settings/hours');
    return response.data;
  },

  updateOpeningHours: async (hours: Partial<OpeningHours>): Promise<OpeningHours> => {
    const response = await api.put('/api/settings/hours', hours);
    return response.data;
  },

  // Delivery Areas
  getDeliveryAreas: async (): Promise<DeliveryArea[]> => {
    const response = await api.get('/api/settings/delivery-areas');
    return response.data;
  },

  addDeliveryArea: async (area: Partial<DeliveryArea>): Promise<DeliveryArea> => {
    const response = await api.post('/api/settings/delivery-areas', area);
    return response.data;
  },

  updateDeliveryArea: async (id: number, area: Partial<DeliveryArea>): Promise<DeliveryArea> => {
    const response = await api.put(`/api/settings/delivery-areas/${id}`, area);
    return response.data;
  },

  deleteDeliveryArea: async (id: number): Promise<void> => {
    await api.delete(`/api/settings/delivery-areas/${id}`);
  },
};
