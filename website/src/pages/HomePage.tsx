import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productsApi } from '../api/products';
import type { Product, Category } from '../types';

/**
 * HomePage - Displays all products grouped by category
 * 
 * Data: products[], categories[]
 * Actions: Navigate to product detail
 */
export default function HomePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [productsData, categoriesData] = await Promise.all([
          productsApi.getProducts(true), // Only available products
          productsApi.getCategories(),
        ]);
        setProducts(productsData);
        setCategories(categoriesData);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load products');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // Helper to get product category (handles both API formats)
  const getProductCategory = (p: Product): string => {
    return p.category || p.categoryName || 'Other';
  };

  // Filter products by selected category
  const filteredProducts = selectedCategory
    ? products.filter((p) => getProductCategory(p) === selectedCategory)
    : products;

  // Group products by category for display
  const productsByCategory = filteredProducts.reduce<Record<string, Product[]>>(
    (acc, product) => {
      const cat = getProductCategory(product);
      if (!acc[cat]) acc[cat] = [];
      acc[cat].push(product);
      return acc;
    },
    {}
  );

  if (loading) {
    return <div>Loading products...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div>
      <h1>Menu</h1>

      {/* Category filter */}
      <nav>
        <button
          onClick={() => setSelectedCategory(null)}
          disabled={selectedCategory === null}
        >
          All
        </button>
        {categories.map((cat) => (
          <button
            key={cat.id}
            onClick={() => setSelectedCategory(cat.name)}
            disabled={selectedCategory === cat.name}
          >
            {cat.name}
          </button>
        ))}
      </nav>

      {/* Products grouped by category */}
      {Object.entries(productsByCategory).map(([category, categoryProducts]) => (
        <section key={category}>
          <h2>{category}</h2>
          <ul>
            {categoryProducts.map((product) => (
              <li key={product.id}>
                <Link to={`/product/${product.id}`}>
                  <h3>{product.name}</h3>
                  {product.description && <p>{product.description}</p>}
                  <p>CHF {product.price.toFixed(2)}</p>
                  {product.sizes && (
                    <p>
                      Sizes: S (CHF {product.sizes.S?.toFixed(2)}) / M (CHF{' '}
                      {product.sizes.M?.toFixed(2)}) / L (CHF {product.sizes.L?.toFixed(2)})
                    </p>
                  )}
                  {product.allergens && product.allergens.length > 0 && (
                    <p>Allergens: {product.allergens.join(', ')}</p>
                  )}
                </Link>
              </li>
            ))}
          </ul>
        </section>
      ))}

      {filteredProducts.length === 0 && <p>No products found.</p>}
    </div>
  );
}
