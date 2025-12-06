import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCartStore } from '../stores/cartStore';

/**
 * CartPage - Display cart items with ability to remove/update quantity
 * 
 * Data: cart items, totals
 * Actions: Remove item, update quantity, proceed to checkout
 */
export default function CartPage() {
  const navigate = useNavigate();
  const {
    items,
    totalItems,
    totalPrice,
    loading,
    error,
    initSession,
    fetchCart,
    removeItem,
    updateQuantity,
  } = useCartStore();

  useEffect(() => {
    initSession();
  }, [initSession]);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const handleRemove = async (itemId: string) => {
    try {
      await removeItem(itemId);
    } catch {
      // Error is in store
    }
  };

  const handleQuantityChange = async (itemId: string, newQuantity: number) => {
    try {
      await updateQuantity(itemId, newQuantity);
    } catch {
      // Error is in store
    }
  };

  if (loading && items.length === 0) {
    return <div>Loading cart...</div>;
  }

  return (
    <div>
      <h1>Your Cart</h1>

      {error && <p>Error: {error}</p>}

      {items.length === 0 ? (
        <div>
          <p>Your cart is empty.</p>
          <Link to="/">Browse Menu</Link>
        </div>
      ) : (
        <>
          <p>{totalItems} item(s) in cart</p>

          <ul>
            {items.map((item) => (
              <li key={item.itemId}>
                <h3>{item.productName}</h3>
                {item.size && <p>Size: {item.size}</p>}
                {item.notes && <p>Notes: {item.notes}</p>}
                <p>Unit price: CHF {item.unitPrice.toFixed(2)}</p>

                <div>
                  <button
                    onClick={() => handleQuantityChange(item.itemId, item.quantity - 1)}
                    disabled={loading}
                  >
                    -
                  </button>
                  <span>{item.quantity}</span>
                  <button
                    onClick={() => handleQuantityChange(item.itemId, item.quantity + 1)}
                    disabled={loading}
                  >
                    +
                  </button>
                </div>

                <p>Item total: CHF {(item.totalPrice || item.unitPrice * item.quantity).toFixed(2)}</p>

                <button onClick={() => handleRemove(item.itemId)} disabled={loading}>
                  Remove
                </button>
              </li>
            ))}
          </ul>

          <hr />

          <dl>
            <dt>Total:</dt>
            <dd>CHF {totalPrice.toFixed(2)}</dd>
          </dl>

          <button onClick={() => navigate('/checkout')}>Proceed to Checkout</button>
          <Link to="/">Continue Shopping</Link>
        </>
      )}
    </div>
  );
}
