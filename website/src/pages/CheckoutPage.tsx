import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ordersApi } from '../api/orders';
import { useCartStore } from '../stores/cartStore';
import { useAuthStore } from '../stores/authStore';
import type { OrderType, CreateOrderRequest } from '../types';

/**
 * CheckoutPage - Customer info form + order submission
 * 
 * Data: cart, logged-in user (optional)
 * Actions: Submit order, navigate to confirmation
 */
export default function CheckoutPage() {
  const navigate = useNavigate();
  const { items, totalPrice, clearCart } = useCartStore();
  const { user, isAuthenticated } = useAuthStore();
  const addresses = user?.addresses || [];

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form state
  const [orderType, setOrderType] = useState<OrderType>('PICKUP');
  const [paymentMethod, setPaymentMethod] = useState<string>('CASH');
  const [customerName, setCustomerName] = useState(
    user ? `${user.firstName} ${user.lastName}` : ''
  );
  const [customerPhone, setCustomerPhone] = useState(user?.phone || '');
  const [customerEmail, setCustomerEmail] = useState(user?.email || '');
  const [street, setStreet] = useState(
    user?.addresses.find((a) => a.isDefault)?.street || ''
  );
  const [city, setCity] = useState(
    user?.addresses.find((a) => a.isDefault)?.city || ''
  );
  const [postalCode, setPostalCode] = useState(
    user?.addresses.find((a) => a.isDefault)?.postalCode || ''
  );
  const [orderNotes, setOrderNotes] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (items.length === 0) {
      setError('Your cart is empty');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Build order items from cart
      const orderItems = items.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        totalPrice: item.totalPrice || item.unitPrice * item.quantity,
        size: item.size || undefined,
        notes: item.notes || undefined,
      }));

      const orderData: CreateOrderRequest = {
        customerName,
        customerEmail: customerEmail || undefined,
        customerPhone: customerPhone || undefined,
        orderType,
        paymentMethod,
        notes: orderNotes || undefined,
        items: orderItems,
        totalPrice,
      };

      if (orderType === 'DELIVERY') {
        orderData.deliveryAddress = {
          street,
          city,
          postalCode,
        };
      }

      const order = await ordersApi.createOrder(orderData);
      clearCart();
      navigate(`/order/${order.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create order');
    } finally {
      setLoading(false);
    }
  };

  if (items.length === 0) {
    return (
      <div>
        <h1>Checkout</h1>
        <p>Your cart is empty. Please add items before checking out.</p>
        <button onClick={() => navigate('/')}>Browse Menu</button>
      </div>
    );
  }

  return (
    <div>
      <h1>Checkout</h1>

      {error && <p>Error: {error}</p>}

      {/* Order summary */}
      <section>
        <h2>Order Summary</h2>
        <ul>
          {items.map((item) => (
            <li key={item.itemId}>
              {item.quantity}x {item.productName}
              {item.size && ` (${item.size})`}
              {item.notes && <em> - "{item.notes}"</em>}
              {' - '}CHF {(item.totalPrice || item.unitPrice * item.quantity).toFixed(2)}
            </li>
          ))}
        </ul>
        <p>
          <strong>Total: CHF {totalPrice.toFixed(2)}</strong>
        </p>
      </section>

      <form onSubmit={handleSubmit}>
        {/* Order type */}
        <fieldset>
          <legend>Order Type</legend>
          <label>
            <input
              type="radio"
              name="orderType"
              value="PICKUP"
              checked={orderType === 'PICKUP'}
              onChange={() => setOrderType('PICKUP')}
            />
            Pickup
          </label>
          <label>
            <input
              type="radio"
              name="orderType"
              value="DELIVERY"
              checked={orderType === 'DELIVERY'}
              onChange={() => setOrderType('DELIVERY')}
            />
            Delivery
          </label>
        </fieldset>

        {/* Customer info */}
        <fieldset>
          <legend>Your Information</legend>
          <label>
            Name *
            <input
              type="text"
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              required
            />
          </label>
          <label>
            Phone
            <input
              type="tel"
              value={customerPhone}
              onChange={(e) => setCustomerPhone(e.target.value)}
            />
          </label>
          <label>
            Email
            <input
              type="email"
              value={customerEmail}
              onChange={(e) => setCustomerEmail(e.target.value)}
            />
          </label>
        </fieldset>

        {/* Delivery address */}
        {orderType === 'DELIVERY' && (
          <fieldset>
            <legend>Delivery Address</legend>
            {addresses.length > 0 && (
              <div>
                <p>Use a saved address:</p>
                {addresses.map((addr) => (
                  <button
                    type="button"
                    key={addr.id}
                    onClick={() => {
                      setOrderType('DELIVERY');
                      setStreet(addr.street);
                      setCity(addr.city);
                      setPostalCode(addr.postalCode);
                    }}
                  >
                    {addr.street}, {addr.postalCode} {addr.city}
                    {addr.isDefault ? ' (Default)' : ''}
                  </button>
                ))}
              </div>
            )}
            <label>
              Street *
              <input
                type="text"
                value={street}
                onChange={(e) => setStreet(e.target.value)}
                required
              />
            </label>
            <label>
              City *
              <input
                type="text"
                value={city}
                onChange={(e) => setCity(e.target.value)}
                required
              />
            </label>
            <label>
              Postal Code *
              <input
                type="text"
                value={postalCode}
                onChange={(e) => setPostalCode(e.target.value)}
                required
              />
            </label>
          </fieldset>
        )}

        {/* Payment method */}
        <fieldset>
          <legend>Payment Method</legend>
          <label>
            <input
              type="radio"
              name="payment"
              value="CASH"
              checked={paymentMethod === 'CASH'}
              onChange={() => setPaymentMethod('CASH')}
            />
            Cash
          </label>
          <label>
            <input
              type="radio"
              name="payment"
              value="CARD"
              checked={paymentMethod === 'CARD'}
              onChange={() => setPaymentMethod('CARD')}
            />
            Card
          </label>
          <label>
            <input
              type="radio"
              name="payment"
              value="TWINT"
              checked={paymentMethod === 'TWINT'}
              onChange={() => setPaymentMethod('TWINT')}
            />
            TWINT
          </label>
        </fieldset>

        {/* Order notes */}
        <fieldset>
          <legend>Order Notes</legend>
          <textarea
            value={orderNotes}
            onChange={(e) => setOrderNotes(e.target.value)}
            placeholder="Any special requests for your order?"
          />
        </fieldset>

        {!isAuthenticated && (
          <p>
            Have an account? <a href="/login">Log in</a> for faster checkout.
          </p>
        )}

        <button type="submit" disabled={loading}>
          {loading ? 'Placing Order...' : `Place Order - CHF ${totalPrice.toFixed(2)}`}
        </button>
      </form>
    </div>
  );
}
