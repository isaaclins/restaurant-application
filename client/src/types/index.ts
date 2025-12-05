// Order Types
// Matches backend: PENDING, CONFIRMED, IN_PROGRESS, READY, DELIVERED, PICKED_UP, CANCELLED
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'READY' | 'DELIVERED' | 'PICKED_UP' | 'CANCELLED';
export type OrderType = 'PICKUP' | 'DELIVERY' | 'DINE_IN';

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
  customerEmail?: string;
  customerPhone?: string;
  // Delivery address fields
  deliveryStreet?: string;
  deliveryCity?: string;
  deliveryPostalCode?: string;
  // Computed for backward compatibility
  customerAddress?: string;
  orderType: OrderType;
  status: OrderStatus;
  items: OrderItem[];
  totalPrice: number;
  // Aliases for compatibility
  subtotal?: number;
  deliveryFee?: number;
  total?: number;
  paymentMethod?: string;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
  estimatedDelivery?: string;
  // Alias
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
  orderNumber?: string;
  orderType: OrderType;
  customerName: string;
  customerEmail?: string;
  customerPhone?: string;
  customerAddress?: string;
  restaurantName?: string;
  restaurantAddress?: string;
  restaurantPhone?: string;
  vatNumber?: string;
  items: ReceiptItem[];
  subtotal: number;
  vatAmount?: number;
  vatRate?: number;
  deliveryFee?: number;
  discount?: number;
  totalAmount: number;
  paymentMethod?: string;
  createdAt: string;
  pdfAvailable?: boolean;
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
