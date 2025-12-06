import { useEffect, useState } from 'react';
import { settingsApi } from '../api/settings';
import type { RestaurantSettings } from '../types/settings';

const DAY_LABELS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

/**
 * AboutPage - Displays restaurant info, opening hours, and delivery areas from settings-service.
 * Minimal/unstyled template for AI restyling.
 */
export default function AboutPage() {
  const [settings, setSettings] = useState<RestaurantSettings | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await settingsApi.getSettings();
        setSettings(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load settings');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <div>Loading settings...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!settings) return <div>No settings found.</div>;

  const {
    restaurantName,
    slogan,
    description,
    address,
    city,
    postalCode,
    country,
    phone,
    email,
    website,
    openingHours = [],
    deliveryAreas = [],
  } = settings;

  return (
    <div>
      <h1>About Us</h1>

      <section>
        <h2>{restaurantName || 'Our Restaurant'}</h2>
        {slogan && <p>{slogan}</p>}
        {description && <p>{description}</p>}
        <p>
          {address && <span>{address}</span>}
          {city && <span>, {city}</span>}
          {postalCode && <span> {postalCode}</span>}
          {country && <span>, {country}</span>}
        </p>
        <p>{phone}</p>
        <p>{email}</p>
        {website && (
          <p>
            <a href={website} target="_blank" rel="noreferrer">
              {website}
            </a>
          </p>
        )}
      </section>

      <section>
        <h2>Opening Hours</h2>
        {openingHours.length === 0 ? (
          <p>No opening hours configured.</p>
        ) : (
          <ul>
            {openingHours.map((oh, idx) => (
              <li key={idx}>
                <strong>{oh.dayName || oh.day || DAY_LABELS[idx] || `Day ${idx + 1}`}:</strong>{' '}
                {oh.isClosed
                  ? 'Closed'
                  : `${oh.openTime || '--'} - ${oh.closeTime || '--'}`}
              </li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Delivery Areas</h2>
        {deliveryAreas.length === 0 ? (
          <p>No delivery areas configured.</p>
        ) : (
          <ul>
            {deliveryAreas.map((area) => (
              <li key={area.id}>
                {area.postalCode} {area.city} — Fee: {area.deliveryFee ?? 0} / Min:{' '}
                {area.minimumOrder ?? 0}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
