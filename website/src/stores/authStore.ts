import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import axios from 'axios';
import { authApi } from '../api/auth';
import type { Customer, LoginRequest, RegisterRequest } from '../types';

interface AuthState {
  user: Customer | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;

  // Actions
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  fetchProfile: () => Promise<void>;
  updateProfile: (data: Partial<Customer>) => Promise<void>;
  clearError: () => void;
}

const extractErrorMessage = (error: unknown, fallback: string) => {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as any;
    if (data) {
      if (typeof data.message === 'string') return data.message;
      if (typeof data.error === 'string') return data.error;
      if (typeof data.detail === 'string') return data.detail;
      // last resort: serialize
      try {
        return JSON.stringify(data);
      } catch {
        /* ignore */
      }
    }
    return error.message || fallback;
  }
  if (error instanceof Error) return error.message;
  return fallback;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      loading: false,
      error: null,

      login: async (data: LoginRequest) => {
        set({ loading: true, error: null });
        try {
          const response = await authApi.login(data);
          localStorage.setItem('customerAccessToken', response.accessToken);
          localStorage.setItem('customerRefreshToken', response.refreshToken);
          if (response.user?.id) {
            localStorage.setItem('customerUserId', String(response.user.id));
          }
          set({
            user: response.user,
            isAuthenticated: true,
            loading: false,
          });
        } catch (error) {
          set({
            loading: false,
            error: extractErrorMessage(error, 'Login failed'),
          });
          throw error;
        }
      },

      register: async (data: RegisterRequest) => {
        set({ loading: true, error: null });
        try {
          const response = await authApi.register(data);
          localStorage.setItem('customerAccessToken', response.accessToken);
          localStorage.setItem('customerRefreshToken', response.refreshToken);
          if (response.user?.id) {
            localStorage.setItem('customerUserId', String(response.user.id));
          }
          set({
            user: response.user,
            isAuthenticated: true,
            loading: false,
          });
        } catch (error) {
          set({
            loading: false,
            error: extractErrorMessage(error, 'Registration failed'),
          });
          throw error;
        }
      },

      logout: async () => {
        set({ loading: true });
        try {
          await authApi.logout();
        } catch {
          // Ignore logout errors
        } finally {
          localStorage.removeItem('customerAccessToken');
          localStorage.removeItem('customerRefreshToken');
          localStorage.removeItem('customerUserId');
          set({
            user: null,
            isAuthenticated: false,
            loading: false,
            error: null,
          });
        }
      },

      fetchProfile: async () => {
        const token = localStorage.getItem('customerAccessToken');
        if (!token) return;

        set({ loading: true });
        try {
          const user = await authApi.getProfile();
          if (user?.id) {
            localStorage.setItem('customerUserId', String(user.id));
          }
          set({ user, isAuthenticated: true, loading: false });
        } catch {
          // Token might be invalid
          localStorage.removeItem('customerAccessToken');
          localStorage.removeItem('customerRefreshToken');
          localStorage.removeItem('customerUserId');
          set({ user: null, isAuthenticated: false, loading: false });
        }
      },

      updateProfile: async (data: Partial<Customer>) => {
        set({ loading: true, error: null });
        try {
          const user = await authApi.updateProfile(data);
          set({ user, loading: false });
        } catch (error) {
          set({
            loading: false,
            error: error instanceof Error ? error.message : 'Update failed',
          });
          throw error;
        }
      },

      clearError: () => set({ error: null }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
