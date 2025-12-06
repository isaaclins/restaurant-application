import { Link, Outlet } from 'react-router-dom';
import { useCartStore } from '../stores/cartStore';
import { useAuthStore } from '../stores/authStore';

/**
 * Layout - Navigation wrapper for all pages
 * 
 * Data: cart item count, auth status
 * Children: Page content via <Outlet />
 */
export default function Layout() {
  const totalItems = useCartStore((state) => state.totalItems);
  const { isAuthenticated, user } = useAuthStore();

  return (
    <div>
      <header>
        <nav>
          <Link to="/">Menu</Link>
          <Link to="/cart">Cart ({totalItems})</Link>
          {isAuthenticated ? (
            <Link to="/profile">
              {user?.firstName || 'Profile'}
            </Link>
          ) : (
            <Link to="/login">Login</Link>
          )}
        </nav>
      </header>

      <main>
        <Outlet />
      </main>

      <footer>
        <p>Restaurant Website</p>
      </footer>
    </div>
  );
}
