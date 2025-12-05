import api from './client';

export interface OrderStatistics {
  totalOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  pendingOrders: number;
  inProgressOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
  ordersByType: {
    pickup: number;
    delivery: number;
  };
  ordersByStatus: Record<string, number>;
  ordersByHour: { hour: number; count: number }[];
  topProducts: { productId: number; productName: string; quantity: number; revenue: number }[];
}

export interface PaymentStatistics {
  totalTransactions: number;
  successfulPayments: number;
  failedPayments: number;
  pendingPayments: number;
  refundedPayments: number;
  totalRevenue: number;
  totalRefunded: number;
  netRevenue: number;
  paymentsByMethod: Record<string, { count: number; amount: number }>;
  averageTransactionValue: number;
}

export interface NotificationStatistics {
  totalNotifications: number;
  sentNotifications: number;
  pendingNotifications: number;
  failedNotifications: number;
  deliveredNotifications: number;
  notificationsByType: Record<string, number>;
  notificationsByChannel: Record<string, number>;
  emailsSentToday: number;
  emailsFailedToday: number;
}

export interface ReceiptStatistics {
  totalReceipts: number;
  receiptsToday: number;
  totalAmount: number;
  averageReceiptValue: number;
  receiptsByType: Record<string, number>;
}

export interface ProductStatistics {
  totalProducts: number;
  activeProducts: number;
  unavailableProducts: number;
  productsByCategory: Record<string, number>;
  lowStockProducts: number;
}

export interface DashboardStatistics {
  orders: OrderStatistics;
  payments: PaymentStatistics;
  notifications: NotificationStatistics;
  receipts: ReceiptStatistics;
  products: ProductStatistics;
  period: {
    startDate: string;
    endDate: string;
  };
}

export interface RevenueDataPoint {
  date: string;
  revenue: number;
  orders: number;
  averageOrderValue: number;
}

export interface ProfitMetrics {
  grossRevenue: number;
  estimatedCosts: number;
  estimatedProfit: number;
  profitMargin: number;
}

export const statisticsApi = {
  // Get dashboard statistics
  getDashboardStats: async (startDate?: string, endDate?: string): Promise<DashboardStatistics> => {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const response = await api.get('/api/statistics/dashboard', { params });
    return response.data;
  },

  // Get order statistics
  getOrderStats: async (startDate?: string, endDate?: string): Promise<OrderStatistics> => {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const response = await api.get('/api/statistics/orders', { params });
    return response.data;
  },

  // Get payment statistics
  getPaymentStats: async (startDate?: string, endDate?: string): Promise<PaymentStatistics> => {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const response = await api.get('/api/statistics/payments', { params });
    return response.data;
  },

  // Get notification statistics
  getNotificationStats: async (): Promise<NotificationStatistics> => {
    const response = await api.get('/api/notifications/stats');
    return response.data;
  },

  // Get revenue over time
  getRevenueTimeline: async (
    period: 'day' | 'week' | 'month' | 'year' = 'week'
  ): Promise<RevenueDataPoint[]> => {
    const response = await api.get('/api/statistics/revenue', { params: { period } });
    return response.data;
  },

  // Get profit metrics
  getProfitMetrics: async (startDate?: string, endDate?: string): Promise<ProfitMetrics> => {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const response = await api.get('/api/statistics/profit', { params });
    return response.data;
  },

  // Get today's quick stats
  getTodayStats: async () => {
    const today = new Date().toISOString().split('T')[0];
    return statisticsApi.getDashboardStats(today, today);
  },
};
