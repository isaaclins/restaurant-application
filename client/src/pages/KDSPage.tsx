import { useState, useEffect, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '../api/orders';
import { productsApi } from '../api/products';
import { receiptsApi } from '../api/receipts';
import { Order, OrderStatus, Product } from '../types';
import { differenceInSeconds, differenceInMinutes, addMinutes, format } from 'date-fns';
import {
  Clock,
  MapPin,
  CreditCard,
  Check,
  Minus,
  Plus,
  Search,
  Settings,
  ArrowUpDown,
  RefreshCw,
  User,
  Phone,
  Mail,
  Download,
  Truck,
  Store,
} from 'lucide-react';

// =============================================================================
// Types
// =============================================================================

interface SelectedOrder extends Order {
  itemChecks: Record<string, boolean>;
}

type SortMode = 'time' | 'item';
type SortDirection = 'asc' | 'desc';

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

// Helper to get status label
function getStatusLabel(status: OrderStatus): string {
  switch (status) {
    case 'PENDING': return 'Pending';
    case 'CONFIRMED': return 'Confirmed';
    case 'IN_PROGRESS': return 'In Progress';
    case 'READY': return 'Ready';
    case 'DELIVERED': return 'Delivered';
    case 'PICKED_UP': return 'Picked Up';
    case 'CANCELLED': return 'Cancelled';
    default: return status;
  }
}

// Helper to get status color
function getStatusColor(status: OrderStatus): string {
  switch (status) {
    case 'PENDING': return 'bg-yellow-100 text-yellow-800';
    case 'CONFIRMED': return 'bg-blue-100 text-blue-800';
    case 'IN_PROGRESS': return 'bg-purple-100 text-purple-800';
    case 'READY': return 'bg-green-100 text-green-800';
    default: return 'bg-gray-100 text-gray-800';
  }
}

interface DetailPanelPropsWithProducts extends DetailPanelProps {
  products: Product[];
}

function DetailPanel({ order, onToggleItem, onComplete, onDelete, products }: DetailPanelPropsWithProducts) {
  const [timeInfo, setTimeInfo] = useState(order ? getTimeInfo(order) : null);
  
  // Build a map of productId -> categoryName for sorting
  const productCategoryMap = useMemo(() => {
    const map: Record<number, string> = {};
    products.forEach(p => {
      map[p.id] = p.categoryName || 'Other';
    });
    return map;
  }, [products]);
  
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
        {/* Status Badge */}
        <div className="text-center mt-2">
          <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStatusColor(order.status)}`}>
            {getStatusLabel(order.status)}
          </span>
        </div>
      </div>

      {/* Items List - Grouped by Category */}
      <div className="flex-1 overflow-auto p-4">
        <div className="space-y-4">
          {/* Group items by category, then sort: unchecked first, checked at bottom within each category */}
          {(() => {
            // Add category info to items
            const itemsWithCategory = order.items.map((item, index) => ({
              item,
              index,
              itemKey: `${index}-${item.productId}`,
              category: productCategoryMap[item.productId] || 'Other',
            }));

            // Get unique categories
            const categories = [...new Set(itemsWithCategory.map(i => i.category))];
            
            // Sort categories: incomplete categories first (alphabetically), then completed categories (alphabetically)
            categories.sort((a, b) => {
              const aItems = itemsWithCategory.filter(i => i.category === a);
              const bItems = itemsWithCategory.filter(i => i.category === b);
              const aAllChecked = aItems.every(i => order.itemChecks[i.itemKey]);
              const bAllChecked = bItems.every(i => order.itemChecks[i.itemKey]);
              
              // Completed categories go to bottom
              if (aAllChecked !== bAllChecked) {
                return aAllChecked ? 1 : -1;
              }
              // Within same completion status, sort alphabetically
              return a.localeCompare(b);
            });

            return categories.map(category => {
              const categoryItems = itemsWithCategory
                .filter(i => i.category === category)
                .sort((a, b) => {
                  // First sort by checked status
                  const aChecked = order.itemChecks[a.itemKey] ? 1 : 0;
                  const bChecked = order.itemChecks[b.itemKey] ? 1 : 0;
                  if (aChecked !== bChecked) return aChecked - bChecked;
                  // Then alphabetically by product name
                  return a.item.productName.localeCompare(b.item.productName);
                });
              
              // Check if all items in this category are completed
              const allCategoryItemsChecked = categoryItems.every(i => order.itemChecks[i.itemKey]);

              return (
                <div key={category} className={`mb-4 transition-all duration-300 ${allCategoryItemsChecked ? 'opacity-50' : ''}`}>
                  <div className={`text-xs font-semibold uppercase tracking-wider mb-2 border-b pb-1 flex items-center justify-between ${
                    allCategoryItemsChecked 
                      ? 'text-gray-400 border-gray-100' 
                      : 'text-gray-500 border-gray-200'
                  }`}>
                    <span>{category}</span>
                    {allCategoryItemsChecked && <Check className="w-3 h-3 text-green-500" />}
                  </div>
                  <div className="space-y-3">
                    {categoryItems.map(({ item, itemKey }) => {
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
              );
            });
          })()}
        </div>
      </div>

      {/* Footer Info - Enhanced with more details */}
      <div className="p-4 border-t border-gray-200 bg-gray-50 space-y-3">
        {/* Order Type Badge */}
        <div className="flex items-center justify-between">
          <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
            order.orderType === 'DELIVERY' 
              ? 'bg-purple-100 text-purple-700' 
              : order.orderType === 'DINE_IN'
              ? 'bg-blue-100 text-blue-700'
              : 'bg-orange-100 text-orange-700'
          }`}>
            {order.orderType === 'DELIVERY' ? (
              <><Truck className="w-4 h-4 mr-1" /> Delivery</>
            ) : order.orderType === 'DINE_IN' ? (
              <><Store className="w-4 h-4 mr-1" /> Dine-In</>
            ) : (
              <><Store className="w-4 h-4 mr-1" /> Pickup</>
            )}
          </div>
          <div className="text-sm text-gray-500">
            {format(new Date(order.createdAt), 'HH:mm')}
          </div>
        </div>

        {/* Customer Info */}
        <div className="space-y-1">
          <div className="flex items-center text-sm text-gray-700">
            <User className="w-4 h-4 mr-2 text-gray-400" />
            <span className="font-medium">{order.customerName}</span>
          </div>
          {order.customerPhone && (
            <div className="flex items-center text-sm text-gray-600">
              <Phone className="w-4 h-4 mr-2 text-gray-400" />
              {order.customerPhone}
            </div>
          )}
          {order.customerEmail && (
            <div className="flex items-center text-sm text-gray-600">
              <Mail className="w-4 h-4 mr-2 text-gray-400" />
              {order.customerEmail}
            </div>
          )}
        </div>

        {/* Delivery Location (for delivery orders) */}
        {order.orderType === 'DELIVERY' && (
          <div className="p-2 bg-purple-50 rounded-lg border border-purple-100">
            <div className="flex items-start text-sm text-purple-800">
              <MapPin className="w-4 h-4 mr-2 mt-0.5 text-purple-500 flex-shrink-0" />
              <div>
                <div className="font-medium">Delivery Address</div>
                <div className="text-purple-700">
                  {order.deliveryStreet 
                    ? `${order.deliveryStreet}`
                    : order.customerAddress || 'No address provided'}
                </div>
                {(order.deliveryPostalCode || order.deliveryCity) && (
                  <div className="text-purple-600">
                    {order.deliveryPostalCode} {order.deliveryCity}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Pickup/Dine-in notice */}
        {order.orderType === 'PICKUP' && (
          <div className="p-2 bg-orange-50 rounded-lg border border-orange-100">
            <div className="flex items-center text-sm text-orange-800">
              <Store className="w-4 h-4 mr-2 text-orange-500" />
              <span>Customer will pick up at counter</span>
            </div>
          </div>
        )}

        {/* Payment Info */}
        <div className="flex items-center justify-between text-sm">
          <div className="flex items-center text-gray-600">
            <CreditCard className="w-4 h-4 mr-2 text-gray-400" />
            {order.paymentMethod || 'Card'}
          </div>
          <div className="font-semibold text-gray-800">
            CHF {(order.totalPrice || 0).toFixed(2)}
          </div>
        </div>

        {/* Notes if any */}
        {order.notes && (
          <div className="p-2 bg-yellow-50 rounded-lg border border-yellow-200">
            <div className="text-sm text-yellow-800">
              <span className="font-medium">Note: </span>{order.notes}
            </div>
          </div>
        )}
      </div>

      {/* Receipt & Action Buttons */}
      <div className="p-4 border-t border-gray-200 space-y-2">
        {/* View/Download Receipt (only for completed orders) */}
        {['DELIVERED', 'PICKED_UP'].includes(order.status) && (
          <button
            onClick={async () => {
              try {
                // Try to download receipt PDF by order ID
                const receipts = await receiptsApi.getReceipts();
                const receipt = receipts.find(r => r.orderId === order.id);
                if (receipt) {
                  await receiptsApi.downloadReceiptPdf(receipt.id, receipt.receiptNumber);
                } else {
                  alert('Receipt not found for this order');
                }
              } catch (err) {
                console.error('Failed to download receipt:', err);
                alert('Failed to download receipt. Please try again.');
              }
            }}
            className="w-full py-2 bg-blue-50 text-blue-700 rounded-lg font-medium hover:bg-blue-100 transition flex items-center justify-center"
          >
            <Download className="w-4 h-4 mr-2" />
            Download Receipt
          </button>
        )}
        {/* Complete & Delete Buttons - Complete works from IN_PROGRESS or READY */}
        <div className="flex space-x-3">
          <button
            onClick={onComplete}
            disabled={!allChecked || !['IN_PROGRESS', 'READY'].includes(order.status)}
            className={`flex-1 py-3 rounded-lg font-medium flex items-center justify-center transition ${
              allChecked && ['IN_PROGRESS', 'READY'].includes(order.status)
                ? 'bg-green-500 text-white hover:bg-green-600'
                : 'bg-gray-200 text-gray-400 cursor-not-allowed'
            }`}
            title={!['IN_PROGRESS', 'READY'].includes(order.status) ? `Order must be IN_PROGRESS or READY (current: ${order.status})` : ''}
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
    </div>
  );
}

// =============================================================================
// Bottom Bar Component
// =============================================================================

interface BottomBarProps {
  sortMode: SortMode;
  sortDirection: SortDirection;
  onSortToggle: (mode: SortMode) => void;
  onManageStore: () => void;
  onCreateOrder: () => void;
}

function BottomBar({ sortMode, sortDirection, onSortToggle, onManageStore, onCreateOrder }: BottomBarProps) {
  // Get arrow indicator based on direction
  const getArrowIndicator = (mode: SortMode) => {
    if (sortMode !== mode) return null;
    return sortDirection === 'asc' ? '↑' : '↓';
  };

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
          onClick={() => onSortToggle('time')}
          className={`px-4 py-2 rounded-lg font-medium transition flex items-center ${
            sortMode === 'time'
              ? 'bg-blue-100 text-blue-700'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          <Clock className="w-4 h-4 mr-2" />
          SORT BY TIME {getArrowIndicator('time')}
        </button>
        
        <button
          onClick={() => onSortToggle('item')}
          className={`px-4 py-2 rounded-lg font-medium transition flex items-center ${
            sortMode === 'item'
              ? 'bg-blue-100 text-blue-700'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          <ArrowUpDown className="w-4 h-4 mr-2" />
          SORT BY ITEM {getArrowIndicator('item')}
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
  products: Product[];
}

function CreateOrderModal({ isOpen, onClose, onSubmit, products }: CreateOrderModalProps) {
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [customerAddress, setCustomerAddress] = useState('');
  const [orderType, setOrderType] = useState<'PICKUP' | 'DELIVERY'>('PICKUP');
  const [notes, setNotes] = useState('');
  const [productSearch, setProductSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<string>('All');
  const [selectedItems, setSelectedItems] = useState<Record<number, number>>({});

  const categories = useMemo(
    () => ['All', ...Array.from(new Set(products.map((p) => p.categoryName || 'Other')))],
    [products]
  );

  const filteredProducts = useMemo(() => {
    const availableProducts = products.filter(
      (product) => (product.active ?? true) && (product.available ?? product.isAvailable ?? true)
    );

    const search = productSearch.trim().toLowerCase();

    return availableProducts.filter((product) => {
      const matchesCategory =
        categoryFilter === 'All' || (product.categoryName || 'Other') === categoryFilter;
      const matchesSearch =
        search.length === 0 ||
        product.name.toLowerCase().includes(search) ||
        (product.description || '').toLowerCase().includes(search);

      return matchesCategory && matchesSearch;
    });
  }, [products, categoryFilter, productSearch]);

  const selectedProducts = useMemo(
    () => products.filter((product) => selectedItems[product.id]),
    [products, selectedItems]
  );

  const totalPrice = useMemo(() => {
    return selectedProducts.reduce((sum, product) => {
      const qty = selectedItems[product.id] || 0;
      const price = typeof product.price === 'number' ? product.price : 0;
      return sum + qty * price;
    }, 0);
  }, [selectedProducts, selectedItems]);

  const handleQuantityChange = (productId: number, delta: number) => {
    setSelectedItems((prev) => {
      const next = { ...prev };
      const newQty = (prev[productId] || 0) + delta;

      if (newQty <= 0) {
        delete next[productId];
      } else {
        next[productId] = newQty;
      }

      return next;
    });
  };

  const resetForm = () => {
    setCustomerName('');
    setCustomerPhone('');
    setCustomerAddress('');
    setOrderType('PICKUP');
    setNotes('');
    setProductSearch('');
    setCategoryFilter('All');
    setSelectedItems({});
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!customerName.trim()) {
      alert('Customer name is required');
      return;
    }

    if (orderType === 'DELIVERY' && !customerAddress.trim()) {
      alert('Delivery address is required for delivery orders');
      return;
    }

    if (selectedProducts.length === 0) {
      alert('Select at least one product for the order');
      return;
    }

    const items = selectedProducts.map((product) => {
      const quantity = selectedItems[product.id] || 0;
      const price = typeof product.price === 'number' ? product.price : 0;
      const itemTotal = Number((price * quantity).toFixed(2));

      return {
        id: product.id,
        productId: product.id,
        productName: product.name,
        quantity,
        unitPrice: price,
        totalPrice: itemTotal,
      };
    });

    const orderPayload: Partial<Order> = {
      customerName: customerName.trim(),
      customerPhone: customerPhone || undefined,
      customerAddress: orderType === 'DELIVERY' ? customerAddress.trim() : undefined,
      orderType,
      notes: notes.trim() || undefined,
      items,
      totalPrice: Number(totalPrice.toFixed(2)),
    };

    if (orderType === 'DELIVERY') {
      orderPayload.deliveryAddress = {
        street: customerAddress.trim(),
      };
    }

    onSubmit(orderPayload);
    handleClose();
  };

  return (
    <div className="fixed inset-0 bg-black/60 z-50 flex">
      <div className="bg-white w-full h-full max-w-none lg:max-w-6xl mx-auto rounded-none lg:rounded-2xl shadow-2xl flex flex-col overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <div>
            <div className="text-xl font-bold text-gray-900">Create New Order</div>
            <div className="text-sm text-gray-500">Full-screen quick entry for counter staff</div>
          </div>
          <button
            type="button"
            onClick={handleClose}
            className="px-4 py-2 text-gray-600 hover:text-gray-900 text-lg"
          >
            Close
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex-1 flex flex-col overflow-hidden">
          <div className="flex-1 overflow-hidden px-6 pb-6 pt-4">
            <div className="h-full flex flex-col lg:flex-row gap-6">
              {/* Customer & order info */}
              <div className="bg-gray-50 border border-gray-200 rounded-2xl p-4 space-y-4 w-full lg:max-w-sm">
                <div>
                  <label className="block text-base font-semibold text-gray-800 mb-1">
                    Customer Name *
                  </label>
                  <input
                    type="text"
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    required
                    className="w-full px-4 py-3 text-lg border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-base font-semibold text-gray-800 mb-1">
                    Phone
                  </label>
                  <input
                    type="tel"
                    value={customerPhone}
                    onChange={(e) => setCustomerPhone(e.target.value)}
                    className="w-full px-4 py-3 text-lg border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-base font-semibold text-gray-800 mb-2">
                    Order Type
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    <button
                      type="button"
                      onClick={() => setOrderType('PICKUP')}
                      className={`py-3 rounded-xl border text-lg font-semibold transition ${
                        orderType === 'PICKUP'
                          ? 'border-blue-500 bg-blue-50 text-blue-700'
                          : 'border-gray-300 text-gray-700 hover:border-blue-300'
                      }`}
                    >
                      Pickup
                    </button>
                    <button
                      type="button"
                      onClick={() => setOrderType('DELIVERY')}
                      className={`py-3 rounded-xl border text-lg font-semibold transition ${
                        orderType === 'DELIVERY'
                          ? 'border-blue-500 bg-blue-50 text-blue-700'
                          : 'border-gray-300 text-gray-700 hover:border-blue-300'
                      }`}
                    >
                      Delivery
                    </button>
                  </div>
                </div>

                {orderType === 'DELIVERY' && (
                  <div>
                    <label className="block text-base font-semibold text-gray-800 mb-1">
                      Delivery Address *
                    </label>
                    <input
                      type="text"
                      value={customerAddress}
                      onChange={(e) => setCustomerAddress(e.target.value)}
                      required={orderType === 'DELIVERY'}
                      className="w-full px-4 py-3 text-lg border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                )}

                <div>
                  <label className="block text-base font-semibold text-gray-800 mb-1">
                    Notes
                  </label>
                  <textarea
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    rows={4}
                    className="w-full px-4 py-3 text-lg border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              </div>

              {/* Products */}
              <div className="flex-1 flex flex-col bg-gray-50 border border-gray-200 rounded-2xl p-4 overflow-hidden">
                <div className="flex flex-col md:flex-row md:items-center md:space-x-3 space-y-3 md:space-y-0 mb-3">
                  <div className="relative flex-1">
                    <Search className="w-5 h-5 text-gray-400 absolute left-4 top-3.5" />
                    <input
                      type="text"
                      value={productSearch}
                      onChange={(e) => setProductSearch(e.target.value)}
                      placeholder="Search products by name or description"
                      className="w-full pl-11 pr-4 py-3 text-lg border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  <select
                    value={categoryFilter}
                    onChange={(e) => setCategoryFilter(e.target.value)}
                    className="px-4 py-3 text-lg border border-gray-300 rounded-xl bg-white focus:ring-2 focus:ring-blue-500 focus:border-transparent w-full md:w-52"
                  >
                    {categories.map((category) => (
                      <option key={category} value={category}>
                        {category}
                      </option>
                    ))}
                  </select>
                  <div className="px-4 py-3 bg-white rounded-xl border border-gray-200 text-lg font-semibold text-gray-800">
                    {selectedProducts.length} items
                  </div>
                </div>

                <div className="flex-1 overflow-hidden flex flex-col gap-3">
                  <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-3 overflow-auto pr-1">
                    {filteredProducts.length === 0 ? (
                      <div className="col-span-full text-center text-gray-400 text-lg py-6">
                        No matching products
                      </div>
                    ) : (
                      filteredProducts.map((product) => {
                        const quantity = selectedItems[product.id] || 0;
                        const price = typeof product.price === 'number' ? product.price : 0;
                        return (
                          <button
                            key={product.id}
                            type="button"
                            onClick={() => handleQuantityChange(product.id, 1)}
                            className={`text-left border rounded-2xl p-4 bg-white shadow-sm transition focus:outline-none ${
                              quantity > 0 ? 'ring-2 ring-blue-300 border-blue-200' : 'border-gray-200 hover:border-blue-200'
                            }`}
                          >
                            <div className="text-lg font-bold text-gray-900 line-clamp-1">
                              {product.name}
                            </div>
                            <div className="text-sm text-gray-500 line-clamp-1">
                              {product.categoryName || 'Other'}
                            </div>
                            <div className="text-lg text-orange-600 font-semibold mt-1">
                              CHF {price.toFixed(2)}
                            </div>
                            <div className="flex items-center justify-between mt-3">
                              <button
                                type="button"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleQuantityChange(product.id, -1);
                                }}
                                disabled={quantity === 0}
                                className={`w-11 h-11 flex items-center justify-center rounded-xl border text-xl font-bold ${
                                  quantity === 0
                                    ? 'text-gray-300 border-gray-200'
                                    : 'text-gray-700 border-gray-300 hover:border-gray-400'
                                }`}
                              >
                                <Minus className="w-5 h-5" />
                              </button>
                              <span className="text-2xl font-semibold text-gray-900">{quantity}</span>
                              <button
                                type="button"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleQuantityChange(product.id, 1);
                                }}
                                className="w-11 h-11 flex items-center justify-center rounded-xl border border-blue-500 text-blue-600 hover:bg-blue-50 text-xl font-bold"
                              >
                                <Plus className="w-5 h-5" />
                              </button>
                            </div>
                          </button>
                        );
                      })
                    )}
                  </div>

                  <div className="bg-white border border-gray-200 rounded-2xl p-4 space-y-3 max-h-56 overflow-auto">
                    {selectedProducts.length === 0 ? (
                      <div className="text-lg text-gray-500 text-center">
                        No products selected yet. Tap to add.
                      </div>
                    ) : (
                      <>
                        {selectedProducts.map((product) => {
                          const quantity = selectedItems[product.id] || 0;
                          const price = typeof product.price === 'number' ? product.price : 0;
                          return (
                            <div
                              key={`selected-${product.id}`}
                              className="flex items-center justify-between text-lg"
                            >
                              <div>
                                <div className="font-semibold text-gray-900">{product.name}</div>
                                <div className="text-sm text-gray-500">
                                  {quantity} x CHF {price.toFixed(2)}
                                </div>
                              </div>
                              <div className="flex items-center space-x-2">
                                <button
                                  type="button"
                                  onClick={() => handleQuantityChange(product.id, -1)}
                                  className="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-300 text-gray-700 hover:border-gray-400 text-xl"
                                >
                                  <Minus className="w-5 h-5" />
                                </button>
                                <span className="font-semibold text-gray-900 w-8 text-center">
                                  {quantity}
                                </span>
                                <button
                                  type="button"
                                  onClick={() => handleQuantityChange(product.id, 1)}
                                  className="w-10 h-10 flex items-center justify-center rounded-lg border border-blue-500 text-blue-600 hover:bg-blue-50 text-xl"
                                >
                                  <Plus className="w-5 h-5" />
                                </button>
                                <div className="w-24 text-right font-semibold text-gray-900">
                                  CHF {(price * quantity).toFixed(2)}
                                </div>
                              </div>
                            </div>
                          );
                        })}
                        <div className="flex items-center justify-between pt-2 border-t border-gray-200 text-xl font-bold">
                          <span>Total</span>
                          <span>CHF {totalPrice.toFixed(2)}</span>
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="px-6 py-4 border-t border-gray-200 bg-white flex flex-col md:flex-row gap-3 md:items-center">
            <div className="w-full md:w-1/3">
              <div className="px-4 py-3 bg-blue-50 text-blue-800 rounded-xl text-lg font-bold flex items-center justify-between">
                <span>Total</span>
                <span>CHF {totalPrice.toFixed(2)}</span>
              </div>
            </div>
            <button
              type="button"
              onClick={handleClose}
              className="w-full md:w-1/3 py-4 border border-gray-300 rounded-xl text-lg font-semibold text-gray-800 hover:bg-gray-50 transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={selectedProducts.length === 0}
              className={`w-full md:flex-1 py-4 rounded-xl text-lg font-semibold transition ${
                selectedProducts.length === 0
                  ? 'bg-blue-200 text-white cursor-not-allowed'
                  : 'bg-blue-600 text-white hover:bg-blue-700'
              }`}
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
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Toggle sort mode or direction
  const handleSortToggle = (mode: SortMode) => {
    if (sortMode === mode) {
      // Same mode clicked - toggle direction
      setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      // Different mode - switch to it with ascending
      setSortMode(mode);
      setSortDirection('asc');
    }
  };

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

  // Fetch products for category grouping
  const { data: products = [] } = useQuery({
    queryKey: ['products'],
    queryFn: () => productsApi.getProducts(),
    staleTime: 60000, // Cache for 1 minute
  });

  // Update status mutation (don't clear selection for auto-updates)
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: OrderStatus }) =>
      ordersApi.updateStatus(id, status),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      // Only clear selection if it's a completion/cancellation
      if (['DELIVERED', 'PICKED_UP', 'CANCELLED'].includes(variables.status)) {
        setSelectedOrder(null);
      }
    },
  });

  // Auto-confirm PENDING orders when they appear in the KDS
  useEffect(() => {
    const pendingOrders = orders.filter(o => o.status === 'PENDING');
    pendingOrders.forEach(order => {
      updateStatusMutation.mutate({ id: order.id, status: 'CONFIRMED' });
    });
  }, [orders]);

  // Create order mutation
  const createOrderMutation = useMutation({
    mutationFn: (order: Partial<Order>) => ordersApi.createManualOrder(order),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });

  // Delete order mutation
  const deleteOrderMutation = useMutation({
    mutationFn: (id: number) => ordersApi.deleteOrder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      setSelectedOrder(null);
    },
  });

  // Filter active orders (not delivered/picked up/cancelled) and with items
  const activeOrders = useMemo(() => {
    return orders.filter(
      (order) => !['DELIVERED', 'PICKED_UP', 'CANCELLED'].includes(order.status) &&
                 order.items && order.items.length > 0
    );
  }, [orders]);

  // Split by order type
  const pickupOrders = useMemo(() => {
    const filtered = activeOrders.filter((o) => o.orderType === 'PICKUP');
    const multiplier = sortDirection === 'asc' ? 1 : -1;
    
    if (sortMode === 'time') {
      // Sort by estimatedDelivery (timer)
      return filtered.sort((a, b) => {
        const etaA = a.estimatedDelivery 
          ? new Date(a.estimatedDelivery).getTime()
          : a.estimatedReadyTime 
            ? new Date(a.estimatedReadyTime).getTime()
            : new Date(a.createdAt).getTime() + 15 * 60 * 1000;
        const etaB = b.estimatedDelivery 
          ? new Date(b.estimatedDelivery).getTime()
          : b.estimatedReadyTime 
            ? new Date(b.estimatedReadyTime).getTime()
            : new Date(b.createdAt).getTime() + 15 * 60 * 1000;
        return (etaA - etaB) * multiplier;
      });
    }
    // Sort by item count
    return filtered.sort((a, b) => 
      (a.items.reduce((s, i) => s + i.quantity, 0) - b.items.reduce((s, i) => s + i.quantity, 0)) * multiplier
    );
  }, [activeOrders, sortMode, sortDirection]);

  const deliveryOrders = useMemo(() => {
    const filtered = activeOrders.filter((o) => o.orderType === 'DELIVERY');
    const multiplier = sortDirection === 'asc' ? 1 : -1;
    
    if (sortMode === 'time') {
      // Sort by estimatedDelivery (timer)
      return filtered.sort((a, b) => {
        const etaA = a.estimatedDelivery 
          ? new Date(a.estimatedDelivery).getTime()
          : a.estimatedReadyTime 
            ? new Date(a.estimatedReadyTime).getTime()
            : new Date(a.createdAt).getTime() + 15 * 60 * 1000;
        const etaB = b.estimatedDelivery 
          ? new Date(b.estimatedDelivery).getTime()
          : b.estimatedReadyTime 
            ? new Date(b.estimatedReadyTime).getTime()
            : new Date(b.createdAt).getTime() + 15 * 60 * 1000;
        return (etaA - etaB) * multiplier;
      });
    }
    return filtered.sort((a, b) => 
      (a.items.reduce((s, i) => s + i.quantity, 0) - b.items.reduce((s, i) => s + i.quantity, 0)) * multiplier
    );
  }, [activeOrders, sortMode, sortDirection]);

  // Handle order selection - restore checks from localStorage if available
  // Auto-start: If order is CONFIRMED, automatically set to IN_PROGRESS when selected
  const handleSelectOrder = (order: Order) => {
    const storedChecks = getOrderChecks(order.id);
    const itemChecks: Record<string, boolean> = {};
    
    order.items.forEach((item, index) => {
      const itemKey = `${index}-${item.productId}`;
      // Use stored check value if available, otherwise false
      itemChecks[itemKey] = storedChecks?.[itemKey] ?? false;
    });
    
    // Auto-start: Set to IN_PROGRESS if CONFIRMED
    if (order.status === 'CONFIRMED') {
      updateStatusMutation.mutate({ id: order.id, status: 'IN_PROGRESS' });
      setSelectedOrder({ ...order, status: 'IN_PROGRESS', itemChecks });
    } else {
      setSelectedOrder({ ...order, itemChecks });
    }
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
  // Note: Can complete from IN_PROGRESS (skipping READY) or from READY
  const handleComplete = () => {
    if (!selectedOrder) return;
    // Allow completion from IN_PROGRESS or READY
    if (!['IN_PROGRESS', 'READY'].includes(selectedOrder.status)) {
      alert(`Order must be IN_PROGRESS or READY to complete. Current status: ${selectedOrder.status}`);
      return;
    }
    // Remove checks from localStorage
    removeOrderChecks(selectedOrder.id);
    // Use PICKED_UP for pickup orders, DELIVERED for delivery orders
    const completedStatus = selectedOrder.orderType === 'PICKUP' ? 'PICKED_UP' : 'DELIVERED';
    updateStatusMutation.mutate({ id: selectedOrder.id, status: completedStatus });
  };

  // Handle delete/cancel order - remove checks from localStorage
  const handleDelete = () => {
    if (!selectedOrder) return;
    const message = `Do you really want to delete order #${selectedOrder.orderNumber}?\nThis action cannot be undone.`;
    if (window.confirm(message)) {
      removeOrderChecks(selectedOrder.id);
      deleteOrderMutation.mutate(selectedOrder.id);
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
            products={products}
          />
        </div>
      </div>

      {/* Bottom Bar */}
      <BottomBar
        sortMode={sortMode}
        sortDirection={sortDirection}
        onSortToggle={handleSortToggle}
        onManageStore={handleManageStore}
        onCreateOrder={() => setShowCreateModal(true)}
      />

      {/* Create Order Modal */}
      <CreateOrderModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateOrder}
        products={products}
      />
    </div>
  );
}

export default KDSPage;
