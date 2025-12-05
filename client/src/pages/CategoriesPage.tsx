import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productsApi, CreateCategoryRequest } from '../api/products';
import { Category, Product } from '../types';
import {
  Plus,
  Pencil,
  Trash2,
  Check,
  X,
  RefreshCw,
  Eye,
  EyeOff,
  GripVertical,
  FolderOpen,
  AlertCircle,
} from 'lucide-react';

// Helper to check category active status (handles both backend and frontend naming)
const isCategoryActive = (category: Category): boolean => {
  return category.isActive ?? category.active ?? true;
};

function CategoriesPage() {
  const queryClient = useQueryClient();
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [updateError, setUpdateError] = useState<string | null>(null);

  const { data: categories = [], isLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: productsApi.getCategories,
  });

  // Fetch products to count per category
  const { data: products = [] } = useQuery({
    queryKey: ['products'],
    queryFn: productsApi.getProducts,
  });

  // Build a map of categoryId -> product count
  const productCountByCategory = useMemo(() => {
    const counts: Record<number, number> = {};
    products.forEach((p: Product) => {
      counts[p.categoryId] = (counts[p.categoryId] || 0) + 1;
    });
    return counts;
  }, [products]);

  const createCategoryMutation = useMutation({
    mutationFn: (data: CreateCategoryRequest) => productsApi.createCategory(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      setIsCreating(false);
      setCreateError(null);
    },
    onError: (error: Error & { response?: { data?: { message?: string } } }) => {
      const message = error.response?.data?.message || error.message || 'Failed to create category';
      setCreateError(message);
    },
  });

  const updateCategoryMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CreateCategoryRequest }) =>
      productsApi.updateCategory(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      setEditingCategory(null);
      setUpdateError(null);
    },
    onError: (error: Error & { response?: { data?: { message?: string } } }) => {
      const message = error.response?.data?.message || error.message || 'Failed to update category';
      setUpdateError(message);
    },
  });

  const deleteCategoryMutation = useMutation({
    mutationFn: (id: number) => productsApi.deleteCategory(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
    onError: (error: Error & { response?: { data?: { message?: string } } }) => {
      const message = error.response?.data?.message || error.message || 'Failed to delete category';
      alert(message);
    },
  });

  const toggleActiveMutation = useMutation({
    mutationFn: (id: number) => productsApi.toggleCategoryActive(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });

  const handleDelete = (category: Category) => {
    if (window.confirm(`Are you sure you want to delete "${category.name}"?\n\nNote: Categories with products cannot be deleted.`)) {
      deleteCategoryMutation.mutate(category.id);
    }
  };

  if (isLoading) {
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
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <FolderOpen className="w-8 h-8 text-orange-500" />
            <div>
              <h1 className="text-2xl font-bold text-gray-800">Categories</h1>
              <p className="text-sm text-gray-500">{categories.length} categories</p>
            </div>
          </div>

          <button
            onClick={() => setIsCreating(true)}
            className="flex items-center px-4 py-2 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600 transition"
          >
            <Plus className="w-5 h-5 mr-2" />
            Add Category
          </button>
        </div>
      </div>

      {/* Categories List */}
      <div className="flex-1 p-6 overflow-auto">
        <div className="max-w-2xl mx-auto space-y-3">
          {/* Create New Category Form */}
          {isCreating && (
            <CategoryForm
              onSubmit={(data) => createCategoryMutation.mutate(data)}
              onCancel={() => {
                setIsCreating(false);
                setCreateError(null);
              }}
              isLoading={createCategoryMutation.isPending}
              error={createError}
            />
          )}

          {/* Category Cards */}
          {categories.map((category) => (
            <div key={category.id}>
              {editingCategory?.id === category.id ? (
                <CategoryForm
                  category={category}
                  onSubmit={(data) =>
                    updateCategoryMutation.mutate({ id: category.id, data })
                  }
                  onCancel={() => {
                    setEditingCategory(null);
                    setUpdateError(null);
                  }}
                  isLoading={updateCategoryMutation.isPending}
                  error={updateError}
                />
              ) : (
                <CategoryCard
                  category={category}
                  productCount={productCountByCategory[category.id] || 0}
                  onEdit={() => setEditingCategory(category)}
                  onDelete={() => handleDelete(category)}
                  onToggleActive={() => toggleActiveMutation.mutate(category.id)}
                  isTogglingActive={toggleActiveMutation.isPending}
                />
              )}
            </div>
          ))}

          {categories.length === 0 && !isCreating && (
            <div className="text-center py-12">
              <FolderOpen className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-600 mb-2">No categories yet</h3>
              <p className="text-gray-500 mb-4">Create your first category to start adding products.</p>
              <button
                onClick={() => setIsCreating(true)}
                className="px-4 py-2 bg-orange-500 text-white rounded-lg font-medium hover:bg-orange-600 transition"
              >
                Create Category
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// Category Card Component
interface CategoryCardProps {
  category: Category;
  productCount: number;
  onEdit: () => void;
  onDelete: () => void;
  onToggleActive: () => void;
  isTogglingActive: boolean;
}

function CategoryCard({ category, productCount, onEdit, onDelete, onToggleActive, isTogglingActive }: CategoryCardProps) {
  const active = isCategoryActive(category);
  
  return (
    <div
      className={`bg-white rounded-lg shadow-sm border-2 transition ${
        active ? 'border-transparent' : 'border-gray-200 opacity-60'
      }`}
    >
      <div className="flex items-center p-4">
        {/* Drag Handle (visual only for now) */}
        <div className="text-gray-300 mr-3 cursor-grab">
          <GripVertical className="w-5 h-5" />
        </div>

        {/* Category Info */}
        <div className="flex-1">
          <div className="flex items-center space-x-2">
            <h3 className="font-semibold text-gray-800">{category.name}</h3>
            {!active && (
              <span className="px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-500 rounded">
                Hidden
              </span>
            )}
          </div>
          <p className="text-sm text-gray-500">
            {productCount} {productCount === 1 ? 'Product' : 'Products'}
          </p>
        </div>

        {/* Actions */}
        <div className="flex items-center space-x-2">
          <button
            onClick={onToggleActive}
            disabled={isTogglingActive}
            className={`p-2 rounded-lg transition ${
              active
                ? 'text-green-600 hover:bg-green-50'
                : 'text-gray-400 hover:bg-gray-100'
            }`}
            title={active ? 'Hide category' : 'Show category'}
          >
            {active ? <Eye className="w-5 h-5" /> : <EyeOff className="w-5 h-5" />}
          </button>

          <button
            onClick={onEdit}
            className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
            title="Edit category"
          >
            <Pencil className="w-5 h-5" />
          </button>

          <button
            onClick={onDelete}
            className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
            title="Delete category"
          >
            <Trash2 className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
}

// Category Form Component
interface CategoryFormProps {
  category?: Category;
  onSubmit: (data: CreateCategoryRequest) => void;
  onCancel: () => void;
  isLoading: boolean;
  error?: string | null;
}

function CategoryForm({ category, onSubmit, onCancel, isLoading, error }: CategoryFormProps) {
  const [name, setName] = useState(category?.name || '');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    onSubmit({ name: name.trim() });
  };

  return (
    <form
      onSubmit={handleSubmit}
      className={`bg-white rounded-lg shadow-sm border-2 p-4 ${error ? 'border-red-300' : 'border-orange-200'}`}
    >
      <div className="flex items-center space-x-3">
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Category name..."
          autoFocus
          className={`flex-1 px-3 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none ${error ? 'border-red-300' : 'border-gray-300'}`}
        />

        <button
          type="submit"
          disabled={isLoading || !name.trim()}
          className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition disabled:opacity-50"
        >
          {isLoading ? (
            <RefreshCw className="w-5 h-5 animate-spin" />
          ) : (
            <Check className="w-5 h-5" />
          )}
        </button>

        <button
          type="button"
          onClick={onCancel}
          disabled={isLoading}
          className="p-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition"
        >
          <X className="w-5 h-5" />
        </button>
      </div>
      
      {error && (
        <div className="mt-2 flex items-center text-red-600 text-sm">
          <AlertCircle className="w-4 h-4 mr-1" />
          {error}
        </div>
      )}
    </form>
  );
}

export default CategoriesPage;
