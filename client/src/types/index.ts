// Order Types
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED';
export type OrderType = 'PICKUP' | 'DELIVERY';

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  notes?: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  customerId?: number;
  customerName: string;
  customerPhone?: string;
  customerAddress?: string;
  orderType: OrderType;
  status: OrderStatus;
  items: OrderItem[];
  subtotal: number;
  deliveryFee: number;
  total: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  estimatedReadyTime?: string;
}

// Product Types
export interface Category {
  id: number;
  name: string;
  displayOrder: number;
  isActive: boolean;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  categoryId: number;
  categoryName: string;
  imageUrl?: string;
  isAvailable: boolean;
  isActive: boolean;
  allergens?: string[];
  preparationTime?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  name: string;
  description?: string;
  price: number;
  categoryId: number;
  imageUrl?: string;
  allergens?: string[];
  preparationTime?: number;
}

export interface UpdateProductRequest extends CreateProductRequest {
  isAvailable?: boolean;
  isActive?: boolean;
}

// Receipt Types
export interface ReceiptItem {
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface Receipt {
  id: number;
  receiptNumber: string;
  orderId: number;
  orderType: OrderType;
  customerName: string;
  customerPhone?: string;
  customerAddress?: string;
  items: ReceiptItem[];
  subtotal: number;
  deliveryFee: number;
  total: number;
  paymentMethod: string;
  createdAt: string;
}

export interface DailyReport {
  date: string;
  totalReceipts: number;
  totalRevenue: number;
  pickupCount: number;
  deliveryCount: number;
  pickupRevenue: number;
  deliveryRevenue: number;
  averageOrderValue: number;
}

// Settings Types
export interface OpeningHours {
  id: number;
  dayOfWeek: string;
  dayName: string;
  openTime: string;
  closeTime: string;
  breakStartTime?: string;
  breakEndTime?: string;
  isClosed: boolean;
  deliveryStartTime?: string;
  deliveryEndTime?: string;
}

export interface DeliveryArea {
  id: number;
  postalCode: string;
  cityName: string;
  deliveryFee: number;
  minimumOrderValue: number;
  isActive: boolean;
}

export interface RestaurantSettings {
  id: number;
  restaurantName: string;
  address: string;
  phone?: string;
  email?: string;
  minimumOrderValue: number;
  deliveryFee: number;
  deliveryEnabled: boolean;
  pickupEnabled: boolean;
  openingHours: OpeningHours[];
}

// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
  };
}
