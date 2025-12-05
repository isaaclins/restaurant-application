import api from './client';
import { Receipt, DailyReport } from '../types';

export const receiptsApi = {
  // Get all receipts
  getReceipts: async (params?: { startDate?: string; endDate?: string }): Promise<Receipt[]> => {
    const response = await api.get('/api/receipts', { params });
    return response.data;
  },

  // Get receipt by ID
  getReceipt: async (id: number): Promise<Receipt> => {
    const response = await api.get(`/api/receipts/${id}`);
    return response.data;
  },

  // Get receipt PDF (returns blob)
  getReceiptPdf: async (id: number): Promise<Blob> => {
    const response = await api.get(`/api/receipts/${id}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Get daily report
  getDailyReport: async (date?: string): Promise<DailyReport> => {
    const params = date ? { date } : {};
    const response = await api.get('/api/receipts/report', { params });
    return response.data;
  },

  // Download receipt PDF
  downloadReceiptPdf: async (id: number, receiptNumber: string): Promise<void> => {
    const blob = await receiptsApi.getReceiptPdf(id);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `receipt-${receiptNumber}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  },
};
