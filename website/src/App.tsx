import { useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import ProductPage from './pages/ProductPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrderConfirmPage from './pages/OrderConfirmPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import { useCartStore } from './stores/cartStore';
import { useAuthStore } from './stores/authStore';

/**
 * App - Main router configuration
 * 
 * Routes:
 * - / (HomePage) - Product listing
 * - /product/:id (ProductPage) - Single product detail
 * - /cart (CartPage) - Shopping cart
 * - /checkout (CheckoutPage) - Order form
 * - /order/:id (OrderConfirmPage) - Order confirmation/tracking
 * - /login (LoginPage) - Customer login
 * - /register (RegisterPage) - Customer registration
 * - /profile (ProfilePage) - Customer profile (protected)
 */
export default function App() {
  const initSession = useCartStore((state) => state.initSession);
  const fetchProfile = useAuthStore((state) => state.fetchProfile);

  // Initialize cart session and check auth on app load
  useEffect(() => {
    initSession();
    fetchProfile();
  }, [initSession, fetchProfile]);

  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="product/:id" element={<ProductPage />} />
        <Route path="cart" element={<CartPage />} />
        <Route path="checkout" element={<CheckoutPage />} />
        <Route path="order/:id" element={<OrderConfirmPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="profile" element={<ProfilePage />} />
      </Route>
    </Routes>
  );
}
