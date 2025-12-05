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
      headers: {
        'Accept': 'application/pdf',
      },
    });
    // Check if the response is actually a PDF
    if (response.data.type === 'application/json') {
      // Error response was returned as blob, parse it
      const text = await response.data.text();
      throw new Error(JSON.parse(text).message || 'Failed to fetch PDF');
    }
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
    try {
      const blob = await receiptsApi.getReceiptPdf(id);
      
      // Validate blob
      if (!blob || blob.size === 0) {
        throw new Error('Received empty PDF');
      }
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `receipt-${receiptNumber}.pdf`;
      a.style.display = 'none';
      document.body.appendChild(a);
      a.click();
      
      // Cleanup after a short delay to ensure download starts
      setTimeout(() => {
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      }, 100);
    } catch (error) {
      console.error('PDF download failed:', error);
      throw error;
    }
  },
};
