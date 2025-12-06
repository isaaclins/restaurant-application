import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ordersApi } from '../api/orders';
import type { Order } from '../types';

/**
 * OrderConfirmPage - Order confirmation and status tracking
 * 
 * Data: order (from URL param :id)
 * Actions: Poll for status updates
 */
export default function OrderConfirmPage() {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrder = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const data = await ordersApi.getOrder(parseInt(id, 10));
        setOrder(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load order');
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();

    // Poll for updates every 30 seconds
    const interval = setInterval(fetchOrder, 30000);
    return () => clearInterval(interval);
  }, [id]);

  const getStatusLabel = (status: string): string => {
    const labels: Record<string, string> = {
      PENDING: 'Order Received',
      CONFIRMED: 'Confirmed',
      IN_PROGRESS: 'Being Prepared',
      READY: 'Ready',
      DELIVERED: 'Delivered',
      PICKED_UP: 'Picked Up',
      CANCELLED: 'Cancelled',
    };
    return labels[status] || status;
  };

  if (loading && !order) {
    return <div>Loading order...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  if (!order) {
    return <div>Order not found</div>;
  }

  return (
    <div>
      <h1>Order Confirmed!</h1>

      <section>
        <h2>Order #{order.orderNumber}</h2>
        <p>
          Status: <strong>{getStatusLabel(order.status)}</strong>
        </p>
        {order.estimatedDelivery && (
          <p>
            Estimated {order.orderType === 'DELIVERY' ? 'delivery' : 'ready'} time:{' '}
            {new Date(order.estimatedDelivery).toLocaleTimeString()}
          </p>
        )}
      </section>

      <section>
        <h3>Order Details</h3>
        <p>Type: {order.orderType}</p>
        {order.paymentMethod && <p>Payment: {order.paymentMethod}</p>}
      </section>

      <section>
        <h3>Customer</h3>
        <p>Name: {order.customerName}</p>
        {order.customerPhone && <p>Phone: {order.customerPhone}</p>}
        {order.customerEmail && <p>Email: {order.customerEmail}</p>}
      </section>

      {order.orderType === 'DELIVERY' && order.deliveryStreet && (
        <section>
          <h3>Delivery Address</h3>
          <p>{order.deliveryStreet}</p>
          <p>
            {order.deliveryPostalCode} {order.deliveryCity}
          </p>
        </section>
      )}

      <section>
        <h3>Items</h3>
        <ul>
          {order.items && order.items.map((item, index) => (
            <li key={index}>
              {item.quantity}x {item.productName}
              {item.notes && <em> - "{item.notes}"</em>}
              {' - '}CHF {item.totalPrice.toFixed(2)}
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h3>Total</h3>
        <p>
          <strong>CHF {order.totalPrice.toFixed(2)}</strong>
        </p>
      </section>

      {order.notes && (
        <section>
          <h3>Notes</h3>
          <p>{order.notes}</p>
        </section>
      )}

      <p>Order placed: {new Date(order.createdAt).toLocaleString()}</p>

      <Link to="/">Order Again</Link>
    </div>
  );
}
