import api from './client';
import type {
  RestaurantSettings,
  OpeningHoursEntry,
  DeliveryArea,
} from '../types/settings';

export const settingsApi = {
  getSettings: async (): Promise<RestaurantSettings> => {
    const response = await api.get('/api/settings');
    return response.data.data || response.data;
  },
};
