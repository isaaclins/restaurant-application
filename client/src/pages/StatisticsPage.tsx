import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { format, subDays, startOfMonth, endOfMonth, startOfWeek, endOfWeek, startOfYear, endOfYear } from 'date-fns';
import {
  BarChart3,
  TrendingUp,
  DollarSign,
  ShoppingCart,
  Mail,
  Receipt,
  Package,
  Clock,
  CheckCircle,
  XCircle,
  RefreshCw,
  Calendar,
  ArrowUpRight,
  Truck,
  Store,
  PieChart,
  Activity,
} from 'lucide-react';
import { ordersApi } from '../api/orders';
import { productsApi } from '../api/products';
import { receiptsApi } from '../api/receipts';
import { settingsApi } from '../api/settings';
import { Order } from '../types';

type DateRange = 'today' | 'yesterday' | 'week' | 'month' | 'year' | 'custom';

function StatisticsPage() {
  const [dateRange, setDateRange] = useState<DateRange>('today');
  const [customStart, setCustomStart] = useState('');
  const [customEnd, setCustomEnd] = useState('');

  const { startDate, endDate } = useMemo(() => {
    const now = new Date();
    switch (dateRange) {
      case 'today':
        const today = format(now, 'yyyy-MM-dd');
        return { startDate: today, endDate: today };
      case 'yesterday':
        const yesterday = format(subDays(now, 1), 'yyyy-MM-dd');
        return { startDate: yesterday, endDate: yesterday };
      case 'week':
        return {
          startDate: format(startOfWeek(now, { weekStartsOn: 1 }), 'yyyy-MM-dd'),
          endDate: format(endOfWeek(now, { weekStartsOn: 1 }), 'yyyy-MM-dd'),
        };
      case 'month':
        return {
          startDate: format(startOfMonth(now), 'yyyy-MM-dd'),
          endDate: format(endOfMonth(now), 'yyyy-MM-dd'),
        };
      case 'year':
        return {
          startDate: format(startOfYear(now), 'yyyy-MM-dd'),
          endDate: format(endOfYear(now), 'yyyy-MM-dd'),
        };
      case 'custom':
        return { startDate: customStart, endDate: customEnd };
      default:
        return { startDate: format(now, 'yyyy-MM-dd'), endDate: format(now, 'yyyy-MM-dd') };
    }
  }, [dateRange, customStart, customEnd]);

  // Fetch all orders for statistics
  const { data: ordersData, isLoading: ordersLoading } = useQuery({
    queryKey: ['allOrders'],
    queryFn: () => ordersApi.getOrders(),
  });

  // Fetch products
  const { data: productsData } = useQuery({
    queryKey: ['products'],
    queryFn: () => productsApi.getProducts(),
  });

  // Fetch notification stats
  const { data: notificationStats } = useQuery({
    queryKey: ['notificationStats'],
    queryFn: () => settingsApi.getNotificationStats(),
  });

  // Fetch receipts
  const { data: receiptsData } = useQuery({
    queryKey: ['receipts', startDate, endDate],
    queryFn: () => receiptsApi.getReceipts({ startDate, endDate }),
  });

  // Calculate order statistics from actual data
  const orderStats = useMemo(() => {
    if (!ordersData) return null;

    const orders = ordersData.filter((order: Order) => {
      const orderDate = order.createdAt?.split('T')[0];
      return orderDate >= startDate && orderDate <= endDate;
    });

    const totalOrders = orders.length;
    const completedOrders = orders.filter((o: Order) => 
      o.status === 'DELIVERED' || o.status === 'PICKED_UP'
    ).length;
    const cancelledOrders = orders.filter((o: Order) => o.status === 'CANCELLED').length;
    const pendingOrders = orders.filter((o: Order) => o.status === 'PENDING').length;
    const inProgressOrders = orders.filter((o: Order) => 
      o.status === 'CONFIRMED' || o.status === 'IN_PROGRESS' || o.status === 'READY'
    ).length;
    const totalRevenue = orders
      .filter((o: Order) => o.status !== 'CANCELLED')
      .reduce((sum: number, o: Order) => sum + (o.totalPrice || 0), 0);
    const averageOrderValue = totalOrders > 0 ? totalRevenue / (totalOrders - cancelledOrders || 1) : 0;
    const pickupOrders = orders.filter((o: Order) => o.orderType === 'PICKUP').length;
    const deliveryOrders = orders.filter((o: Order) => o.orderType === 'DELIVERY').length;

    // Orders by hour
    const ordersByHour = Array.from({ length: 24 }, (_, hour) => {
      const count = orders.filter((o: Order) => {
        const h = new Date(o.createdAt).getHours();
        return h === hour;
      }).length;
      return { hour, count };
    });

    // Top products
    const productCounts: Record<string, { quantity: number; revenue: number; name: string }> = {};
    orders.forEach((order: Order) => {
      order.items?.forEach((item) => {
        const key = item.productId?.toString() || item.productName;
        if (!productCounts[key]) {
          productCounts[key] = { quantity: 0, revenue: 0, name: item.productName };
        }
        productCounts[key].quantity += item.quantity;
        productCounts[key].revenue += item.totalPrice;
      });
    });
    const topProducts = Object.entries(productCounts)
      .map(([productId, data]) => ({
        productId: parseInt(productId) || 0,
        productName: data.name,
        quantity: data.quantity,
        revenue: data.revenue,
      }))
      .sort((a, b) => b.quantity - a.quantity)
      .slice(0, 10);

    // Calculate average completion time (order created to delivered/picked_up)
    const completedOrdersWithTime = orders.filter((o: Order) => 
      (o.status === 'DELIVERED' || o.status === 'PICKED_UP') && o.createdAt && o.updatedAt
    );
    let avgCompletionTime = '- min';
    if (completedOrdersWithTime.length > 0) {
      const totalMinutes = completedOrdersWithTime.reduce((sum: number, o: Order) => {
        const created = new Date(o.createdAt).getTime();
        const completed = new Date(o.updatedAt!).getTime();
        return sum + (completed - created) / (1000 * 60); // Convert to minutes
      }, 0);
      const avgMinutes = Math.round(totalMinutes / completedOrdersWithTime.length);
      if (avgMinutes >= 60) {
        const hours = Math.floor(avgMinutes / 60);
        const mins = avgMinutes % 60;
        avgCompletionTime = `${hours}h ${mins}m`;
      } else {
        avgCompletionTime = `${avgMinutes} min`;
      }
    }

    return {
      totalOrders,
      completedOrders,
      cancelledOrders,
      pendingOrders,
      inProgressOrders,
      totalRevenue,
      averageOrderValue,
      ordersByType: { pickup: pickupOrders, delivery: deliveryOrders },
      ordersByHour,
      topProducts,
      avgCompletionTime,
    };
  }, [ordersData, startDate, endDate]);

  // Calculate product statistics
  const productStats = useMemo(() => {
    if (!productsData) return null;
    const totalProducts = productsData.length;
    const activeProducts = productsData.filter((p) => p.isAvailable).length;
    const unavailableProducts = totalProducts - activeProducts;
    
    const byCategory: Record<string, number> = {};
    productsData.forEach((p) => {
      const cat = p.categoryName || 'Uncategorized';
      byCategory[cat] = (byCategory[cat] || 0) + 1;
    });

    return { totalProducts, activeProducts, unavailableProducts, productsByCategory: byCategory };
  }, [productsData]);

  // Calculate receipt statistics
  const receiptStats = useMemo(() => {
    if (!receiptsData) return null;
    const totalReceipts = receiptsData.length;
    const totalAmount = receiptsData.reduce((sum, r) => sum + (r.totalAmount || 0), 0);
    const averageValue = totalReceipts > 0 ? totalAmount / totalReceipts : 0;

    return { totalReceipts, totalAmount, averageValue };
  }, [receiptsData]);

  const isLoading = ordersLoading;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <RefreshCw className="w-8 h-8 text-orange-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col overflow-hidden">
      {/* Header */}
      <div className="bg-white shadow px-6 py-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
              <BarChart3 className="w-7 h-7 text-orange-500" />
              Statistics Dashboard
            </h1>
            <p className="text-sm text-gray-500">Complete business analytics and insights</p>
          </div>
          
          {/* Date Range Selector */}
          <div className="flex items-center gap-2">
            {(['today', 'yesterday', 'week', 'month', 'year'] as DateRange[]).map((range) => (
              <button
                key={range}
                onClick={() => setDateRange(range)}
                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                  dateRange === range
                    ? 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {range.charAt(0).toUpperCase() + range.slice(1)}
              </button>
            ))}
            <button
              onClick={() => setDateRange('custom')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-1 ${
                dateRange === 'custom'
                  ? 'bg-orange-500 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              <Calendar className="w-4 h-4" />
              Custom
            </button>
          </div>
        </div>

        {/* Custom Date Range */}
        {dateRange === 'custom' && (
          <div className="flex items-center gap-4 mt-4">
            <div className="flex items-center gap-2">
              <label className="text-sm text-gray-600">From:</label>
              <input
                type="date"
                value={customStart}
                onChange={(e) => setCustomStart(e.target.value)}
                className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
            <div className="flex items-center gap-2">
              <label className="text-sm text-gray-600">To:</label>
              <input
                type="date"
                value={customEnd}
                onChange={(e) => setCustomEnd(e.target.value)}
                className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
          </div>
        )}
      </div>

      {/* Dashboard Content */}
      <div className="flex-1 overflow-auto p-6">
        {/* Key Metrics Row */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          {/* Total Revenue */}
          <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-100 text-sm font-medium">Total Revenue</p>
                <p className="text-3xl font-bold mt-1">
                  CHF {(orderStats?.totalRevenue || 0).toFixed(2)}
                </p>
                <p className="text-green-200 text-sm mt-2 flex items-center gap-1">
                  <ArrowUpRight className="w-4 h-4" />
                  {orderStats?.completedOrders || 0} completed orders
                </p>
              </div>
              <div className="bg-white/20 p-3 rounded-lg">
                <DollarSign className="w-8 h-8" />
              </div>
            </div>
          </div>

          {/* Total Orders */}
          <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-blue-100 text-sm font-medium">Total Orders</p>
                <p className="text-3xl font-bold mt-1">{orderStats?.totalOrders || 0}</p>
                <p className="text-blue-200 text-sm mt-2 flex items-center gap-1">
                  <Activity className="w-4 h-4" />
                  {orderStats?.inProgressOrders || 0} in progress
                </p>
              </div>
              <div className="bg-white/20 p-3 rounded-lg">
                <ShoppingCart className="w-8 h-8" />
              </div>
            </div>
          </div>

          {/* Average Order Value */}
          <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-purple-100 text-sm font-medium">Avg Order Value</p>
                <p className="text-3xl font-bold mt-1">
                  CHF {(orderStats?.averageOrderValue || 0).toFixed(2)}
                </p>
                <p className="text-purple-200 text-sm mt-2 flex items-center gap-1">
                  <TrendingUp className="w-4 h-4" />
                  Per successful order
                </p>
              </div>
              <div className="bg-white/20 p-3 rounded-lg">
                <TrendingUp className="w-8 h-8" />
              </div>
            </div>
          </div>

          {/* Average Completion Time */}
          <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-orange-100 text-sm font-medium">Avg Completion Time</p>
                <p className="text-3xl font-bold mt-1">
                  {orderStats?.avgCompletionTime || '- min'}
                </p>
                <p className="text-orange-200 text-sm mt-2 flex items-center gap-1">
                  <Clock className="w-4 h-4" />
                  Order to ready
                </p>
              </div>
              <div className="bg-white/20 p-3 rounded-lg">
                <Clock className="w-8 h-8" />
              </div>
            </div>
          </div>
        </div>

        {/* Second Row - Order Details */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
          {/* Order Status Breakdown */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <PieChart className="w-5 h-5 text-orange-500" />
              Order Status
            </h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                  <span className="text-gray-600">Completed</span>
                </div>
                <span className="font-semibold">{orderStats?.completedOrders || 0}</span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                  <span className="text-gray-600">Pending</span>
                </div>
                <span className="font-semibold">{orderStats?.pendingOrders || 0}</span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                  <span className="text-gray-600">In Progress</span>
                </div>
                <span className="font-semibold">{orderStats?.inProgressOrders || 0}</span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                  <span className="text-gray-600">Cancelled</span>
                </div>
                <span className="font-semibold">{orderStats?.cancelledOrders || 0}</span>
              </div>
            </div>
            
            {/* Progress Bar */}
            {(orderStats?.totalOrders || 0) > 0 && (
              <div className="mt-6">
                <div className="h-4 bg-gray-100 rounded-full overflow-hidden flex">
                  <div
                    className="bg-green-500 h-full"
                    style={{ width: `${((orderStats?.completedOrders || 0) / (orderStats?.totalOrders || 1)) * 100}%` }}
                  ></div>
                  <div
                    className="bg-blue-500 h-full"
                    style={{ width: `${((orderStats?.inProgressOrders || 0) / (orderStats?.totalOrders || 1)) * 100}%` }}
                  ></div>
                  <div
                    className="bg-yellow-500 h-full"
                    style={{ width: `${((orderStats?.pendingOrders || 0) / (orderStats?.totalOrders || 1)) * 100}%` }}
                  ></div>
                  <div
                    className="bg-red-500 h-full"
                    style={{ width: `${((orderStats?.cancelledOrders || 0) / (orderStats?.totalOrders || 1)) * 100}%` }}
                  ></div>
                </div>
              </div>
            )}
          </div>

          {/* Order Types */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <Truck className="w-5 h-5 text-orange-500" />
              Order Types
            </h3>
            <div className="space-y-6">
              <div>
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Store className="w-5 h-5 text-blue-500" />
                    <span className="text-gray-600">Pickup</span>
                  </div>
                  <span className="font-semibold">{orderStats?.ordersByType.pickup || 0}</span>
                </div>
                <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="bg-blue-500 h-full transition-all"
                    style={{
                      width: `${
                        ((orderStats?.ordersByType.pickup || 0) /
                          ((orderStats?.ordersByType.pickup || 0) + (orderStats?.ordersByType.delivery || 0) || 1)) *
                        100
                      }%`,
                    }}
                  ></div>
                </div>
              </div>
              <div>
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Truck className="w-5 h-5 text-green-500" />
                    <span className="text-gray-600">Delivery</span>
                  </div>
                  <span className="font-semibold">{orderStats?.ordersByType.delivery || 0}</span>
                </div>
                <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="bg-green-500 h-full transition-all"
                    style={{
                      width: `${
                        ((orderStats?.ordersByType.delivery || 0) /
                          ((orderStats?.ordersByType.pickup || 0) + (orderStats?.ordersByType.delivery || 0) || 1)) *
                        100
                      }%`,
                    }}
                  ></div>
                </div>
              </div>
            </div>
          </div>

          {/* Notification Stats */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <Mail className="w-5 h-5 text-orange-500" />
              Email Notifications
            </h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-green-50 rounded-lg p-4 text-center">
                <CheckCircle className="w-8 h-8 text-green-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-green-700">{notificationStats?.sent || 0}</p>
                <p className="text-sm text-green-600">Sent</p>
              </div>
              <div className="bg-yellow-50 rounded-lg p-4 text-center">
                <Clock className="w-8 h-8 text-yellow-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-yellow-700">{notificationStats?.pending || 0}</p>
                <p className="text-sm text-yellow-600">Pending</p>
              </div>
              <div className="bg-red-50 rounded-lg p-4 text-center">
                <XCircle className="w-8 h-8 text-red-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-red-700">{notificationStats?.failed || 0}</p>
                <p className="text-sm text-red-600">Failed</p>
              </div>
              <div className="bg-blue-50 rounded-lg p-4 text-center">
                <Mail className="w-8 h-8 text-blue-500 mx-auto mb-2" />
                <p className="text-2xl font-bold text-blue-700">
                  {(notificationStats?.sent || 0) + (notificationStats?.pending || 0) + (notificationStats?.failed || 0)}
                </p>
                <p className="text-sm text-blue-600">Total</p>
              </div>
            </div>
          </div>
        </div>

        {/* Third Row - Products & Hourly Chart */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          {/* Top Products */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <Package className="w-5 h-5 text-orange-500" />
              Top Products
            </h3>
            {(orderStats?.topProducts?.length || 0) > 0 ? (
              <div className="space-y-3">
                {orderStats?.topProducts.slice(0, 5).map((product, index) => (
                  <div key={product.productId} className="flex items-center gap-3">
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white font-bold ${
                      index === 0 ? 'bg-yellow-500' :
                      index === 1 ? 'bg-gray-400' :
                      index === 2 ? 'bg-amber-600' :
                      'bg-gray-300'
                    }`}>
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <p className="font-medium text-gray-800">{product.productName}</p>
                      <p className="text-sm text-gray-500">{product.quantity} sold</p>
                    </div>
                    <p className="font-semibold text-green-600">CHF {product.revenue.toFixed(2)}</p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 text-center py-8">No product data available</p>
            )}
          </div>

          {/* Orders by Hour */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <Clock className="w-5 h-5 text-orange-500" />
              Orders by Hour
            </h3>
            {orderStats?.ordersByHour && orderStats.ordersByHour.some(d => d.count > 0) ? (
              <div className="h-48 flex items-end justify-between gap-1">
                {orderStats.ordersByHour.map((data, index) => {
                  const maxCount = Math.max(...orderStats.ordersByHour.map(d => d.count), 1);
                  const heightPercent = maxCount > 0 ? (data.count / maxCount) * 100 : 0;
                  // Convert percentage to actual pixels (h-48 = 192px); give zero-count bars a visible baseline
                  const heightPx = Math.max((heightPercent / 100) * 180, data.count > 0 ? 12 : 8);
                  return (
                    <div key={index} className="flex-1 flex flex-col items-center justify-end h-full">
                      <div
                        className={`w-full rounded-t transition-all ${
                          data.count > 0 ? 'bg-orange-500 hover:bg-orange-600' : 'bg-gray-200'
                        }`}
                        style={{ height: `${heightPx}px` }}
                        title={`${data.hour}:00 - ${data.count} orders`}
                      />
                      {index % 4 === 0 && (
                        <span className="text-xs text-gray-500 mt-1">{data.hour}</span>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="h-48 flex items-center justify-center text-gray-400">
                <div className="text-center">
                  <Clock className="w-8 h-8 mx-auto mb-2 opacity-50" />
                  <p>No orders in selected period</p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Fourth Row - Product & Receipt Stats */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Product Statistics */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <Package className="w-5 h-5 text-orange-500" />
              Product Overview
            </h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-600">Total Products</span>
                <span className="font-bold text-xl">{productStats?.totalProducts || 0}</span>
              </div>
              <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                <span className="text-green-600">Available</span>
                <span className="font-bold text-xl text-green-700">{productStats?.activeProducts || 0}</span>
              </div>
              <div className="flex items-center justify-between p-3 bg-red-50 rounded-lg">
                <span className="text-red-600">Unavailable</span>
                <span className="font-bold text-xl text-red-700">{productStats?.unavailableProducts || 0}</span>
              </div>
            </div>
          </div>

          {/* Receipt Statistics */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <Receipt className="w-5 h-5 text-orange-500" />
              Receipts
            </h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-600">Total Receipts</span>
                <span className="font-bold text-xl">{receiptStats?.totalReceipts || 0}</span>
              </div>
              <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                <span className="text-green-600">Total Amount</span>
                <span className="font-bold text-xl text-green-700">
                  CHF {(receiptStats?.totalAmount || 0).toFixed(2)}
                </span>
              </div>
              <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
                <span className="text-blue-600">Average Value</span>
                <span className="font-bold text-xl text-blue-700">
                  CHF {(receiptStats?.averageValue || 0).toFixed(2)}
                </span>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <Activity className="w-5 h-5 text-orange-500" />
              Quick Summary
            </h3>
            <div className="space-y-3">
              <div className="p-4 bg-gradient-to-r from-orange-50 to-orange-100 rounded-lg border border-orange-200">
                <p className="text-sm text-orange-600 font-medium">Conversion Rate</p>
                <p className="text-2xl font-bold text-orange-700">
                  {orderStats?.totalOrders && orderStats.totalOrders > 0
                    ? (((orderStats.completedOrders || 0) / orderStats.totalOrders) * 100).toFixed(1)
                    : 0}%
                </p>
                <p className="text-xs text-orange-500">Orders completed vs total</p>
              </div>
              <div className="p-4 bg-gradient-to-r from-green-50 to-green-100 rounded-lg border border-green-200">
                <p className="text-sm text-green-600 font-medium">Revenue per Order</p>
                <p className="text-2xl font-bold text-green-700">
                  CHF {(orderStats?.averageOrderValue || 0).toFixed(2)}
                </p>
                <p className="text-xs text-green-500">Average transaction value</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StatisticsPage;
