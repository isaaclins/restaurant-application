import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { settingsApi, ReceiptTemplate, EmailTemplate, SmtpConfig, TestEmailRequest } from '../api/settings';
import { productsApi } from '../api/products';
import { OpeningHours, DeliveryArea, Product } from '../types';
import {
  Save,
  Plus,
  Trash2,
  Clock,
  MapPin,
  RefreshCw,
  Store,
  Check,
  Code,
  Zap,
  Mail,
  FileText,
  Globe,
  RotateCcw,
  Star,
  AlertCircle,
  CheckCircle,
  Edit2,
  X,
  Eye,
  Send,
  Settings,
  Server,
  Shield,
  TestTube,
} from 'lucide-react';
import api from '../api/client';

function SettingsPage() {
  const [activeTab, setActiveTab] = useState<'general' | 'hours' | 'delivery' | 'email' | 'developer'>('general');

  const { data: settings, isLoading, error } = useQuery({
    queryKey: ['settings'],
    queryFn: settingsApi.getSettings,
    staleTime: 60000, // Cache for 1 minute
    retry: 2,
    retryDelay: 1000,
  });

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center h-full">
        <RefreshCw className="w-8 h-8 text-orange-500 animate-spin" />
        <p className="mt-4 text-gray-500">Loading settings...</p>
      </div>
    );
  }

  if (error || !settings) {
    return (
      <div className="flex flex-col items-center justify-center h-full">
        <AlertCircle className="w-12 h-12 text-red-500 mb-4" />
        <h2 className="text-xl font-semibold text-gray-800">Failed to load settings</h2>
        <p className="text-gray-500 mt-2">Please check if the Settings Service is running.</p>
        <button
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600"
        >
          <RefreshCw className="w-4 h-4 inline mr-2" />
          Retry
        </button>
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
          <TabButton
            active={activeTab === 'email'}
            onClick={() => setActiveTab('email')}
            icon={Mail}
            label="Email & Receipts"
          />
          <TabButton
            active={activeTab === 'developer'}
            onClick={() => setActiveTab('developer')}
            icon={Code}
            label="Developer"
          />
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 p-6 overflow-auto">
        {activeTab === 'general' && <GeneralSettings settings={settings} />}
        {activeTab === 'hours' && <OpeningHoursSettings hours={settings.openingHours} />}
        {activeTab === 'delivery' && <DeliveryAreaSettings />}
        {activeTab === 'email' && <EmailReceiptSettings />}
        {activeTab === 'developer' && <DeveloperSettings />}
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

// =============================================================================
// Email & Receipt Settings
// =============================================================================

const LANGUAGE_NAMES: Record<string, { native: string; english: string }> = {
  DE: { native: 'Deutsch', english: 'German' },
  EN: { native: 'English', english: 'English' },
  FR: { native: 'Français', english: 'French' },
  IT: { native: 'Italiano', english: 'Italian' },
};

function EmailReceiptSettings() {
  const [subTab, setSubTab] = useState<'receipts' | 'emails' | 'smtp' | 'stats'>('receipts');

  return (
    <div className="max-w-4xl">
      {/* Sub-tabs */}
      <div className="flex space-x-4 mb-6">
        <button
          onClick={() => setSubTab('receipts')}
          className={`px-4 py-2 rounded-lg font-medium transition ${
            subTab === 'receipts'
              ? 'bg-orange-500 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <FileText className="w-4 h-4 inline mr-2" />
          Receipt Templates
        </button>
        <button
          onClick={() => setSubTab('emails')}
          className={`px-4 py-2 rounded-lg font-medium transition ${
            subTab === 'emails'
              ? 'bg-orange-500 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <Mail className="w-4 h-4 inline mr-2" />
          Email Templates
        </button>
        <button
          onClick={() => setSubTab('smtp')}
          className={`px-4 py-2 rounded-lg font-medium transition ${
            subTab === 'smtp'
              ? 'bg-orange-500 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <Server className="w-4 h-4 inline mr-2" />
          SMTP Settings
        </button>
        <button
          onClick={() => setSubTab('stats')}
          className={`px-4 py-2 rounded-lg font-medium transition ${
            subTab === 'stats'
              ? 'bg-orange-500 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <CheckCircle className="w-4 h-4 inline mr-2" />
          Notification Stats
        </button>
      </div>

      {subTab === 'receipts' && <ReceiptTemplateSettings />}
      {subTab === 'emails' && <EmailTemplateSettings />}
      {subTab === 'smtp' && <SmtpSettings />}
      {subTab === 'stats' && <NotificationStats />}
    </div>
  );
}

function ReceiptTemplateSettings() {
  const queryClient = useQueryClient();
  const [selectedLanguage, setSelectedLanguage] = useState<string>('DE');
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState<Partial<ReceiptTemplate>>({});

  const { data: templates = [], isLoading } = useQuery({
    queryKey: ['receiptTemplates'],
    queryFn: settingsApi.getReceiptTemplates,
  });

  const currentTemplate = templates.find(t => t.language === selectedLanguage);

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!currentTemplate) return Promise.reject('No template');
      return settingsApi.updateReceiptTemplate(currentTemplate.id, formData);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receiptTemplates'] });
      setEditMode(false);
    },
  });

  const resetMutation = useMutation({
    mutationFn: () => {
      if (!currentTemplate) return Promise.reject('No template');
      return settingsApi.resetReceiptTemplate(currentTemplate.id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receiptTemplates'] });
    },
  });

  const setDefaultMutation = useMutation({
    mutationFn: () => {
      if (!currentTemplate) return Promise.reject('No template');
      return settingsApi.setDefaultReceiptTemplate(currentTemplate.id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receiptTemplates'] });
    },
  });

  const startEdit = () => {
    if (currentTemplate) {
      setFormData({ ...currentTemplate });
      setEditMode(true);
    }
  };

  const cancelEdit = () => {
    setEditMode(false);
    setFormData({});
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-48">
        <RefreshCw className="w-6 h-6 text-orange-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Language Selector */}
      <div className="bg-white rounded-lg shadow p-4">
        <h3 className="text-sm font-medium text-gray-700 mb-3">Select Language</h3>
        <div className="flex space-x-2">
          {Object.entries(LANGUAGE_NAMES).map(([code, names]) => (
            <button
              key={code}
              onClick={() => {
                setSelectedLanguage(code);
                setEditMode(false);
              }}
              className={`px-4 py-2 rounded-lg font-medium transition flex items-center ${
                selectedLanguage === code
                  ? 'bg-orange-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              <Globe className="w-4 h-4 mr-2" />
              {names.native}
              {templates.find(t => t.language === code)?.isDefault && (
                <Star className="w-4 h-4 ml-2 fill-current" />
              )}
            </button>
          ))}
        </div>
      </div>

      {currentTemplate && (
        <div className="bg-white rounded-lg shadow">
          {/* Header */}
          <div className="p-4 border-b flex justify-between items-center">
            <div>
              <h3 className="font-semibold text-gray-800">
                {LANGUAGE_NAMES[selectedLanguage]?.english} Receipt Template
              </h3>
              <p className="text-sm text-gray-500">
                {currentTemplate.isDefault && (
                  <span className="inline-flex items-center text-orange-600">
                    <Star className="w-3 h-3 mr-1 fill-current" /> Default Template
                  </span>
                )}
              </p>
            </div>
            <div className="flex space-x-2">
              {!editMode ? (
                <>
                  <button
                    onClick={startEdit}
                    className="px-3 py-1.5 bg-orange-500 text-white rounded-lg text-sm font-medium hover:bg-orange-600 flex items-center"
                  >
                    <Edit2 className="w-4 h-4 mr-1" /> Edit
                  </button>
                  {!currentTemplate.isDefault && (
                    <button
                      onClick={() => setDefaultMutation.mutate()}
                      disabled={setDefaultMutation.isPending}
                      className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50 flex items-center"
                    >
                      <Star className="w-4 h-4 mr-1" /> Set Default
                    </button>
                  )}
                  <button
                    onClick={() => resetMutation.mutate()}
                    disabled={resetMutation.isPending}
                    className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50 flex items-center text-gray-600"
                  >
                    <RotateCcw className="w-4 h-4 mr-1" /> Reset
                  </button>
                </>
              ) : (
                <>
                  <button
                    onClick={() => updateMutation.mutate()}
                    disabled={updateMutation.isPending}
                    className="px-3 py-1.5 bg-green-500 text-white rounded-lg text-sm font-medium hover:bg-green-600 flex items-center"
                  >
                    <Save className="w-4 h-4 mr-1" /> Save
                  </button>
                  <button
                    onClick={cancelEdit}
                    className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50 flex items-center"
                  >
                    <X className="w-4 h-4 mr-1" /> Cancel
                  </button>
                </>
              )}
            </div>
          </div>

          {/* Template Fields */}
          <div className="p-4 space-y-4">
            {/* Display Options */}
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Currency Symbol</label>
                {editMode ? (
                  <input
                    type="text"
                    value={formData.currency || ''}
                    onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
                  />
                ) : (
                  <div className="px-3 py-2 bg-gray-50 rounded-lg text-gray-800">{currentTemplate.currency}</div>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Currency Position</label>
                {editMode ? (
                  <select
                    value={formData.currencyPosition || 'BEFORE'}
                    onChange={(e) => setFormData({ ...formData, currencyPosition: e.target.value as 'BEFORE' | 'AFTER' })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
                  >
                    <option value="BEFORE">Before (CHF 10.00)</option>
                    <option value="AFTER">After (10.00 CHF)</option>
                  </select>
                ) : (
                  <div className="px-3 py-2 bg-gray-50 rounded-lg text-gray-800">
                    {currentTemplate.currencyPosition === 'BEFORE' ? 'Before Amount' : 'After Amount'}
                  </div>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Show Logo</label>
                {editMode ? (
                  <label className="flex items-center h-10">
                    <input
                      type="checkbox"
                      checked={formData.showLogo ?? true}
                      onChange={(e) => setFormData({ ...formData, showLogo: e.target.checked })}
                      className="w-4 h-4 text-orange-500 border-gray-300 rounded focus:ring-orange-500"
                    />
                    <span className="ml-2 text-gray-700">Display logo on receipt</span>
                  </label>
                ) : (
                  <div className="px-3 py-2 bg-gray-50 rounded-lg text-gray-800">
                    {currentTemplate.showLogo ? 'Yes' : 'No'}
                  </div>
                )}
              </div>
            </div>

            {/* Labels */}
            <div>
              <h4 className="font-medium text-gray-800 mb-3 flex items-center">
                <FileText className="w-4 h-4 mr-2" /> Labels & Text
              </h4>
              <div className="grid grid-cols-2 gap-4">
                {[
                  { key: 'labelReceipt', label: 'Receipt Title' },
                  { key: 'labelOrderNumber', label: 'Order Number' },
                  { key: 'labelDate', label: 'Date' },
                  { key: 'labelCustomer', label: 'Customer' },
                  { key: 'labelProduct', label: 'Product' },
                  { key: 'labelQuantity', label: 'Quantity' },
                  { key: 'labelUnitPrice', label: 'Unit Price' },
                  { key: 'labelTotal', label: 'Total' },
                  { key: 'labelSubtotal', label: 'Subtotal' },
                  { key: 'labelVat', label: 'VAT' },
                  { key: 'labelDeliveryFee', label: 'Delivery Fee' },
                  { key: 'labelDiscount', label: 'Discount' },
                  { key: 'labelPaymentMethod', label: 'Payment Method' },
                ].map(({ key, label }) => (
                  <div key={key}>
                    <label className="block text-sm font-medium text-gray-600 mb-1">{label}</label>
                    {editMode ? (
                      <input
                        type="text"
                        value={(formData as unknown as Record<string, string>)[key] || ''}
                        onChange={(e) => setFormData({ ...formData, [key]: e.target.value })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none text-sm"
                      />
                    ) : (
                      <div className="px-3 py-2 bg-gray-50 rounded-lg text-gray-800 text-sm">
                        {(currentTemplate as unknown as Record<string, string>)[key]}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>

            {/* Messages */}
            <div>
              <h4 className="font-medium text-gray-800 mb-3 flex items-center">
                <Mail className="w-4 h-4 mr-2" /> Messages
              </h4>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1">Thank You Message</label>
                  {editMode ? (
                    <textarea
                      value={formData.thankYouMessage || ''}
                      onChange={(e) => setFormData({ ...formData, thankYouMessage: e.target.value })}
                      rows={2}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
                    />
                  ) : (
                    <div className="px-3 py-2 bg-gray-50 rounded-lg text-gray-800">{currentTemplate.thankYouMessage}</div>
                  )}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1">Header Text (optional)</label>
                  {editMode ? (
                    <textarea
                      value={formData.headerText || ''}
                      onChange={(e) => setFormData({ ...formData, headerText: e.target.value })}
                      rows={2}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
                      placeholder="Text at the top of the receipt"
                    />
                  ) : (
                    <div className="px-3 py-2 bg-gray-50 rounded-lg text-gray-800">
                      {currentTemplate.headerText || <span className="text-gray-400 italic">Not set</span>}
                    </div>
                  )}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1">Footer Text (optional)</label>
                  {editMode ? (
                    <textarea
                      value={formData.footerText || ''}
                      onChange={(e) => setFormData({ ...formData, footerText: e.target.value })}
                      rows={2}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
                      placeholder="Text at the bottom of the receipt"
                    />
                  ) : (
                    <div className="px-3 py-2 bg-gray-50 rounded-lg text-gray-800">
                      {currentTemplate.footerText || <span className="text-gray-400 italic">Not set</span>}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function EmailTemplateSettings() {
  const queryClient = useQueryClient();
  const [selectedTemplate, setSelectedTemplate] = useState<EmailTemplate | null>(null);
  const [previewTemplate, setPreviewTemplate] = useState<EmailTemplate | null>(null);
  const [showTestEmailModal, setShowTestEmailModal] = useState(false);
  const [testEmailAddress, setTestEmailAddress] = useState('');
  const [testEmailTemplateId, setTestEmailTemplateId] = useState<number | null>(null);
  const [editData, setEditData] = useState<{ subject: string; htmlTemplate: string; textTemplate: string }>({
    subject: '',
    htmlTemplate: '',
    textTemplate: '',
  });

  const { data: templates = [], isLoading, error } = useQuery({
    queryKey: ['emailTemplates'],
    queryFn: async () => {
      console.log('[EmailTemplates] Fetching templates...');
      try {
        const result = await settingsApi.getEmailTemplates();
        console.log('[EmailTemplates] Success:', result.length, 'templates');
        return result;
      } catch (err) {
        console.error('[EmailTemplates] Error:', err);
        throw err;
      }
    },
    retry: 2,
    staleTime: 30000,
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedTemplate) return Promise.reject('No template');
      return settingsApi.updateEmailTemplate(selectedTemplate.id, editData);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['emailTemplates'] });
      setSelectedTemplate(null);
    },
  });

  const testEmailMutation = useMutation({
    mutationFn: (request: TestEmailRequest) => settingsApi.sendTestEmail(request),
    onSuccess: () => {
      setShowTestEmailModal(false);
      setTestEmailAddress('');
      setTestEmailTemplateId(null);
    },
  });

  const startEdit = (template: EmailTemplate) => {
    setSelectedTemplate(template);
    setEditData({
      subject: template.subject,
      htmlTemplate: template.htmlTemplate,
      textTemplate: template.textTemplate || '',
    });
  };

  const openPreview = (template: EmailTemplate) => {
    setPreviewTemplate(template);
  };

  const openTestEmailModal = (templateId: number) => {
    setTestEmailTemplateId(templateId);
    setShowTestEmailModal(true);
  };

  const sendTestEmail = () => {
    if (!testEmailTemplateId || !testEmailAddress) return;
    testEmailMutation.mutate({
      templateId: testEmailTemplateId,
      recipientEmail: testEmailAddress,
      templateVariables: {
        orderNumber: '12345',
        customerName: 'Test User',
        restaurantName: 'My Restaurant',
        totalAmount: '45.50',
        currency: 'CHF',
        orderType: 'Delivery',
        year: new Date().getFullYear().toString(),
      },
    });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-48">
        <RefreshCw className="w-6 h-6 text-orange-500 animate-spin" />
      </div>
    );
  }

  if (error || templates.length === 0) {
    console.log('[EmailTemplates] Showing error state:', { error, templatesCount: templates.length });
    return (
      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
        <AlertCircle className="w-8 h-8 text-yellow-600 mx-auto mb-2" />
        <h3 className="font-medium text-yellow-800">No Email Templates Found</h3>
        <p className="text-sm text-yellow-700 mt-1">
          {error ? `Error: ${(error as Error).message}` : 'Email templates will be available once the Notification Service is running.'}
        </p>
        <button
          onClick={() => queryClient.invalidateQueries({ queryKey: ['emailTemplates'] })}
          className="mt-3 px-4 py-2 bg-yellow-600 text-white rounded-lg text-sm hover:bg-yellow-700"
        >
          <RefreshCw className="w-4 h-4 inline mr-1" /> Retry
        </button>
      </div>
    );
  }

  // Group templates by type
  const templatesByType = templates.reduce((acc, t) => {
    if (!acc[t.type]) acc[t.type] = [];
    acc[t.type].push(t);
    return acc;
  }, {} as Record<string, EmailTemplate[]>);

  const typeLabels: Record<string, string> = {
    ORDER_CONFIRMATION: 'Order Confirmation',
    ORDER_READY: 'Order Ready',
    ORDER_DELIVERED: 'Order Delivered',
    RECEIPT_READY: 'Receipt Ready',
    PAYMENT_RECEIVED: 'Payment Received',
    PAYMENT_SUCCESS: 'Payment Success',
    PAYMENT_FAILED: 'Payment Failed',
    WELCOME: 'Welcome Email',
    PASSWORD_RESET: 'Password Reset',
    PROMOTION: 'Promotion',
  };

  return (
    <div className="space-y-6">
      {/* Preview Modal */}
      {previewTemplate && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
            <div className="p-4 border-b flex justify-between items-center bg-gray-50">
              <div>
                <h3 className="font-semibold text-gray-800">Preview: {previewTemplate.name}</h3>
                <p className="text-sm text-gray-500">{previewTemplate.subject}</p>
              </div>
              <button
                onClick={() => setPreviewTemplate(null)}
                className="p-2 hover:bg-gray-200 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-4 overflow-auto max-h-[70vh]">
              <iframe
                srcDoc={previewTemplate.htmlTemplate
                  .replace(/\$\{customerName\}/g, 'Max Mustermann')
                  .replace(/\$\{orderNumber\}/g, '12345')
                  .replace(/\$\{totalAmount\}/g, '45.50')
                  .replace(/\$\{currency\}/g, 'CHF')
                  .replace(/\$\{orderType\}/g, 'Delivery')
                  .replace(/\$\{paymentMethod\}/g, 'Credit Card')
                  .replace(/\$\{pdfUrl\}/g, '#')
                  .replace(/\$\{pngUrl\}/g, '#')
                  .replace(/\$\{year\}/g, new Date().getFullYear().toString())
                }
                className="w-full h-[500px] border rounded"
                title="Email Preview"
              />
            </div>
          </div>
        </div>
      )}

      {/* Test Email Modal */}
      {showTestEmailModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
            <div className="p-4 border-b">
              <h3 className="font-semibold text-gray-800">Send Test Email</h3>
              <p className="text-sm text-gray-500">Send a test email with sample data</p>
            </div>
            <div className="p-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">Recipient Email</label>
              <input
                type="email"
                value={testEmailAddress}
                onChange={(e) => setTestEmailAddress(e.target.value)}
                placeholder="your@email.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
              {testEmailMutation.isError && (
                <p className="text-red-500 text-sm mt-2">Failed to send test email. Check SMTP settings.</p>
              )}
              {testEmailMutation.isSuccess && (
                <p className="text-green-500 text-sm mt-2">Test email sent successfully!</p>
              )}
            </div>
            <div className="p-4 border-t flex justify-end space-x-2">
              <button
                onClick={() => {
                  setShowTestEmailModal(false);
                  setTestEmailAddress('');
                }}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={sendTestEmail}
                disabled={!testEmailAddress || testEmailMutation.isPending}
                className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:opacity-50 flex items-center"
              >
                {testEmailMutation.isPending ? (
                  <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
                ) : (
                  <Send className="w-4 h-4 mr-2" />
                )}
                Send Test
              </button>
            </div>
          </div>
        </div>
      )}

      {selectedTemplate ? (
        // Edit Mode
        <div className="bg-white rounded-lg shadow">
          <div className="p-4 border-b flex justify-between items-center">
            <div>
              <h3 className="font-semibold text-gray-800">Edit: {selectedTemplate.name}</h3>
              <p className="text-sm text-gray-500">
                {typeLabels[selectedTemplate.type] || selectedTemplate.type} • {LANGUAGE_NAMES[selectedTemplate.language]?.native || selectedTemplate.language}
              </p>
            </div>
            <div className="flex space-x-2">
              <button
                onClick={() => openPreview({ ...selectedTemplate, htmlTemplate: editData.htmlTemplate })}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50 flex items-center"
              >
                <Eye className="w-4 h-4 mr-1" /> Preview
              </button>
              <button
                onClick={() => updateMutation.mutate()}
                disabled={updateMutation.isPending}
                className="px-3 py-1.5 bg-green-500 text-white rounded-lg text-sm font-medium hover:bg-green-600 flex items-center"
              >
                <Save className="w-4 h-4 mr-1" /> Save
              </button>
              <button
                onClick={() => setSelectedTemplate(null)}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50 flex items-center"
              >
                <X className="w-4 h-4 mr-1" /> Cancel
              </button>
            </div>
          </div>
          <div className="p-4 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Subject</label>
              <input
                type="text"
                value={editData.subject}
                onChange={(e) => setEditData({ ...editData, subject: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
                placeholder="Email subject line..."
              />
              <p className="text-xs text-gray-500 mt-1">
                Use {'${orderNumber}'}, {'${customerName}'}, {'${restaurantName}'} as placeholders
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">HTML Template</label>
              <textarea
                value={editData.htmlTemplate}
                onChange={(e) => setEditData({ ...editData, htmlTemplate: e.target.value })}
                rows={12}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none font-mono text-sm"
                placeholder="HTML email template..."
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Plain Text Template (optional)</label>
              <textarea
                value={editData.textTemplate}
                onChange={(e) => setEditData({ ...editData, textTemplate: e.target.value })}
                rows={5}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none font-mono text-sm"
                placeholder="Plain text fallback..."
              />
            </div>
          </div>
        </div>
      ) : (
        // List Mode
        Object.entries(templatesByType).map(([type, typeTemplates]) => (
          <div key={type} className="bg-white rounded-lg shadow">
            <div className="p-4 border-b">
              <h3 className="font-semibold text-gray-800 flex items-center">
                <Mail className="w-4 h-4 mr-2" />
                {typeLabels[type] || type.replace(/_/g, ' ')}
              </h3>
            </div>
            <div className="divide-y">
              {typeTemplates.map(template => (
                <div key={template.id} className="p-4 flex justify-between items-center hover:bg-gray-50">
                  <div>
                    <div className="font-medium text-gray-800">{template.name}</div>
                    <div className="text-sm text-gray-500">{template.subject}</div>
                    <div className="text-xs text-gray-400 mt-1">
                      {LANGUAGE_NAMES[template.language]?.native || template.language}
                      {template.isActive ? (
                        <span className="ml-2 text-green-600">• Active</span>
                      ) : (
                        <span className="ml-2 text-gray-400">• Inactive</span>
                      )}
                    </div>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => openPreview(template)}
                      className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-100 flex items-center"
                      title="Preview"
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => openTestEmailModal(template.id)}
                      className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-100 flex items-center"
                      title="Send Test Email"
                    >
                      <Send className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => startEdit(template)}
                      className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-100 flex items-center"
                    >
                      <Edit2 className="w-4 h-4 mr-1" /> Edit
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))
      )}
    </div>
  );
}

// SMTP Settings Component
function SmtpSettings() {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<Partial<SmtpConfig>>({
    host: '',
    port: 587,
    username: '',
    password: '',
    fromEmail: '',
    fromName: '',
    useSsl: false,
    useTls: true,
  });
  const [showPassword, setShowPassword] = useState(false);

  const { data: config, isLoading, error } = useQuery({
    queryKey: ['smtpConfig'],
    queryFn: settingsApi.getSmtpConfig,
    retry: 1,
  });

  useEffect(() => {
    if (config) {
      setFormData({
        host: config.host || '',
        port: config.port || 587,
        username: config.username || '',
        password: '', // Don't show existing password
        fromEmail: config.fromEmail || '',
        fromName: config.fromName || '',
        useSsl: config.useSsl || false,
        useTls: config.useTls ?? true,
      });
    }
  }, [config]);

  const updateMutation = useMutation({
    mutationFn: () => settingsApi.updateSmtpConfig(formData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['smtpConfig'] });
    },
  });

  const testMutation = useMutation({
    mutationFn: settingsApi.testSmtpConnection,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-48">
        <RefreshCw className="w-6 h-6 text-orange-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
          <Server className="w-5 h-5 mr-2" /> SMTP Server Configuration
        </h3>
        
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">SMTP Host</label>
              <input
                type="text"
                value={formData.host}
                onChange={(e) => setFormData({ ...formData, host: e.target.value })}
                placeholder="smtp.example.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Port</label>
              <input
                type="number"
                value={formData.port}
                onChange={(e) => setFormData({ ...formData, port: parseInt(e.target.value) })}
                placeholder="587"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                placeholder="your-email@example.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  placeholder="••••••••"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                >
                  {showPassword ? <X className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              <p className="text-xs text-gray-500 mt-1">Leave empty to keep existing password</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">From Email</label>
              <input
                type="email"
                value={formData.fromEmail}
                onChange={(e) => setFormData({ ...formData, fromEmail: e.target.value })}
                placeholder="noreply@restaurant.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">From Name</label>
              <input
                type="text"
                value={formData.fromName}
                onChange={(e) => setFormData({ ...formData, fromName: e.target.value })}
                placeholder="My Restaurant"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none"
              />
            </div>
          </div>

          <div className="flex space-x-6">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.useTls}
                onChange={(e) => setFormData({ ...formData, useTls: e.target.checked, useSsl: e.target.checked ? false : formData.useSsl })}
                className="w-4 h-4 text-orange-500 border-gray-300 rounded focus:ring-orange-500"
              />
              <span className="ml-2 text-sm text-gray-700">Use TLS (STARTTLS)</span>
            </label>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.useSsl}
                onChange={(e) => setFormData({ ...formData, useSsl: e.target.checked, useTls: e.target.checked ? false : formData.useTls })}
                className="w-4 h-4 text-orange-500 border-gray-300 rounded focus:ring-orange-500"
              />
              <span className="ml-2 text-sm text-gray-700">Use SSL/TLS</span>
            </label>
          </div>
        </div>

        <div className="mt-6 flex space-x-3">
          <button
            onClick={() => updateMutation.mutate()}
            disabled={updateMutation.isPending}
            className="px-4 py-2 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600 disabled:opacity-50 flex items-center"
          >
            {updateMutation.isPending ? (
              <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
            ) : (
              <Save className="w-4 h-4 mr-2" />
            )}
            Save Settings
          </button>
          <button
            onClick={() => testMutation.mutate()}
            disabled={testMutation.isPending}
            className="px-4 py-2 border border-gray-300 rounded-lg font-medium hover:bg-gray-50 disabled:opacity-50 flex items-center"
          >
            {testMutation.isPending ? (
              <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
            ) : (
              <TestTube className="w-4 h-4 mr-2" />
            )}
            Test Connection
          </button>
        </div>

        {updateMutation.isSuccess && (
          <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm flex items-center">
            <CheckCircle className="w-4 h-4 mr-2" /> Settings saved successfully!
          </div>
        )}

        {testMutation.isSuccess && (
          <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm flex items-center">
            <CheckCircle className="w-4 h-4 mr-2" /> SMTP connection successful!
          </div>
        )}

        {testMutation.isError && (
          <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm flex items-center">
            <AlertCircle className="w-4 h-4 mr-2" /> Failed to connect to SMTP server. Please check your settings.
          </div>
        )}
      </div>

      {/* Common SMTP Presets */}
      <div className="bg-white rounded-lg shadow p-6">
        <h4 className="font-medium text-gray-800 mb-3 flex items-center">
          <Shield className="w-4 h-4 mr-2" /> Quick Presets
        </h4>
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => setFormData({ ...formData, host: 'smtp.gmail.com', port: 587, useTls: true, useSsl: false })}
            className="px-3 py-1.5 border border-gray-200 rounded-lg text-sm hover:bg-gray-50"
          >
            Gmail
          </button>
          <button
            onClick={() => setFormData({ ...formData, host: 'smtp.office365.com', port: 587, useTls: true, useSsl: false })}
            className="px-3 py-1.5 border border-gray-200 rounded-lg text-sm hover:bg-gray-50"
          >
            Microsoft 365
          </button>
          <button
            onClick={() => setFormData({ ...formData, host: 'smtp.sendgrid.net', port: 587, useTls: true, useSsl: false })}
            className="px-3 py-1.5 border border-gray-200 rounded-lg text-sm hover:bg-gray-50"
          >
            SendGrid
          </button>
          <button
            onClick={() => setFormData({ ...formData, host: 'smtp.mailgun.org', port: 587, useTls: true, useSsl: false })}
            className="px-3 py-1.5 border border-gray-200 rounded-lg text-sm hover:bg-gray-50"
          >
            Mailgun
          </button>
          <button
            onClick={() => setFormData({ ...formData, host: 'email-smtp.eu-central-1.amazonaws.com', port: 587, useTls: true, useSsl: false })}
            className="px-3 py-1.5 border border-gray-200 rounded-lg text-sm hover:bg-gray-50"
          >
            AWS SES
          </button>
        </div>
      </div>
    </div>
  );
}

function NotificationStats() {
  const queryClient = useQueryClient();

  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['notificationStats'],
    queryFn: settingsApi.getNotificationStats,
    refetchInterval: 30000, // Refresh every 30 seconds
  });

  const retryMutation = useMutation({
    mutationFn: settingsApi.retryFailedNotifications,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificationStats'] });
    },
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-48">
        <RefreshCw className="w-6 h-6 text-orange-500 animate-spin" />
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
        <AlertCircle className="w-8 h-8 text-yellow-600 mx-auto mb-2" />
        <h3 className="font-medium text-yellow-800">Stats Unavailable</h3>
        <p className="text-sm text-yellow-700 mt-1">
          Notification statistics will be available once the Notification Service is running.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Stats Cards */}
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm text-gray-500">Sent</div>
              <div className="text-2xl font-bold text-green-600">{stats.sent}</div>
            </div>
            <CheckCircle className="w-8 h-8 text-green-200" />
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm text-gray-500">Pending</div>
              <div className="text-2xl font-bold text-yellow-600">{stats.pending}</div>
            </div>
            <Clock className="w-8 h-8 text-yellow-200" />
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm text-gray-500">Failed</div>
              <div className="text-2xl font-bold text-red-600">{stats.failed}</div>
            </div>
            <AlertCircle className="w-8 h-8 text-red-200" />
          </div>
        </div>
      </div>

      {/* By Type */}
      {stats.byType && stats.byType.length > 0 && (
        <div className="bg-white rounded-lg shadow">
          <div className="p-4 border-b">
            <h3 className="font-semibold text-gray-800">By Notification Type</h3>
          </div>
          <div className="p-4">
            <div className="space-y-2">
              {stats.byType.map(([type, count]) => (
                <div key={type} className="flex justify-between items-center py-2 border-b last:border-0">
                  <span className="text-gray-700">{type.replace(/_/g, ' ')}</span>
                  <span className="font-medium text-gray-900">{count}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Actions */}
      {stats.failed > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex justify-between items-center">
          <div>
            <h4 className="font-medium text-red-800">Failed Notifications</h4>
            <p className="text-sm text-red-700">
              {stats.failed} notification(s) failed to send. You can retry them.
            </p>
          </div>
          <button
            onClick={() => retryMutation.mutate()}
            disabled={retryMutation.isPending}
            className="px-4 py-2 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 flex items-center disabled:opacity-50"
          >
            {retryMutation.isPending ? (
              <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
            ) : (
              <RotateCcw className="w-4 h-4 mr-2" />
            )}
            Retry Failed
          </button>
        </div>
      )}
    </div>
  );
}

// =============================================================================
// Developer Settings - Fake Orders Generator
// =============================================================================

const FAKE_NAMES = [
  'Max Müller', 'Anna Schmidt', 'Peter Weber', 'Laura Fischer', 'Thomas Wagner',
  'Julia Hoffmann', 'Michael Bauer', 'Sarah Koch', 'Daniel Richter', 'Lisa Klein',
  'Markus Schröder', 'Katharina Wolf', 'Stefan Neumann', 'Nicole Braun', 'Andreas Schwarz',
];

const FAKE_STREETS = [
  'Bahnhofstrasse 15', 'Hauptstrasse 42', 'Dorfstrasse 7', 'Seeweg 23', 'Bergstrasse 8',
  'Kirchgasse 31', 'Schulweg 12', 'Mühlestrasse 5', 'Gartenweg 19', 'Lindenstrasse 28',
];

const FAKE_CITIES = [
  { city: 'Zürich', postal: '8001' },
  { city: 'Winterthur', postal: '8400' },
  { city: 'Basel', postal: '4051' },
  { city: 'Bern', postal: '3011' },
  { city: 'Luzern', postal: '6003' },
];

function DeveloperSettings() {
  const queryClient = useQueryClient();
  const [orderCount, setOrderCount] = useState(5);
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedCount, setGeneratedCount] = useState(0);
  const [error, setError] = useState<string | null>(null);

  // Fetch products to use in fake orders
  const { data: products = [] } = useQuery({
    queryKey: ['products'],
    queryFn: productsApi.getProducts,
  });

  const generateFakeOrders = async () => {
    if (products.length === 0) {
      setError('No products available. Please add products first.');
      return;
    }

    setIsGenerating(true);
    setGeneratedCount(0);
    setError(null);

    try {
      for (let i = 0; i < orderCount; i++) {
        // Random time offset: -5 minutes to +20 minutes from NOW
        const minMs = -5 * 60 * 1000;   // -5 minutes in ms
        const maxMs = 20 * 60 * 1000;   // +20 minutes in ms
        const randomOffsetMs = Math.floor(Math.random() * (maxMs - minMs + 1)) + minMs;
        
        // Calculate the estimated delivery time
        const estimatedTime = new Date(Date.now() + randomOffsetMs);
        
        // Format as LOCAL time string (not UTC!) for Java LocalDateTime
        // Format: "2025-12-05T10:45:30"
        const pad = (n: number) => n.toString().padStart(2, '0');
        const estimatedDelivery = `${estimatedTime.getFullYear()}-${pad(estimatedTime.getMonth() + 1)}-${pad(estimatedTime.getDate())}T${pad(estimatedTime.getHours())}:${pad(estimatedTime.getMinutes())}:${pad(estimatedTime.getSeconds())}`;

        // Random order type
        const orderType = Math.random() > 0.5 ? 'DELIVERY' : 'PICKUP';

        // Random customer
        const customerName = FAKE_NAMES[Math.floor(Math.random() * FAKE_NAMES.length)];
        const cityInfo = FAKE_CITIES[Math.floor(Math.random() * FAKE_CITIES.length)];
        const street = FAKE_STREETS[Math.floor(Math.random() * FAKE_STREETS.length)];

        // Random 1-5 items
        const itemCount = Math.floor(Math.random() * 5) + 1;
        const shuffledProducts = [...products].sort(() => Math.random() - 0.5);
        const selectedProducts = shuffledProducts.slice(0, Math.min(itemCount, products.length));

        const items = selectedProducts.map((product: Product) => ({
          productId: product.id,
          productName: product.name,
          quantity: Math.floor(Math.random() * 3) + 1,
          unitPrice: product.price,
          totalPrice: product.price * (Math.floor(Math.random() * 3) + 1),
        }));

        const totalPrice = items.reduce((sum, item) => sum + item.totalPrice, 0);

        const orderData = {
          customerName,
          customerEmail: `${customerName.toLowerCase().replace(' ', '.')}@example.com`,
          customerPhone: `+41 79 ${Math.floor(Math.random() * 900 + 100)} ${Math.floor(Math.random() * 9000 + 1000)}`,
          orderType,
          paymentMethod: Math.random() > 0.5 ? 'CARD' : 'CASH',
          notes: Math.random() > 0.7 ? 'Extra napkins please' : undefined,
          items,
          totalPrice,
          estimatedDelivery, // Random timer between -5sec and +20min
          // For delivery orders, include address
          ...(orderType === 'DELIVERY' && {
            deliveryAddress: {
              street,
              city: cityInfo.city,
              postalCode: cityInfo.postal,
            },
          }),
        };

        // Create the order via API
        await api.post('/api/orders', orderData);
        setGeneratedCount(i + 1);

        // Small delay to not overwhelm the server
        await new Promise(resolve => setTimeout(resolve, 100));
      }

      // Invalidate orders query to refresh KDS
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate orders');
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className="max-w-2xl">
      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
        <div className="flex items-start">
          <Code className="w-5 h-5 text-yellow-600 mt-0.5 mr-3" />
          <div>
            <h3 className="font-medium text-yellow-800">Developer Tools</h3>
            <p className="text-sm text-yellow-700 mt-1">
              These tools are for testing and development purposes only. 
              Use with caution in production environments.
            </p>
          </div>
        </div>
      </div>

      {/* Fake Order Generator */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center mb-4">
          <Zap className="w-5 h-5 text-purple-500 mr-2" />
          <h3 className="text-lg font-semibold text-gray-800">Fake Order Generator</h3>
        </div>

        <p className="text-sm text-gray-600 mb-4">
          Generate fake orders for testing the KDS. Orders will have random items, 
          customer names, and timers ranging from -5 seconds (overdue) to 20 minutes.
        </p>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Number of Orders
            </label>
            <div className="flex items-center space-x-4">
              <input
                type="range"
                min="1"
                max="50"
                value={orderCount}
                onChange={(e) => setOrderCount(parseInt(e.target.value))}
                className="flex-1 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-purple-500"
              />
              <input
                type="number"
                min="1"
                max="50"
                value={orderCount}
                onChange={(e) => setOrderCount(Math.min(50, Math.max(1, parseInt(e.target.value) || 1)))}
                className="w-20 px-3 py-2 border border-gray-300 rounded-lg text-center focus:ring-2 focus:ring-purple-500 outline-none"
              />
            </div>
          </div>

          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div>
              <div className="text-sm font-medium text-gray-700">Available Products</div>
              <div className="text-2xl font-bold text-gray-900">{products.length}</div>
            </div>
            <div className="text-right">
              <div className="text-sm font-medium text-gray-700">Timer Range</div>
              <div className="text-sm text-gray-600">-5 sec to +20 min</div>
            </div>
          </div>

          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          {isGenerating && (
            <div className="space-y-2">
              <div className="flex justify-between text-sm text-gray-600">
                <span>Generating orders...</span>
                <span>{generatedCount} / {orderCount}</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-purple-500 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${(generatedCount / orderCount) * 100}%` }}
                />
              </div>
            </div>
          )}

          <button
            onClick={generateFakeOrders}
            disabled={isGenerating || products.length === 0}
            className={`w-full py-3 rounded-lg font-medium flex items-center justify-center transition ${
              isGenerating || products.length === 0
                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-purple-500 text-white hover:bg-purple-600'
            }`}
          >
            {isGenerating ? (
              <>
                <RefreshCw className="w-5 h-5 mr-2 animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Zap className="w-5 h-5 mr-2" />
                Generate {orderCount} Fake Order{orderCount !== 1 ? 's' : ''}
              </>
            )}
          </button>

          {generatedCount > 0 && !isGenerating && (
            <div className="p-3 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm flex items-center">
              <Check className="w-4 h-4 mr-2" />
              Successfully generated {generatedCount} orders! Check the KDS.
            </div>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow p-6 mt-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-2 gap-4">
          <button
            onClick={() => window.location.href = '/kds'}
            className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 text-left transition"
          >
            <div className="font-medium text-gray-800">Open KDS</div>
            <div className="text-sm text-gray-500">Kitchen Display System</div>
          </button>
          <button
            onClick={() => window.location.href = '/products'}
            className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 text-left transition"
          >
            <div className="font-medium text-gray-800">Manage Products</div>
            <div className="text-sm text-gray-500">Add/edit menu items</div>
          </button>
        </div>
      </div>
    </div>
  );
}

export default SettingsPage;
