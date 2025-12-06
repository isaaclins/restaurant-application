import { create } from 'zustand';
import { persist } from 'zustand/middleware';
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

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
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
          set({
            user: response.user,
            isAuthenticated: true,
            loading: false,
          });
        } catch (error) {
          set({
            loading: false,
            error: error instanceof Error ? error.message : 'Login failed',
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
          set({
            user: response.user,
            isAuthenticated: true,
            loading: false,
          });
        } catch (error) {
          set({
            loading: false,
            error: error instanceof Error ? error.message : 'Registration failed',
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
          set({ user, isAuthenticated: true, loading: false });
        } catch {
          // Token might be invalid
          localStorage.removeItem('customerAccessToken');
          localStorage.removeItem('customerRefreshToken');
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
