import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { ordersApi } from '../api/orders';
import { authApi } from '../api/auth';
import type { Order } from '../types';

/**
 * ProfilePage - Customer profile, addresses, and order history
 * 
 * Data: user, orders[]
 * Actions: Update profile, add address, logout
 */
export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout, updateProfile, fetchProfile, loading } =
    useAuthStore();

  const [orders, setOrders] = useState<Order[]>([]);
  const [ordersLoading, setOrdersLoading] = useState(true);

  // Edit mode state
  const [editing, setEditing] = useState(false);
  const [firstName, setFirstName] = useState(user?.firstName || '');
  const [lastName, setLastName] = useState(user?.lastName || '');
  const [phone, setPhone] = useState(user?.phone || '');

  // Add address state
  const [addingAddress, setAddingAddress] = useState(false);
  const [newStreet, setNewStreet] = useState('');
  const [newCity, setNewCity] = useState('');
  const [newPostalCode, setNewPostalCode] = useState('');

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    fetchProfile();
  }, [isAuthenticated, navigate, fetchProfile]);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const data = await ordersApi.getMyOrders();
        setOrders(data);
      } catch {
        // Ignore errors
      } finally {
        setOrdersLoading(false);
      }
    };
    if (isAuthenticated) {
      fetchOrders();
    }
  }, [isAuthenticated]);

  // Update form when user changes
  useEffect(() => {
    if (user) {
      setFirstName(user.firstName);
      setLastName(user.lastName);
      setPhone(user.phone || '');
    }
  }, [user]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateProfile({ firstName, lastName, phone });
      setEditing(false);
    } catch {
      // Error in store
    }
  };

  const handleAddAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await authApi.addAddress({
        street: newStreet,
        city: newCity,
        postalCode: newPostalCode,
      });
      setAddingAddress(false);
      setNewStreet('');
      setNewCity('');
      setNewPostalCode('');
      await fetchProfile();
    } catch {
      // Handle error
    }
  };

  if (!isAuthenticated || !user) {
    return <div>Loading...</div>;
  }

  // Fallback to empty addresses array if backend omits it
  const addresses = user.addresses || [];

  return (
    <div>
      <h1>My Profile</h1>

      {/* Profile info */}
      <section>
        <h2>Account Information</h2>
        {editing ? (
          <form onSubmit={handleUpdateProfile}>
            <label>
              First Name
              <input
                type="text"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
              />
            </label>
            <label>
              Last Name
              <input
                type="text"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </label>
            <label>
              Phone
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
            </label>
            <button type="submit" disabled={loading}>
              Save
            </button>
            <button type="button" onClick={() => setEditing(false)}>
              Cancel
            </button>
          </form>
        ) : (
          <dl>
            <dt>Name:</dt>
            <dd>
              {user.firstName} {user.lastName}
            </dd>
            <dt>Email:</dt>
            <dd>{user.email}</dd>
            <dt>Phone:</dt>
            <dd>{user.phone || 'Not set'}</dd>
          </dl>
        )}
        {!editing && <button onClick={() => setEditing(true)}>Edit Profile</button>}
      </section>

      {/* Addresses */}
      <section>
        <h2>My Addresses</h2>
        {addresses.length === 0 ? (
          <p>No saved addresses.</p>
        ) : (
          <ul>
            {addresses.map((addr) => (
              <li key={addr.id}>
                <p>{addr.street}</p>
                <p>
                  {addr.postalCode} {addr.city}
                </p>
                {addr.isDefault && <span>(Default)</span>}
              </li>
            ))}
          </ul>
        )}

        {addingAddress ? (
          <form onSubmit={handleAddAddress}>
            <h3>Add New Address</h3>
            <label>
              Street
              <input
                type="text"
                value={newStreet}
                onChange={(e) => setNewStreet(e.target.value)}
                required
              />
            </label>
            <label>
              City
              <input
                type="text"
                value={newCity}
                onChange={(e) => setNewCity(e.target.value)}
                required
              />
            </label>
            <label>
              Postal Code
              <input
                type="text"
                value={newPostalCode}
                onChange={(e) => setNewPostalCode(e.target.value)}
                required
              />
            </label>
            <button type="submit">Add Address</button>
            <button type="button" onClick={() => setAddingAddress(false)}>
              Cancel
            </button>
          </form>
        ) : (
          <button onClick={() => setAddingAddress(true)}>Add Address</button>
        )}
      </section>

      {/* Order history */}
      <section>
        <h2>Order History</h2>
        {ordersLoading ? (
          <p>Loading orders...</p>
        ) : orders.length === 0 ? (
          <p>No orders yet.</p>
        ) : (
          <ul>
            {orders.map((order) => (
              <li key={order.id}>
                <Link to={`/order/${order.id}`}>
                  <h3>Order #{order.orderNumber}</h3>
                  <p>Status: {order.status}</p>
                  <p>Total: CHF {order.totalPrice.toFixed(2)}</p>
                  <p>{new Date(order.createdAt).toLocaleDateString()}</p>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      <hr />

      <button onClick={handleLogout}>Logout</button>
    </div>
  );
}
