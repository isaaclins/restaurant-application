export interface OpeningHoursEntry {
  id?: number;
  day?: string; // legacy fallback
  dayName?: string;
  dayOfWeek?: string;
  openTime?: string | null;
  closeTime?: string | null;
  isClosed?: boolean;
}

export interface DeliveryArea {
  id: number;
  postalCode: string;
  city?: string;
  cityName?: string; // legacy alias
  deliveryFee?: number;
  minimumOrderValue?: number;
  minimumOrder?: number; // legacy alias
}

export interface RestaurantSettings {
  id?: number;
  restaurantName?: string;
  slogan?: string;
  description?: string;
  logoUrl?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
  phone?: string;
  email?: string;
  website?: string;
  vatNumber?: string;
  vatRate?: number;
  businessRegistration?: string;
  minimumOrderValue?: number;
  deliveryFee?: number;
  freeDeliveryThreshold?: number;
  estimatedDeliveryMinutes?: number;
  estimatedPickupMinutes?: number;
  deliveryEnabled?: boolean;
  pickupEnabled?: boolean;
  dineInEnabled?: boolean;
  onlinePaymentEnabled?: boolean;
  cashPaymentEnabled?: boolean;
  cardPaymentEnabled?: boolean;
  isOpen?: boolean;
  closedMessage?: string;
  isCurrentlyOpen?: boolean;
  openingHours?: OpeningHoursEntry[];
  deliveryAreas?: DeliveryArea[];
  updatedAt?: string;
}
