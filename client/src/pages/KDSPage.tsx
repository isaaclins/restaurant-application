import { useState, useEffect, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '../api/orders';
import { Order, OrderStatus } from '../types';
import { differenceInSeconds, differenceInMinutes, addMinutes, format } from 'date-fns';
import {
  Clock,
  MapPin,
  CreditCard,
  Check,
  Plus,
  Settings,
  ArrowUpDown,
  RefreshCw,
} from 'lucide-react';

// =============================================================================
// Types
// =============================================================================

interface SelectedOrder extends Order {
  itemChecks: Record<string, boolean>;
}

type SortMode = 'time' | 'item';

// =============================================================================
// LocalStorage Helpers for Item Checks
// =============================================================================

const CHECKS_STORAGE_KEY = 'kds_item_checks';

interface StoredChecks {
  [orderId: string]: {
    checks: Record<string, boolean>;
    timestamp: number;
  };
}

function getStoredChecks(): StoredChecks {
  try {
    const stored = localStorage.getItem(CHECKS_STORAGE_KEY);
    return stored ? JSON.parse(stored) : {};
  } catch {
    return {};
  }
}

function saveOrderChecks(orderId: number, checks: Record<string, boolean>): void {
  const stored = getStoredChecks();
  stored[orderId.toString()] = {
    checks,
    timestamp: Date.now(),
  };
  localStorage.setItem(CHECKS_STORAGE_KEY, JSON.stringify(stored));
}

function getOrderChecks(orderId: number): Record<string, boolean> | null {
  const stored = getStoredChecks();
  const orderData = stored[orderId.toString()];
  if (!orderData) return null;
  
  // Check if older than 30 minutes (1800000ms)
  if (Date.now() - orderData.timestamp > 1800000) {
    removeOrderChecks(orderId);
    return null;
  }
  
  return orderData.checks;
}

function removeOrderChecks(orderId: number): void {
  const stored = getStoredChecks();
  delete stored[orderId.toString()];
  localStorage.setItem(CHECKS_STORAGE_KEY, JSON.stringify(stored));
}

function cleanupExpiredChecks(): void {
  const stored = getStoredChecks();
  const now = Date.now();
  let changed = false;
  
  for (const orderId in stored) {
    // Remove if older than 30 minutes
    if (now - stored[orderId].timestamp > 1800000) {
      delete stored[orderId];
      changed = true;
    }
  }
  
  if (changed) {
    localStorage.setItem(CHECKS_STORAGE_KEY, JSON.stringify(stored));
  }
}

// =============================================================================
// Utility Functions
// =============================================================================

// Calculate time remaining until ETA (or time elapsed since order)
function getTimeInfo(order: Order) {
  const now = new Date();
  const createdAt = new Date(order.createdAt);
  
  // If ETA is set, use it; otherwise use 15 min from creation as default
  // Backend uses 'estimatedDelivery', frontend might use 'estimatedReadyTime'
  const eta = order.estimatedDelivery 
    ? new Date(order.estimatedDelivery)
    : order.estimatedReadyTime
      ? new Date(order.estimatedReadyTime)
      : addMinutes(createdAt, 15);
  
  const secondsRemaining = differenceInSeconds(eta, now);
  const minutesElapsed = differenceInMinutes(now, createdAt);
  
  return {
    secondsRemaining,
    minutesElapsed,
    isOverdue: secondsRemaining < 0,
    eta,
  };
}

// Format countdown timer (MM:SS)
function formatCountdown(seconds: number): string {
  const absSeconds = Math.abs(seconds);
  const mins = Math.floor(absSeconds / 60);
  const secs = absSeconds % 60;
  const sign = seconds < 0 ? '-' : '';
  return `${sign}${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

// Get color based on time remaining
function getTimeColor(secondsRemaining: number): 'red' | 'yellow' | 'green' | 'blue' {
  if (secondsRemaining < 0) return 'red';      // Overdue
  if (secondsRemaining < 300) return 'red';    // < 5 min
  if (secondsRemaining < 600) return 'yellow'; // < 10 min
  return 'green';                               // OK
}

// Color classes for order cards
const cardColorClasses = {
  red: {
    header: 'bg-red-400',
    headerText: 'text-white',
  },
  yellow: {
    header: 'bg-yellow-400',
    headerText: 'text-gray-800',
  },
  green: {
    header: 'bg-green-400',
    headerText: 'text-white',
  },
  blue: {
    header: 'bg-blue-400',
    headerText: 'text-white',
  },
};

// =============================================================================
// Order Card Component (for grid view)
// =============================================================================

interface OrderCardProps {
  order: Order;
  isSelected: boolean;
  onClick: () => void;
}

function OrderCard({ order, isSelected, onClick }: OrderCardProps) {
  const [timeInfo, setTimeInfo] = useState(getTimeInfo(order));
  
  useEffect(() => {
    const interval = setInterval(() => {
      setTimeInfo(getTimeInfo(order));
    }, 1000);
    return () => clearInterval(interval);
  }, [order]);

  const color = isSelected ? 'blue' : getTimeColor(timeInfo.secondsRemaining);
  const colorClasses = cardColorClasses[color];
  const itemCount = order.items.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <div 
      onClick={onClick}
      className={`cursor-pointer rounded-lg overflow-hidden shadow-md transition-transform hover:scale-[1.02] ${
        isSelected ? 'ring-2 ring-blue-500' : ''
      }`}
    >
      {/* Header */}
      <div className={`${colorClasses.header} px-3 py-2`}>
        <div className={`text-xs ${colorClasses.headerText} opacity-80`}>
          #{order.orderNumber}
        </div>
        <div className={`text-lg font-bold ${colorClasses.headerText} font-mono`}>
          {formatCountdown(timeInfo.secondsRemaining)}
        </div>
        <div className={`text-xs ${colorClasses.headerText} opacity-80 truncate`}>
          {order.customerName}
        </div>
      </div>
      
      {/* Body */}
      <div className="bg-white px-3 py-2">
        <div className="text-sm text-gray-700">
          {itemCount} ITEMS
        </div>
      </div>
    </div>
  );
}

// =============================================================================
// Pickup Column Component (left side)
// =============================================================================

interface PickupColumnProps {
  orders: Order[];
  selectedOrderId: number | null;
  onSelectOrder: (order: Order) => void;
}

function PickupColumn({ orders, selectedOrderId, onSelectOrder }: PickupColumnProps) {
  // Group orders by ETA (rough time buckets)
  const groupedByEta = useMemo(() => {
    const groups: { eta: string; orders: Order[] }[] = [];
    
    // Sort by ETA
    const sorted = [...orders].sort((a, b) => {
      const etaA = a.estimatedDelivery || a.estimatedReadyTime ? new Date(a.estimatedDelivery || a.estimatedReadyTime!) : addMinutes(new Date(a.createdAt), 15);
      const etaB = b.estimatedDelivery || b.estimatedReadyTime ? new Date(b.estimatedDelivery || b.estimatedReadyTime!) : addMinutes(new Date(b.createdAt), 15);
      return etaA.getTime() - etaB.getTime();
    });

    // Group by similar ETA times (within 5 minutes)
    sorted.forEach((order) => {
      const eta = order.estimatedDelivery || order.estimatedReadyTime 
        ? new Date(order.estimatedDelivery || order.estimatedReadyTime!) 
        : addMinutes(new Date(order.createdAt), 15);
      
      const etaLabel = format(eta, 'HH:mm');
      
      const existingGroup = groups.find(g => g.eta === etaLabel);
      if (existingGroup) {
        existingGroup.orders.push(order);
      } else {
        groups.push({ eta: etaLabel, orders: [order] });
      }
    });

    return groups;
  }, [orders]);

  return (
    <div className="flex flex-col h-full border-r border-gray-200">
      <div className="p-4 border-b border-gray-200">
        <h2 className="text-2xl font-light text-gray-800">Pickup</h2>
      </div>
      
      <div className="flex-1 overflow-auto p-4">
        {groupedByEta.map((group) => (
          <div key={group.eta} className="mb-6">
            <div className="text-lg font-bold text-gray-700 mb-2">{group.eta}</div>
            <div className="space-y-3">
              {group.orders.map((order) => (
                <OrderCard
                  key={order.id}
                  order={order}
                  isSelected={selectedOrderId === order.id}
                  onClick={() => onSelectOrder(order)}
                />
              ))}
            </div>
          </div>
        ))}
        
        {orders.length === 0 && (
          <div className="text-center text-gray-400 py-8">
            No pickup orders
          </div>
        )}
      </div>
    </div>
  );
}

// =============================================================================
// Delivery Grid Component (center)
// =============================================================================

interface DeliveryGridProps {
  orders: Order[];
  selectedOrderId: number | null;
  onSelectOrder: (order: Order) => void;
}

function DeliveryGrid({ orders, selectedOrderId, onSelectOrder }: DeliveryGridProps) {
  return (
    <div className="flex flex-col h-full">
      <div className="p-4 border-b border-gray-200 bg-blue-50">
        <h2 className="text-2xl font-light text-gray-800 text-center">Delivery</h2>
      </div>
      
      <div className="flex-1 overflow-auto p-4">
        <div className="grid grid-cols-4 gap-3">
          {orders.map((order) => (
            <OrderCard
              key={order.id}
              order={order}
              isSelected={selectedOrderId === order.id}
              onClick={() => onSelectOrder(order)}
            />
          ))}
        </div>
        
        {orders.length === 0 && (
          <div className="text-center text-gray-400 py-8">
            No delivery orders
          </div>
        )}
      </div>
    </div>
  );
}

// =============================================================================
// Detail Panel Component (right side)
// =============================================================================

interface DetailPanelProps {
  order: SelectedOrder | null;
  onToggleItem: (itemKey: string) => void;
  onComplete: () => void;
  onDelete: () => void;
}

function DetailPanel({ order, onToggleItem, onComplete, onDelete }: DetailPanelProps) {
  const [timeInfo, setTimeInfo] = useState(order ? getTimeInfo(order) : null);
  
  useEffect(() => {
    if (!order) return;
    
    const interval = setInterval(() => {
      setTimeInfo(getTimeInfo(order));
    }, 1000);
    
    setTimeInfo(getTimeInfo(order));
    return () => clearInterval(interval);
  }, [order]);

  if (!order) {
    return (
      <div className="flex flex-col h-full border-l border-gray-200 bg-gray-50">
        <div className="flex-1 flex items-center justify-center text-gray-400">
          Select an order to view details
        </div>
      </div>
    );
  }

  const allChecked = order.items.length > 0 && order.items.every((_, index) => {
    const itemKey = `${index}-${order.items[index].productId}`;
    return order.itemChecks[itemKey];
  });

  return (
    <div className="flex flex-col h-full border-l border-gray-200">
      {/* Header */}
      <div className="p-4 border-b border-gray-200 bg-blue-50">
        <div className="text-center">
          <div className="text-sm text-gray-600">Ticket #{order.orderNumber}</div>
        </div>
        <div className="text-center mt-2">
          <div className="text-3xl font-mono font-bold text-blue-600">
            {timeInfo && formatCountdown(timeInfo.secondsRemaining)}
          </div>
        </div>
      </div>

      {/* Items List */}
      <div className="flex-1 overflow-auto p-4">
        <div className="space-y-4">
          {/* Sort items: unchecked first, checked (completed) at bottom */}
          {[...order.items]
            .map((item, index) => ({ item, index, itemKey: `${index}-${item.productId}` }))
            .sort((a, b) => {
              const aChecked = order.itemChecks[a.itemKey] ? 1 : 0;
              const bChecked = order.itemChecks[b.itemKey] ? 1 : 0;
              return aChecked - bChecked;
            })
            .map(({ item, itemKey }) => {
              const isChecked = order.itemChecks[itemKey];
              return (
                <div 
                  key={itemKey} 
                  className={`flex items-start transition-all duration-300 ${
                    isChecked ? 'opacity-50' : ''
                  }`}
                >
                  <div className="flex-1">
                    <div className={`text-sm font-medium ${
                      isChecked 
                        ? 'text-gray-400 line-through' 
                        : 'text-orange-600'
                    }`}>
                      {item.quantity}x {item.productName}
                    </div>
                    {item.notes && (
                      <div className={`text-xs ml-4 ${
                        isChecked ? 'text-gray-300 line-through' : 'text-gray-500'
                      }`}>
                        - {item.notes}
                      </div>
                    )}
                  </div>
                  <button
                    onClick={() => onToggleItem(itemKey)}
                    className={`w-6 h-6 rounded border-2 flex items-center justify-center transition ${
                      isChecked
                        ? 'bg-green-500 border-green-500 text-white'
                        : 'border-gray-300 hover:border-gray-400'
                    }`}
                  >
                    {isChecked && <Check className="w-4 h-4" />}
                  </button>
                </div>
              );
            })}
        </div>
      </div>

      {/* Footer Info */}
      <div className="p-4 border-t border-gray-200 bg-gray-50">
        {order.orderType === 'DELIVERY' && (order.deliveryStreet || order.customerAddress) && (
          <div className="flex items-center text-sm text-gray-600 mb-2">
            <MapPin className="w-4 h-4 mr-2" />
            Location: {order.deliveryStreet 
              ? `${order.deliveryStreet}, ${order.deliveryPostalCode} ${order.deliveryCity}`
              : order.customerAddress}
          </div>
        )}
        <div className="flex items-center text-sm text-gray-600">
          <CreditCard className="w-4 h-4 mr-2" />
          Payment: {order.paymentMethod || 'Card'}
        </div>
      </div>

      {/* Action Buttons */}
      <div className="p-4 border-t border-gray-200 flex space-x-3">
        <button
          onClick={onComplete}
          disabled={!allChecked}
          className={`flex-1 py-3 rounded-lg font-medium flex items-center justify-center transition ${
            allChecked
              ? 'bg-green-500 text-white hover:bg-green-600'
              : 'bg-gray-200 text-gray-400 cursor-not-allowed'
          }`}
        >
          COMPLETE
        </button>
        <button
          onClick={onDelete}
          className="flex-1 py-3 bg-red-500 text-white rounded-lg font-medium hover:bg-red-600 transition flex items-center justify-center"
        >
          DELETE
        </button>
      </div>
    </div>
  );
}

// =============================================================================
// Bottom Bar Component
// =============================================================================

interface BottomBarProps {
  sortMode: SortMode;
  onSortModeChange: (mode: SortMode) => void;
  onManageStore: () => void;
  onCreateOrder: () => void;
}

function BottomBar({ sortMode, onSortModeChange, onManageStore, onCreateOrder }: BottomBarProps) {
  return (
    <div className="bg-white border-t border-gray-200 px-4 py-3 flex items-center justify-between">
      <div className="flex space-x-3">
        <button
          onClick={onManageStore}
          className="px-4 py-2 bg-purple-100 text-purple-700 rounded-lg font-medium hover:bg-purple-200 transition flex items-center"
        >
          <Settings className="w-4 h-4 mr-2" />
          MANAGE STORE
        </button>
        
        <button
          onClick={() => onSortModeChange('time')}
          className={`px-4 py-2 rounded-lg font-medium transition flex items-center ${
            sortMode === 'time'
              ? 'bg-blue-100 text-blue-700'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          <Clock className="w-4 h-4 mr-2" />
          SORT BY TIME
        </button>
        
        <button
          onClick={() => onSortModeChange('item')}
          className={`px-4 py-2 rounded-lg font-medium transition flex items-center ${
            sortMode === 'item'
              ? 'bg-blue-100 text-blue-700'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          <ArrowUpDown className="w-4 h-4 mr-2" />
          SORT BY ITEM
        </button>
      </div>

      <button
        onClick={onCreateOrder}
        className="px-6 py-2 bg-blue-100 text-blue-700 rounded-lg font-medium hover:bg-blue-200 transition flex items-center"
      >
        <Plus className="w-4 h-4 mr-2" />
        CREATE NEW ORDER
      </button>
    </div>
  );
}

// =============================================================================
// Create Order Modal
// =============================================================================

interface CreateOrderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (order: Partial<Order>) => void;
}

function CreateOrderModal({ isOpen, onClose, onSubmit }: CreateOrderModalProps) {
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [customerAddress, setCustomerAddress] = useState('');
  const [orderType, setOrderType] = useState<'PICKUP' | 'DELIVERY'>('PICKUP');
  const [notes, setNotes] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      customerName,
      customerPhone,
      customerAddress: orderType === 'DELIVERY' ? customerAddress : undefined,
      orderType,
      notes,
      items: [], // Items would be added via a product selector
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md">
        <div className="p-4 border-b border-gray-200">
          <h2 className="text-lg font-bold">Create New Order</h2>
        </div>
        
        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Customer Name *
            </label>
            <input
              type="text"
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Phone
            </label>
            <input
              type="tel"
              value={customerPhone}
              onChange={(e) => setCustomerPhone(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Order Type
            </label>
            <div className="flex space-x-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  checked={orderType === 'PICKUP'}
                  onChange={() => setOrderType('PICKUP')}
                  className="mr-2"
                />
                Pickup
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  checked={orderType === 'DELIVERY'}
                  onChange={() => setOrderType('DELIVERY')}
                  className="mr-2"
                />
                Delivery
              </label>
            </div>
          </div>

          {orderType === 'DELIVERY' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Delivery Address *
              </label>
              <input
                type="text"
                value={customerAddress}
                onChange={(e) => setCustomerAddress(e.target.value)}
                required={orderType === 'DELIVERY'}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Notes
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2 border border-gray-300 rounded-lg font-medium hover:bg-gray-50 transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex-1 py-2 bg-blue-500 text-white rounded-lg font-medium hover:bg-blue-600 transition"
            >
              Create Order
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// =============================================================================
// Main KDS Page Component
// =============================================================================

function KDSPage() {
  const queryClient = useQueryClient();
  const [selectedOrder, setSelectedOrder] = useState<SelectedOrder | null>(null);
  const [sortMode, setSortMode] = useState<SortMode>('time');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Cleanup expired checks on mount
  useEffect(() => {
    cleanupExpiredChecks();
  }, []);

  // Fetch orders
  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: () => ordersApi.getOrders(),
    refetchInterval: 5000, // Refresh every 5 seconds for real-time updates
  });

  // Update status mutation
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: OrderStatus }) =>
      ordersApi.updateStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      setSelectedOrder(null);
    },
  });

  // Create order mutation
  const createOrderMutation = useMutation({
    mutationFn: (order: Partial<Order>) => ordersApi.createManualOrder(order),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });

  // Filter active orders (not delivered/picked up/cancelled)
  const activeOrders = useMemo(() => {
    return orders.filter(
      (order) => !['DELIVERED', 'PICKED_UP', 'CANCELLED'].includes(order.status)
    );
  }, [orders]);

  // Split by order type
  const pickupOrders = useMemo(() => {
    const filtered = activeOrders.filter((o) => o.orderType === 'PICKUP');
    if (sortMode === 'time') {
      return filtered.sort((a, b) => 
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
    }
    // Sort by item count
    return filtered.sort((a, b) => 
      a.items.reduce((s, i) => s + i.quantity, 0) - b.items.reduce((s, i) => s + i.quantity, 0)
    );
  }, [activeOrders, sortMode]);

  const deliveryOrders = useMemo(() => {
    const filtered = activeOrders.filter((o) => o.orderType === 'DELIVERY');
    if (sortMode === 'time') {
      return filtered.sort((a, b) => 
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
    }
    return filtered.sort((a, b) => 
      a.items.reduce((s, i) => s + i.quantity, 0) - b.items.reduce((s, i) => s + i.quantity, 0)
    );
  }, [activeOrders, sortMode]);

  // Handle order selection - restore checks from localStorage if available
  const handleSelectOrder = (order: Order) => {
    const storedChecks = getOrderChecks(order.id);
    const itemChecks: Record<string, boolean> = {};
    
    order.items.forEach((item, index) => {
      const itemKey = `${index}-${item.productId}`;
      // Use stored check value if available, otherwise false
      itemChecks[itemKey] = storedChecks?.[itemKey] ?? false;
    });
    
    setSelectedOrder({ ...order, itemChecks });
  };

  // Handle item toggle - save to localStorage
  const handleToggleItem = (itemKey: string) => {
    if (!selectedOrder) return;
    
    const newChecks = {
      ...selectedOrder.itemChecks,
      [itemKey]: !selectedOrder.itemChecks[itemKey],
    };
    
    // Save to localStorage
    saveOrderChecks(selectedOrder.id, newChecks);
    
    setSelectedOrder({
      ...selectedOrder,
      itemChecks: newChecks,
    });
  };

  // Handle complete order - remove checks from localStorage
  const handleComplete = () => {
    if (!selectedOrder) return;
    // Remove checks from localStorage
    removeOrderChecks(selectedOrder.id);
    // Use PICKED_UP for pickup orders, DELIVERED for delivery orders
    const completedStatus = selectedOrder.orderType === 'PICKUP' ? 'PICKED_UP' : 'DELIVERED';
    updateStatusMutation.mutate({ id: selectedOrder.id, status: completedStatus });
  };

  // Handle delete/cancel order - remove checks from localStorage
  const handleDelete = () => {
    if (!selectedOrder) return;
    if (confirm('Are you sure you want to cancel this order?')) {
      // Remove checks from localStorage
      removeOrderChecks(selectedOrder.id);
      updateStatusMutation.mutate({ id: selectedOrder.id, status: 'CANCELLED' });
    }
  };

  // Handle create order
  const handleCreateOrder = (order: Partial<Order>) => {
    createOrderMutation.mutate(order);
  };

  // Navigate to settings
  const handleManageStore = () => {
    window.location.href = '/settings';
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-100">
        <RefreshCw className="w-12 h-12 text-blue-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col bg-gray-100">
      {/* Main Content - 3 Column Layout */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Column - Pickup */}
        <div className="w-64 bg-white flex-shrink-0">
          <PickupColumn
            orders={pickupOrders}
            selectedOrderId={selectedOrder?.id ?? null}
            onSelectOrder={handleSelectOrder}
          />
        </div>

        {/* Center - Delivery Grid */}
        <div className="flex-1 bg-gray-50 border-x border-gray-200">
          <DeliveryGrid
            orders={deliveryOrders}
            selectedOrderId={selectedOrder?.id ?? null}
            onSelectOrder={handleSelectOrder}
          />
        </div>

        {/* Right Column - Detail Panel */}
        <div className="w-80 bg-white flex-shrink-0">
          <DetailPanel
            order={selectedOrder}
            onToggleItem={handleToggleItem}
            onComplete={handleComplete}
            onDelete={handleDelete}
          />
        </div>
      </div>

      {/* Bottom Bar */}
      <BottomBar
        sortMode={sortMode}
        onSortModeChange={setSortMode}
        onManageStore={handleManageStore}
        onCreateOrder={() => setShowCreateModal(true)}
      />

      {/* Create Order Modal */}
      <CreateOrderModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateOrder}
      />
    </div>
  );
}

export default KDSPage;
