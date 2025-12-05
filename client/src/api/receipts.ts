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
    try {
      const blob = await receiptsApi.getReceiptPdf(id);
      
      // Create a blob URL
      const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
      
      // Create a temporary link element
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `receipt-${receiptNumber}.pdf`);
      
      // Append to body (required for Firefox)
      document.body.appendChild(link);
      
      // Trigger the download
      link.click();
      
      // Cleanup: remove the link and revoke the URL after a short delay
      setTimeout(() => {
        if (link.parentNode) {
          link.parentNode.removeChild(link);
        }
        window.URL.revokeObjectURL(url);
      }, 100);
    } catch (error) {
      console.error('Failed to download receipt PDF:', error);
      throw error;
    }
  },
};
