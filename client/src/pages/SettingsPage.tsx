import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { settingsApi } from '../api/settings';
import { OpeningHours, DeliveryArea } from '../types';
import {
  Save,
  Plus,
  Trash2,
  Clock,
  MapPin,
  RefreshCw,
  Store,
  Check,
} from 'lucide-react';

function SettingsPage() {
  const [activeTab, setActiveTab] = useState<'general' | 'hours' | 'delivery'>('general');

  const { data: settings, isLoading } = useQuery({
    queryKey: ['settings'],
    queryFn: settingsApi.getSettings,
  });

  if (isLoading || !settings) {
    return (
      <div className="flex items-center justify-center h-full">
        <RefreshCw className="w-8 h-8 text-orange-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="bg-white shadow px-6 py-4">
        <h1 className="text-2xl font-bold text-gray-800">Settings</h1>
        <p className="text-sm text-gray-500">Manage restaurant configuration</p>
      </div>

      {/* Tabs */}
      <div className="bg-white border-b px-6">
        <div className="flex space-x-8">
          <TabButton
            active={activeTab === 'general'}
            onClick={() => setActiveTab('general')}
            icon={Store}
            label="General"
          />
          <TabButton
            active={activeTab === 'hours'}
            onClick={() => setActiveTab('hours')}
            icon={Clock}
            label="Opening Hours"
          />
          <TabButton
            active={activeTab === 'delivery'}
            onClick={() => setActiveTab('delivery')}
            icon={MapPin}
            label="Delivery Areas"
          />
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 p-6 overflow-auto">
        {activeTab === 'general' && <GeneralSettings settings={settings} />}
        {activeTab === 'hours' && <OpeningHoursSettings hours={settings.openingHours} />}
        {activeTab === 'delivery' && <DeliveryAreaSettings />}
      </div>
    </div>
  );
}

function TabButton({
  active,
  onClick,
  icon: Icon,
  label,
}: {
  active: boolean;
  onClick: () => void;
  icon: React.ElementType;
  label: string;
}) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center py-4 border-b-2 -mb-px transition ${
        active
          ? 'border-orange-500 text-orange-500'
          : 'border-transparent text-gray-500 hover:text-gray-700'
      }`}
    >
      <Icon className="w-5 h-5 mr-2" />
      {label}
    </button>
  );
}

function GeneralSettings({ settings }: { settings: NonNullable<ReturnType<typeof settingsApi.getSettings> extends Promise<infer T> ? T : never> }) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState({
    restaurantName: settings.restaurantName,
    address: settings.address,
    phone: settings.phone || '',
    email: settings.email || '',
    minimumOrderValue: settings.minimumOrderValue.toString(),
    deliveryFee: settings.deliveryFee.toString(),
    deliveryEnabled: settings.deliveryEnabled,
    pickupEnabled: settings.pickupEnabled,
  });

  const updateMutation = useMutation({
    mutationFn: () => settingsApi.updateSettings({
      restaurantName: formData.restaurantName,
      address: formData.address,
      phone: formData.phone || undefined,
      email: formData.email || undefined,
      minimumOrderValue: parseFloat(formData.minimumOrderValue),
      deliveryFee: parseFloat(formData.deliveryFee),
      deliveryEnabled: formData.deliveryEnabled,
      pickupEnabled: formData.pickupEnabled,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['settings'] });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate();
  };

  return (
    <div className="max-w-2xl">
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white rounded-lg shadow p-6 space-y-4">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Restaurant Information</h3>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Restaurant Name</label>
            <input
              type="text"
              value={formData.restaurantName}
              onChange={(e) => setFormData({ ...formData, restaurantName: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
            <input
              type="text"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input
                type="tel"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6 space-y-4">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Order Settings</h3>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Minimum Order (CHF)</label>
              <input
                type="number"
                step="0.50"
                value={formData.minimumOrderValue}
                onChange={(e) => setFormData({ ...formData, minimumOrderValue: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Delivery Fee (CHF)</label>
              <input
                type="number"
                step="0.50"
                value={formData.deliveryFee}
                onChange={(e) => setFormData({ ...formData, deliveryFee: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
          </div>

          <div className="flex space-x-6">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.deliveryEnabled}
                onChange={(e) => setFormData({ ...formData, deliveryEnabled: e.target.checked })}
                className="w-4 h-4 text-orange-500 border-gray-300 rounded focus:ring-orange-500"
              />
              <span className="ml-2 text-sm text-gray-700">Delivery Enabled</span>
            </label>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.pickupEnabled}
                onChange={(e) => setFormData({ ...formData, pickupEnabled: e.target.checked })}
                className="w-4 h-4 text-orange-500 border-gray-300 rounded focus:ring-orange-500"
              />
              <span className="ml-2 text-sm text-gray-700">Pickup Enabled</span>
            </label>
          </div>
        </div>

        <button
          type="submit"
          disabled={updateMutation.isPending}
          className="flex items-center px-6 py-2 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600 disabled:opacity-50"
        >
          {updateMutation.isPending ? (
            <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
          ) : updateMutation.isSuccess ? (
            <Check className="w-4 h-4 mr-2" />
          ) : (
            <Save className="w-4 h-4 mr-2" />
          )}
          Save Changes
        </button>
      </form>
    </div>
  );
}

function OpeningHoursSettings({ hours }: { hours: OpeningHours[] }) {
  const queryClient = useQueryClient();

  const updateMutation = useMutation({
    mutationFn: (data: Partial<OpeningHours>) => settingsApi.updateOpeningHours(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['settings'] });
    },
  });

  return (
    <div className="max-w-3xl">
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Day</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Open</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Close</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {hours.map((hour) => (
              <HoursRow key={hour.id} hour={hour} onUpdate={updateMutation.mutate} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function HoursRow({
  hour,
  onUpdate,
}: {
  hour: OpeningHours;
  onUpdate: (data: Partial<OpeningHours>) => void;
}) {
  const [openTime, setOpenTime] = useState(hour.openTime);
  const [closeTime, setCloseTime] = useState(hour.closeTime);
  const [isClosed, setIsClosed] = useState(hour.isClosed);

  const handleSave = () => {
    onUpdate({
      dayOfWeek: hour.dayOfWeek,
      openTime,
      closeTime,
      isClosed,
    });
  };

  return (
    <tr>
      <td className="px-4 py-3 font-medium text-gray-800">{hour.dayName}</td>
      <td className="px-4 py-3">
        <input
          type="time"
          value={openTime}
          onChange={(e) => setOpenTime(e.target.value)}
          disabled={isClosed}
          className="px-2 py-1 border border-gray-300 rounded focus:ring-2 focus:ring-orange-500 outline-none disabled:bg-gray-100"
        />
      </td>
      <td className="px-4 py-3">
        <input
          type="time"
          value={closeTime}
          onChange={(e) => setCloseTime(e.target.value)}
          disabled={isClosed}
          className="px-2 py-1 border border-gray-300 rounded focus:ring-2 focus:ring-orange-500 outline-none disabled:bg-gray-100"
        />
      </td>
      <td className="px-4 py-3">
        <div className="flex items-center space-x-3">
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={isClosed}
              onChange={(e) => setIsClosed(e.target.checked)}
              className="w-4 h-4 text-orange-500 border-gray-300 rounded focus:ring-orange-500"
            />
            <span className="ml-2 text-sm text-gray-600">Closed</span>
          </label>
          <button
            onClick={handleSave}
            className="p-1 text-orange-500 hover:bg-orange-50 rounded"
          >
            <Save className="w-4 h-4" />
          </button>
        </div>
      </td>
    </tr>
  );
}

function DeliveryAreaSettings() {
  const queryClient = useQueryClient();
  const [showAddForm, setShowAddForm] = useState(false);
  const [newArea, setNewArea] = useState({
    postalCode: '',
    cityName: '',
    deliveryFee: '',
    minimumOrderValue: '',
  });

  const { data: areas = [] } = useQuery({
    queryKey: ['deliveryAreas'],
    queryFn: settingsApi.getDeliveryAreas,
  });

  const addMutation = useMutation({
    mutationFn: () => settingsApi.addDeliveryArea({
      postalCode: newArea.postalCode,
      cityName: newArea.cityName,
      deliveryFee: parseFloat(newArea.deliveryFee),
      minimumOrderValue: parseFloat(newArea.minimumOrderValue),
      isActive: true,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deliveryAreas'] });
      setShowAddForm(false);
      setNewArea({ postalCode: '', cityName: '', deliveryFee: '', minimumOrderValue: '' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => settingsApi.deleteDeliveryArea(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deliveryAreas'] });
    },
  });

  return (
    <div className="max-w-3xl">
      <div className="flex justify-end mb-4">
        <button
          onClick={() => setShowAddForm(true)}
          className="flex items-center px-4 py-2 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600"
        >
          <Plus className="w-4 h-4 mr-2" /> Add Area
        </button>
      </div>

      {showAddForm && (
        <div className="bg-white rounded-lg shadow p-4 mb-4">
          <div className="grid grid-cols-4 gap-4">
            <input
              type="text"
              placeholder="Postal Code"
              value={newArea.postalCode}
              onChange={(e) => setNewArea({ ...newArea, postalCode: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
            />
            <input
              type="text"
              placeholder="City"
              value={newArea.cityName}
              onChange={(e) => setNewArea({ ...newArea, cityName: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
            />
            <input
              type="number"
              placeholder="Delivery Fee"
              value={newArea.deliveryFee}
              onChange={(e) => setNewArea({ ...newArea, deliveryFee: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
            />
            <input
              type="number"
              placeholder="Min. Order"
              value={newArea.minimumOrderValue}
              onChange={(e) => setNewArea({ ...newArea, minimumOrderValue: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
            />
          </div>
          <div className="flex justify-end mt-4 space-x-2">
            <button
              onClick={() => setShowAddForm(false)}
              className="px-4 py-2 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              onClick={() => addMutation.mutate()}
              className="px-4 py-2 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600"
            >
              Add
            </button>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Postal Code</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">City</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Delivery Fee</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Min. Order</th>
              <th className="px-4 py-3 text-right text-sm font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {areas.map((area: DeliveryArea) => (
              <tr key={area.id}>
                <td className="px-4 py-3 font-medium text-gray-800">{area.postalCode}</td>
                <td className="px-4 py-3 text-gray-600">{area.cityName}</td>
                <td className="px-4 py-3 text-gray-600">CHF {area.deliveryFee.toFixed(2)}</td>
                <td className="px-4 py-3 text-gray-600">CHF {area.minimumOrderValue.toFixed(2)}</td>
                <td className="px-4 py-3 text-right">
                  <button
                    onClick={() => deleteMutation.mutate(area.id)}
                    className="p-2 text-gray-500 hover:text-red-500 hover:bg-red-50 rounded transition"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {areas.length === 0 && (
          <div className="text-center py-12 text-gray-500">
            No delivery areas configured
          </div>
        )}
      </div>
    </div>
  );
}

export default SettingsPage;
