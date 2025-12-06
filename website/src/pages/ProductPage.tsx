import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productsApi } from '../api/products';
import { useCartStore } from '../stores/cartStore';
import type { Product } from '../types';

/**
 * ProductPage - Single product detail with add-to-cart form
 * 
 * Data: product (from URL param :id)
 * Actions: Add to cart with quantity, size, and notes
 */
export default function ProductPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addItem, initSession, loading: cartLoading } = useCartStore();

  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Form state
  const [quantity, setQuantity] = useState(1);
  const [selectedSize, setSelectedSize] = useState<string | undefined>();
  const [notes, setNotes] = useState('');

  useEffect(() => {
    initSession();
  }, [initSession]);

  useEffect(() => {
    const fetchProduct = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const data = await productsApi.getProduct(parseInt(id, 10));
        setProduct(data);
        // Set default size if product has sizes
        if (data.sizes) {
          setSelectedSize('M');
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load product');
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!product) return;
    try {
      await addItem({
        productId: product.id,
        quantity,
        size: selectedSize,
        notes: notes || undefined,
      });
      navigate('/cart');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add to cart');
    }
  };

  // Calculate current price based on size selection
  const getCurrentPrice = (): number => {
    if (!product) return 0;
    if (product.sizes && selectedSize) {
      return product.sizes[selectedSize as keyof typeof product.sizes] || product.price;
    }
    return product.price;
  };

  const totalPrice = getCurrentPrice() * quantity;

  if (loading) {
    return <div>Loading product...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  if (!product) {
    return <div>Product not found</div>;
  }

  return (
    <div>
      <button onClick={() => navigate(-1)}>← Back</button>

      <h1>{product.name}</h1>
      {product.description && <p>{product.description}</p>}
      {product.imageUrl && <img src={product.imageUrl} alt={product.name} />}

      <p>Base price: CHF {product.price.toFixed(2)}</p>

      {product.allergens && product.allergens.length > 0 && (
        <p>Allergens: {product.allergens.join(', ')}</p>
      )}

      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleAddToCart();
        }}
      >
        {/* Size selection */}
        {product.sizes && (
          <fieldset>
            <legend>Size</legend>
            {Object.entries(product.sizes).map(([size, price]) => (
              <label key={size}>
                <input
                  type="radio"
                  name="size"
                  value={size}
                  checked={selectedSize === size}
                  onChange={() => setSelectedSize(size)}
                />
                {size} - CHF {price?.toFixed(2)}
              </label>
            ))}
          </fieldset>
        )}

        {/* Quantity */}
        <fieldset>
          <legend>Quantity</legend>
          <button
            type="button"
            onClick={() => setQuantity((q) => Math.max(1, q - 1))}
          >
            -
          </button>
          <span>{quantity}</span>
          <button
            type="button"
            onClick={() => setQuantity((q) => Math.min(99, q + 1))}
          >
            +
          </button>
        </fieldset>

        {/* Notes */}
        <fieldset>
          <legend>Special requests</legend>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Any special requests? (e.g., no onions, extra crispy)"
            rows={3}
            maxLength={500}
          />
        </fieldset>

        {/* Total and submit */}
        <p>Total: CHF {totalPrice.toFixed(2)}</p>
        <button type="submit" disabled={cartLoading}>
          {cartLoading ? 'Adding...' : 'Add to Cart'}
        </button>
      </form>
    </div>
  );
}
