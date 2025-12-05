import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { receiptsApi } from '../api/receipts';
import { Receipt } from '../types';
import { format, subDays, startOfWeek, endOfWeek, startOfMonth, endOfMonth, startOfYear, endOfYear } from 'date-fns';
import {
  Download,
  Search,
  Calendar,
  RefreshCw,
  TrendingUp,
  Receipt as ReceiptIcon,
  Package,
  Truck,
  ChevronRight,
} from 'lucide-react';

type DateRange = 'today' | 'yesterday' | 'week' | 'month' | 'year' | 'custom';

function ReceiptsPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [dateRange, setDateRange] = useState<DateRange>('today');
  const [customDate, setCustomDate] = useState('');
  const [selectedReceipt, setSelectedReceipt] = useState<Receipt | null>(null);

  const { startDate, endDate } = useMemo(() => {
    const now = new Date();
    switch (dateRange) {
      case 'today':
        const today = format(now, 'yyyy-MM-dd');
        return { startDate: today, endDate: today };
      case 'yesterday':
        const yesterday = format(subDays(now, 1), 'yyyy-MM-dd');
        return { startDate: yesterday, endDate: yesterday };
      case 'week':
        return {
          startDate: format(startOfWeek(now, { weekStartsOn: 1 }), 'yyyy-MM-dd'),
          endDate: format(endOfWeek(now, { weekStartsOn: 1 }), 'yyyy-MM-dd'),
        };
      case 'month':
        return {
          startDate: format(startOfMonth(now), 'yyyy-MM-dd'),
          endDate: format(endOfMonth(now), 'yyyy-MM-dd'),
        };
      case 'year':
        return {
          startDate: format(startOfYear(now), 'yyyy-MM-dd'),
          endDate: format(endOfYear(now), 'yyyy-MM-dd'),
        };
      case 'custom':
        return { startDate: customDate, endDate: customDate };
      default:
        return { startDate: format(now, 'yyyy-MM-dd'), endDate: format(now, 'yyyy-MM-dd') };
    }
  }, [dateRange, customDate]);

  const { data: receipts = [], isLoading: receiptsLoading } = useQuery({
    queryKey: ['receipts', startDate, endDate],
    queryFn: () => receiptsApi.getReceipts(startDate && endDate ? { startDate, endDate } : undefined),
  });

  const { data: dailyReport } = useQuery({
    queryKey: ['dailyReport', startDate],
    queryFn: () => receiptsApi.getDailyReport(startDate || undefined),
  });

  const filteredReceipts = receipts.filter((receipt) =>
    receipt.receiptNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
    receipt.customerName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleDownload = async (receipt: Receipt) => {
    try {
      await receiptsApi.downloadReceiptPdf(receipt.id, receipt.receiptNumber);
    } catch (error) {
      console.error('Failed to download PDF:', error);
      alert('Failed to download PDF. Please try again.');
    }
  };

  if (receiptsLoading) {
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
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Receipts</h1>
            <p className="text-sm text-gray-500">{receipts.length} receipts</p>
          </div>
        </div>

        {/* Filters */}
        <div className="flex items-center space-x-4">
          {/* Search */}
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search by receipt # or customer..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
            />
          </div>

          {/* Date Range Buttons */}
          <div className="flex items-center gap-2">
            {(['today', 'yesterday', 'week', 'month', 'year'] as DateRange[]).map((range) => (
              <button
                key={range}
                onClick={() => setDateRange(range)}
                className={`px-3 py-2 rounded-lg font-medium text-sm transition-colors ${
                  dateRange === range
                    ? 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {range.charAt(0).toUpperCase() + range.slice(1)}
              </button>
            ))}
            <button
              onClick={() => setDateRange('custom')}
              className={`px-3 py-2 rounded-lg font-medium text-sm transition-colors flex items-center gap-1 ${
                dateRange === 'custom'
                  ? 'bg-orange-500 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              <Calendar className="w-4 h-4" />
              Custom
            </button>
          </div>
        </div>

        {/* Custom Date Picker */}
        {dateRange === 'custom' && (
          <div className="flex items-center gap-2 mt-4">
            <Calendar className="w-5 h-5 text-gray-400" />
            <input
              type="date"
              value={customDate}
              onChange={(e) => setCustomDate(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
            />
          </div>
        )}
      </div>

      {/* Stats Cards */}
      {dailyReport && (
        <div className="grid grid-cols-4 gap-4 p-6 pb-0">
          <StatCard
            icon={ReceiptIcon}
            label="Total Receipts"
            value={dailyReport.totalReceipts.toString()}
            color="blue"
          />
          <StatCard
            icon={TrendingUp}
            label="Total Revenue"
            value={`CHF ${dailyReport.totalRevenue.toFixed(2)}`}
            color="green"
          />
          <StatCard
            icon={Package}
            label="Pickup Orders"
            value={`${dailyReport.pickupCount} (CHF ${dailyReport.pickupRevenue.toFixed(2)})`}
            color="orange"
          />
          <StatCard
            icon={Truck}
            label="Delivery Orders"
            value={`${dailyReport.deliveryCount} (CHF ${dailyReport.deliveryRevenue.toFixed(2)})`}
            color="purple"
          />
        </div>
      )}

      {/* Receipts List */}
      <div className="flex-1 p-6 overflow-auto">
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Receipt #</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Customer</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Type</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Items</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Total</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Date</th>
                <th className="px-4 py-3 text-right text-sm font-medium text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredReceipts.map((receipt) => (
                <tr key={receipt.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <span className="font-medium text-gray-800">{receipt.receiptNumber}</span>
                  </td>
                  <td className="px-4 py-3">
                    <span className="text-gray-600">{receipt.customerName}</span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${
                      receipt.orderType === 'DELIVERY' 
                        ? 'bg-purple-100 text-purple-700' 
                        : 'bg-orange-100 text-orange-700'
                    }`}>
                      {receipt.orderType === 'DELIVERY' ? (
                        <><Truck className="w-3 h-3 mr-1" /> Delivery</>
                      ) : (
                        <><Package className="w-3 h-3 mr-1" /> Pickup</>
                      )}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className="text-gray-600">{receipt.items.length} items</span>
                  </td>
                  <td className="px-4 py-3">
                    <span className="font-semibold text-gray-800">CHF {(receipt.totalAmount || 0).toFixed(2)}</span>
                  </td>
                  <td className="px-4 py-3">
                    <span className="text-gray-500 text-sm">
                      {format(new Date(receipt.createdAt), 'dd.MM.yyyy HH:mm')}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex items-center justify-end space-x-2">
                      <button
                        onClick={() => setSelectedReceipt(receipt)}
                        className="p-2 text-gray-500 hover:text-blue-500 hover:bg-blue-50 rounded transition"
                        title="View details"
                      >
                        <ChevronRight className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDownload(receipt)}
                        className="p-2 text-gray-500 hover:text-orange-500 hover:bg-orange-50 rounded transition"
                        title="Download PDF"
                      >
                        <Download className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {filteredReceipts.length === 0 && (
            <div className="text-center py-12 text-gray-500">
              No receipts found
            </div>
          )}
        </div>
      </div>

      {/* Receipt Detail Modal */}
      {selectedReceipt && (
        <ReceiptDetailModal
          receipt={selectedReceipt}
          onClose={() => setSelectedReceipt(null)}
          onDownload={() => handleDownload(selectedReceipt)}
        />
      )}
    </div>
  );
}

function StatCard({
  icon: Icon,
  label,
  value,
  color,
}: {
  icon: React.ElementType;
  label: string;
  value: string;
  color: 'blue' | 'green' | 'orange' | 'purple';
}) {
  const colors = {
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    orange: 'bg-orange-50 text-orange-600',
    purple: 'bg-purple-50 text-purple-600',
  };

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <div className="flex items-center">
        <div className={`p-2 rounded-lg ${colors[color]}`}>
          <Icon className="w-5 h-5" />
        </div>
        <div className="ml-3">
          <p className="text-sm text-gray-500">{label}</p>
          <p className="font-semibold text-gray-800">{value}</p>
        </div>
      </div>
    </div>
  );
}

function ReceiptDetailModal({
  receipt,
  onClose,
  onDownload,
}: {
  receipt: Receipt;
  onClose: () => void;
  onDownload: () => void;
}) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg m-4">
        <div className="flex items-center justify-between p-4 border-b">
          <h2 className="text-lg font-bold">Receipt #{receipt.receiptNumber}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            ✕
          </button>
        </div>

        <div className="p-4 space-y-4">
          {/* Customer Info */}
          <div className="pb-4 border-b">
            <p className="font-medium text-gray-800">{receipt.customerName}</p>
            {receipt.customerPhone && (
              <p className="text-sm text-gray-500">{receipt.customerPhone}</p>
            )}
            {receipt.customerAddress && (
              <p className="text-sm text-gray-500">{receipt.customerAddress}</p>
            )}
          </div>

          {/* Items */}
          <div className="space-y-2">
            {receipt.items.map((item, index) => (
              <div key={index} className="flex justify-between">
                <span>
                  <span className="font-medium text-orange-500">{item.quantity}x</span>{' '}
                  {item.productName}
                </span>
                <span className="text-gray-600">CHF {item.totalPrice.toFixed(2)}</span>
              </div>
            ))}
          </div>

          {/* Totals */}
          <div className="pt-4 border-t space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">Subtotal</span>
              <span>CHF {(receipt.subtotal || 0).toFixed(2)}</span>
            </div>
            {(receipt.deliveryFee || 0) > 0 && (
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Delivery Fee</span>
                <span>CHF {(receipt.deliveryFee || 0).toFixed(2)}</span>
              </div>
            )}
            <div className="flex justify-between font-bold text-lg">
              <span>Total</span>
              <span>CHF {(receipt.totalAmount || 0).toFixed(2)}</span>
            </div>
          </div>

          {/* Payment Method */}
          <div className="pt-4 border-t">
            <p className="text-sm text-gray-500">
              Payment: <span className="text-gray-800">{receipt.paymentMethod || 'N/A'}</span>
            </p>
            <p className="text-sm text-gray-500">
              Date: <span className="text-gray-800">{format(new Date(receipt.createdAt), 'dd.MM.yyyy HH:mm')}</span>
            </p>
          </div>
        </div>

        <div className="flex justify-end space-x-3 p-4 border-t">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50"
          >
            Close
          </button>
          <button
            onClick={onDownload}
            className="px-4 py-2 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600 flex items-center"
          >
            <Download className="w-4 h-4 mr-2" />
            Download PDF
          </button>
        </div>
      </div>
    </div>
  );
}

export default ReceiptsPage;
