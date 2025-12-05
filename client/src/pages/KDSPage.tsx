import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '../api/orders';
import { Order, OrderStatus } from '../types';
import { format, differenceInMinutes } from 'date-fns';
import {
  Clock,
  MapPin,
  Phone,
  CheckCircle,
  ChefHat,
  Package,
  Truck,
  RefreshCw,
} from 'lucide-react';

const statusColors: Record<OrderStatus, string> = {
  PENDING: 'bg-red-500',
  CONFIRMED: 'bg-orange-500',
  PREPARING: 'bg-yellow-500',
  READY: 'bg-green-500',
  COMPLETED: 'bg-gray-400',
  CANCELLED: 'bg-gray-600',
};

const statusLabels: Record<OrderStatus, string> = {
  PENDING: 'New',
  CONFIRMED: 'Confirmed',
  PREPARING: 'Preparing',
  READY: 'Ready',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

function OrderCard({ order, onStatusChange }: { order: Order; onStatusChange: (id: number, status: OrderStatus) => void }) {
  const [elapsedMinutes, setElapsedMinutes] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setElapsedMinutes(differenceInMinutes(new Date(), new Date(order.createdAt)));
    }, 30000); // Update every 30 seconds

    setElapsedMinutes(differenceInMinutes(new Date(), new Date(order.createdAt)));
    return () => clearInterval(interval);
  }, [order.createdAt]);

  const getNextStatus = (): OrderStatus | null => {
    const flow: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'COMPLETED'];
    const currentIndex = flow.indexOf(order.status);
    return currentIndex < flow.length - 1 ? flow[currentIndex + 1] : null;
  };

  const nextStatus = getNextStatus();

  const getTimerColor = () => {
    if (elapsedMinutes > 30) return 'text-red-600 bg-red-100';
    if (elapsedMinutes > 15) return 'text-yellow-600 bg-yellow-100';
    return 'text-green-600 bg-green-100';
  };

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden">
      {/* Header */}
      <div className={`${statusColors[order.status]} px-4 py-2 flex items-center justify-between`}>
        <div className="flex items-center space-x-2">
          <span className="text-white font-bold">#{order.orderNumber}</span>
          {order.orderType === 'DELIVERY' ? (
            <Truck className="w-4 h-4 text-white" />
          ) : (
            <Package className="w-4 h-4 text-white" />
          )}
        </div>
        <span className={`px-2 py-1 rounded text-xs font-medium ${getTimerColor()}`}>
          <Clock className="w-3 h-3 inline mr-1" />
          {elapsedMinutes}m
        </span>
      </div>

      {/* Content */}
      <div className="p-4">
        {/* Customer Info */}
        <div className="mb-3 pb-3 border-b border-gray-100">
          <p className="font-medium text-gray-800">{order.customerName}</p>
          {order.customerPhone && (
            <p className="text-sm text-gray-500 flex items-center mt-1">
              <Phone className="w-3 h-3 mr-1" /> {order.customerPhone}
            </p>
          )}
          {order.orderType === 'DELIVERY' && order.customerAddress && (
            <p className="text-sm text-gray-500 flex items-center mt-1">
              <MapPin className="w-3 h-3 mr-1" /> {order.customerAddress}
            </p>
          )}
        </div>

        {/* Items */}
        <div className="space-y-2 mb-4">
          {order.items.map((item) => (
            <div key={item.id} className="flex items-start">
              <span className="font-medium text-orange-500 mr-2">{item.quantity}x</span>
              <div className="flex-1">
                <span className="text-gray-800">{item.productName}</span>
                {item.notes && (
                  <p className="text-xs text-gray-500 italic">{item.notes}</p>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* Notes */}
        {order.notes && (
          <div className="bg-yellow-50 border border-yellow-200 rounded p-2 mb-4">
            <p className="text-sm text-yellow-800">{order.notes}</p>
          </div>
        )}

        {/* Total */}
        <div className="flex justify-between items-center pt-2 border-t border-gray-100">
          <span className="text-sm text-gray-500">Total</span>
          <span className="font-bold text-lg">CHF {order.total.toFixed(2)}</span>
        </div>

        {/* Action Button */}
        {nextStatus && (
          <button
            onClick={() => onStatusChange(order.id, nextStatus)}
            className="w-full mt-4 py-2 px-4 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600 transition flex items-center justify-center"
          >
            {nextStatus === 'CONFIRMED' && <CheckCircle className="w-4 h-4 mr-2" />}
            {nextStatus === 'PREPARING' && <ChefHat className="w-4 h-4 mr-2" />}
            {nextStatus === 'READY' && <Package className="w-4 h-4 mr-2" />}
            {nextStatus === 'COMPLETED' && <CheckCircle className="w-4 h-4 mr-2" />}
            Mark as {statusLabels[nextStatus]}
          </button>
        )}
      </div>

      {/* Footer */}
      <div className="px-4 py-2 bg-gray-50 text-xs text-gray-500">
        {format(new Date(order.createdAt), 'HH:mm - dd.MM.yyyy')}
      </div>
    </div>
  );
}

function KDSPage() {
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState<'all' | 'pickup' | 'delivery'>('all');

  const { data: orders = [], isLoading, refetch } = useQuery({
    queryKey: ['orders'],
    queryFn: () => ordersApi.getOrders(),
    refetchInterval: 10000, // Refresh every 10 seconds
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: OrderStatus }) =>
      ordersApi.updateStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });

  const handleStatusChange = (id: number, status: OrderStatus) => {
    updateStatusMutation.mutate({ id, status });
  };

  const filteredOrders = orders.filter((order) => {
    if (filter === 'pickup') return order.orderType === 'PICKUP';
    if (filter === 'delivery') return order.orderType === 'DELIVERY';
    return true;
  });

  const activeOrders = filteredOrders.filter(
    (order) => !['COMPLETED', 'CANCELLED'].includes(order.status)
  );

  const groupedOrders = {
    pending: activeOrders.filter((o) => o.status === 'PENDING'),
    preparing: activeOrders.filter((o) => ['CONFIRMED', 'PREPARING'].includes(o.status)),
    ready: activeOrders.filter((o) => o.status === 'READY'),
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <RefreshCw className="w-8 h-8 text-orange-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="bg-white shadow px-6 py-4 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Kitchen Display</h1>
          <p className="text-sm text-gray-500">{activeOrders.length} active orders</p>
        </div>

        <div className="flex items-center space-x-4">
          {/* Filter */}
          <div className="flex bg-gray-100 rounded-lg p-1">
            <button
              onClick={() => setFilter('all')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition ${
                filter === 'all' ? 'bg-white shadow text-gray-800' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setFilter('pickup')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition flex items-center ${
                filter === 'pickup' ? 'bg-white shadow text-gray-800' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              <Package className="w-4 h-4 mr-1" /> Pickup
            </button>
            <button
              onClick={() => setFilter('delivery')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition flex items-center ${
                filter === 'delivery' ? 'bg-white shadow text-gray-800' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              <Truck className="w-4 h-4 mr-1" /> Delivery
            </button>
          </div>

          {/* Refresh Button */}
          <button
            onClick={() => refetch()}
            className="p-2 text-gray-500 hover:text-orange-500 hover:bg-orange-50 rounded-lg transition"
          >
            <RefreshCw className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* KDS Grid */}
      <div className="flex-1 p-6 overflow-auto">
        <div className="grid grid-cols-3 gap-6 h-full">
          {/* New Orders Column */}
          <div className="flex flex-col">
            <div className="flex items-center mb-4">
              <div className="w-3 h-3 rounded-full bg-red-500 mr-2" />
              <h2 className="font-bold text-gray-800">New ({groupedOrders.pending.length})</h2>
            </div>
            <div className="flex-1 space-y-4 overflow-auto">
              {groupedOrders.pending.map((order) => (
                <OrderCard key={order.id} order={order} onStatusChange={handleStatusChange} />
              ))}
              {groupedOrders.pending.length === 0 && (
                <div className="text-center text-gray-400 py-8">
                  No new orders
                </div>
              )}
            </div>
          </div>

          {/* Preparing Column */}
          <div className="flex flex-col">
            <div className="flex items-center mb-4">
              <div className="w-3 h-3 rounded-full bg-yellow-500 mr-2" />
              <h2 className="font-bold text-gray-800">Preparing ({groupedOrders.preparing.length})</h2>
            </div>
            <div className="flex-1 space-y-4 overflow-auto">
              {groupedOrders.preparing.map((order) => (
                <OrderCard key={order.id} order={order} onStatusChange={handleStatusChange} />
              ))}
              {groupedOrders.preparing.length === 0 && (
                <div className="text-center text-gray-400 py-8">
                  No orders in preparation
                </div>
              )}
            </div>
          </div>

          {/* Ready Column */}
          <div className="flex flex-col">
            <div className="flex items-center mb-4">
              <div className="w-3 h-3 rounded-full bg-green-500 mr-2" />
              <h2 className="font-bold text-gray-800">Ready ({groupedOrders.ready.length})</h2>
            </div>
            <div className="flex-1 space-y-4 overflow-auto">
              {groupedOrders.ready.map((order) => (
                <OrderCard key={order.id} order={order} onStatusChange={handleStatusChange} />
              ))}
              {groupedOrders.ready.length === 0 && (
                <div className="text-center text-gray-400 py-8">
                  No orders ready
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default KDSPage;
