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

  // Download receipt PDF with "Save As" dialog
  downloadReceiptPdf: async (id: number, receiptNumber: string): Promise<boolean> => {
    try {
      const blob = await receiptsApi.getReceiptPdf(id);
      
      // Validate blob
      if (!blob || blob.size === 0) {
        throw new Error('Received empty PDF');
      }

      // Check if the File System Access API is available (modern browsers)
      if ('showSaveFilePicker' in window) {
        try {
          const handle = await (window as any).showSaveFilePicker({
            suggestedName: `receipt-${receiptNumber}.pdf`,
            types: [
              {
                description: 'PDF Document',
                accept: { 'application/pdf': ['.pdf'] },
              },
            ],
          });
          const writable = await handle.createWritable();
          await writable.write(blob);
          await writable.close();
          return true;
        } catch (err: any) {
          // User cancelled the save dialog
          if (err.name === 'AbortError') {
            return false;
          }
          throw err;
        }
      } else {
        // Fallback for browsers without File System Access API
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `receipt-${receiptNumber}.pdf`;
        a.style.display = 'none';
        document.body.appendChild(a);
        a.click();
        
        setTimeout(() => {
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a);
        }, 100);
        return true;
      }
    } catch (error) {
      console.error('PDF download failed:', error);
      throw error;
    }
  },
};
