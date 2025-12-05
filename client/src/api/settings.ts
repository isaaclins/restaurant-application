import api from './client';
import { RestaurantSettings, OpeningHours, DeliveryArea } from '../types';

// Receipt Template Types
export interface ReceiptTemplate {
  id: number;
  language: 'DE' | 'EN' | 'FR' | 'IT';
  isDefault: boolean;
  showLogo: boolean;
  currency: string;
  currencyPosition: 'BEFORE' | 'AFTER';
  labelReceipt: string;
  labelOrderNumber: string;
  labelDate: string;
  labelCustomer: string;
  labelProduct: string;
  labelQuantity: string;
  labelUnitPrice: string;
  labelTotal: string;
  labelSubtotal: string;
  labelVat: string;
  labelDeliveryFee: string;
  labelDiscount: string;
  labelPaymentMethod: string;
  thankYouMessage: string;
  headerText?: string;
  footerText?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Language {
  code: string;
  nativeName: string;
  englishName: string;
}

// Email Template Types
export interface EmailTemplate {
  id: number;
  type: string;
  language: string;
  name: string;
  subject: string;
  htmlTemplate: string;
  textTemplate?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Notification Types
export interface NotificationStats {
  sent: number;
  failed: number;
  pending: number;
  byType: Array<[string, number]>;
}

// SMTP Configuration Types
export interface SmtpConfig {
  host: string;
  port: number;
  username: string;
  password?: string;
  fromEmail: string;
  fromName: string;
  useSsl: boolean;
  useTls: boolean;
}

// Test Email Request
export interface TestEmailRequest {
  templateId: number;
  recipientEmail: string;
  templateVariables?: Record<string, string>;
}

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

  // Receipt Templates
  getLanguages: async (): Promise<Language[]> => {
    const response = await api.get('/api/receipt-templates/languages');
    return response.data;
  },

  getReceiptTemplates: async (): Promise<ReceiptTemplate[]> => {
    const response = await api.get('/api/receipt-templates');
    return response.data;
  },

  getReceiptTemplate: async (id: number): Promise<ReceiptTemplate> => {
    const response = await api.get(`/api/receipt-templates/${id}`);
    return response.data;
  },

  getReceiptTemplateByLanguage: async (language: string): Promise<ReceiptTemplate> => {
    const response = await api.get(`/api/receipt-templates/language/${language}`);
    return response.data;
  },

  getDefaultReceiptTemplate: async (): Promise<ReceiptTemplate> => {
    const response = await api.get('/api/receipt-templates/default');
    return response.data;
  },

  updateReceiptTemplate: async (id: number, template: Partial<ReceiptTemplate>): Promise<ReceiptTemplate> => {
    const response = await api.put(`/api/receipt-templates/${id}`, template);
    return response.data;
  },

  resetReceiptTemplate: async (id: number): Promise<ReceiptTemplate> => {
    const response = await api.post(`/api/receipt-templates/${id}/reset`);
    return response.data;
  },

  setDefaultReceiptTemplate: async (id: number): Promise<ReceiptTemplate> => {
    const response = await api.post(`/api/receipt-templates/${id}/set-default`);
    return response.data;
  },

  // Email Templates (Notifications)
  getEmailTemplates: async (): Promise<EmailTemplate[]> => {
    const response = await api.get('/api/notifications/templates');
    return response.data;
  },

  getEmailTemplatesByLanguage: async (language: string): Promise<EmailTemplate[]> => {
    const response = await api.get(`/api/notifications/templates/language/${language}`);
    return response.data;
  },

  updateEmailTemplate: async (id: number, template: { subject: string; htmlTemplate: string; textTemplate?: string }): Promise<EmailTemplate> => {
    const response = await api.put(`/api/notifications/templates/${id}`, template);
    return response.data;
  },

  // Notification Stats
  getNotificationStats: async (): Promise<NotificationStats> => {
    const response = await api.get('/api/notifications/stats');
    return response.data;
  },

  retryFailedNotifications: async (): Promise<void> => {
    await api.post('/api/notifications/retry-failed');
  },

  // Send Test Email
  sendTestEmail: async (request: TestEmailRequest): Promise<{ success: boolean; message: string }> => {
    const response = await api.post('/api/notifications/test-email', request);
    return response.data;
  },

  // SMTP Configuration
  getSmtpConfig: async (): Promise<SmtpConfig> => {
    const response = await api.get('/api/notifications/smtp-config');
    return response.data;
  },

  updateSmtpConfig: async (config: Partial<SmtpConfig>): Promise<SmtpConfig> => {
    const response = await api.put('/api/notifications/smtp-config', config);
    return response.data;
  },

  testSmtpConnection: async (): Promise<{ success: boolean; message: string }> => {
    const response = await api.post('/api/notifications/smtp-config/test');
    return response.data;
  },
};

