// ============================================
// PRODUCT TYPES
// ============================================

export interface ProductExtra {
  id: number;
  name: string;
  price: number;
}

export interface ProductSizes {
  S?: number;
  M?: number;
  L?: number;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  category?: string;
  categoryId?: number;
  categoryName?: string;
  imageUrl?: string;
  available?: boolean;
  isAvailable?: boolean;
  allergens?: string[];
  sizes?: ProductSizes | null;
  extras?: ProductExtra[] | null;
}

export interface Category {
  id: number;
  name: string;
  displayOrder: number;
  isActive: boolean;
}

// ============================================
// CART TYPES
// ============================================

export interface CartItem {
  itemId: string;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  size?: string | null;
  notes?: string | null;
  totalPrice?: number;
}

export interface Cart {
  sessionId: string;
  items: CartItem[];
  totalItems: number;
  totalPrice: number;
}

export interface AddToCartRequest {
  productId: number;
  quantity: number;
  size?: string;
  notes?: string;
}

// ============================================
// ORDER TYPES
// ============================================

export type OrderType = 'DELIVERY' | 'PICKUP';
export type OrderStatus = 'NEW' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
export type PaymentMethod = 'CARD' | 'TWINT' | 'CASH';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';

export interface OrderItemExtra {
  name: string;
  price: number;
}

export interface OrderItem {
  id?: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  notes?: string;
}

export interface DeliveryAddress {
  street: string;
  city: string;
  postalCode: string;
  notes?: string;
}

export interface OrderCustomer {
  name: string;
  phone: string;
  email?: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  orderType: OrderType;
  status: OrderStatus;
  // Flat customer fields (backend format)
  customerName: string;
  customerEmail?: string;
  customerPhone?: string;
  customerId?: number;
  // Delivery address fields (flat in backend)
  deliveryStreet?: string;
  deliveryCity?: string;
  deliveryPostalCode?: string;
  items: OrderItem[];
  totalPrice: number;
  paymentMethod?: string;
  notes?: string;
  estimatedDelivery?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface OrderItemRequest {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  size?: string;
  notes?: string;
}

export interface CreateOrderRequest {
  customerName: string;
  customerEmail?: string;
  customerPhone?: string;
  orderType: OrderType;
  deliveryAddress?: DeliveryAddress;
  paymentMethod: string;
  notes?: string;
  items: OrderItemRequest[];
  totalPrice: number;
}

// ============================================
// CUSTOMER / AUTH TYPES
// ============================================

export interface CustomerAddress {
  id: string;
  street: string;
  city: string;
  postalCode: string;
  isDefault: boolean;
}

export interface Customer {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  addresses: CustomerAddress[];
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  address?: {
    street: string;
    city: string;
    postalCode: string;
  };
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  user: Customer;
}
