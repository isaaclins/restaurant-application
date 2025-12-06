import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { cartApi } from '../api/cart';
import type { Cart, CartItem, AddToCartRequest } from '../types';

// Generate a UUID for cart session
const generateSessionId = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
};

interface CartState {
  sessionId: string;
  items: CartItem[];
  totalItems: number;
  totalPrice: number;
  loading: boolean;
  error: string | null;

  // Actions
  initSession: () => void;
  fetchCart: () => Promise<void>;
  addItem: (item: AddToCartRequest) => Promise<void>;
  removeItem: (itemId: string) => Promise<void>;
  updateQuantity: (itemId: string, quantity: number) => Promise<void>;
  clearCart: () => void;
  setCartFromResponse: (cart: Cart) => void;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      sessionId: '',
      items: [],
      totalItems: 0,
      totalPrice: 0,
      loading: false,
      error: null,

      initSession: () => {
        const { sessionId } = get();
        if (!sessionId) {
          set({ sessionId: generateSessionId() });
        }
      },

      setCartFromResponse: (cart: Cart) => {
        set({
          items: cart.items || [],
          totalItems: cart.totalItems || 0,
          totalPrice: cart.totalPrice || 0,
          loading: false,
          error: null,
        });
      },

      fetchCart: async () => {
        const { sessionId, setCartFromResponse } = get();
        if (!sessionId) return;

        set({ loading: true, error: null });
        try {
          const cart = await cartApi.getCart(sessionId);
          setCartFromResponse(cart);
        } catch {
          // Cart might not exist yet, that's okay
          set({ loading: false });
        }
      },

      addItem: async (item: AddToCartRequest) => {
        const { sessionId, setCartFromResponse } = get();
        if (!sessionId) return;

        set({ loading: true, error: null });
        try {
          const cart = await cartApi.addItem(sessionId, item);
          setCartFromResponse(cart);
        } catch (error) {
          set({
            loading: false,
            error: error instanceof Error ? error.message : 'Failed to add item',
          });
          throw error;
        }
      },

      removeItem: async (itemId: string) => {
        const { sessionId, setCartFromResponse } = get();
        if (!sessionId) return;

        set({ loading: true, error: null });
        try {
          const cart = await cartApi.removeItem(sessionId, itemId);
          setCartFromResponse(cart);
        } catch (error) {
          set({
            loading: false,
            error: error instanceof Error ? error.message : 'Failed to remove item',
          });
          throw error;
        }
      },

      updateQuantity: async (itemId: string, quantity: number) => {
        const { sessionId, setCartFromResponse, removeItem } = get();
        if (!sessionId) return;

        if (quantity <= 0) {
          await removeItem(itemId);
          return;
        }

        set({ loading: true, error: null });
        try {
          const cart = await cartApi.updateItemQuantity(sessionId, itemId, quantity);
          setCartFromResponse(cart);
        } catch (error) {
          set({
            loading: false,
            error: error instanceof Error ? error.message : 'Failed to update quantity',
          });
          throw error;
        }
      },

      clearCart: () => {
        const { sessionId } = get();
        if (sessionId) {
          cartApi.clearCart(sessionId).catch(() => {});
        }
        set({
          sessionId: generateSessionId(),
          items: [],
          totalItems: 0,
          totalPrice: 0,
          error: null,
        });
      },
    }),
    {
      name: 'cart-storage',
      partialize: (state) => ({
        sessionId: state.sessionId,
      }),
    }
  )
);
