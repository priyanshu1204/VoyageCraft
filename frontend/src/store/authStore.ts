import { create } from 'zustand';
import { AuthResponse } from '../types/auth';
import { authApi } from '../api/authApi';
import { LoginRequest, RegisterRequest } from '../types/auth';

interface AuthState {
  user: AuthResponse | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  loadFromStorage: () => void;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  login: async (data) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authApi.login(data);
      const authData = response.data.data;
      localStorage.setItem('token', authData.token);
      localStorage.setItem('user', JSON.stringify(authData));
      set({ user: authData, token: authData.token, isAuthenticated: true, isLoading: false });
    } catch (err: any) {
      const message = err.response?.data?.message || 'Login failed';
      set({ error: message, isLoading: false });
      throw new Error(message);
    }
  },

  register: async (data) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authApi.register(data);
      const authData = response.data.data;
      localStorage.setItem('token', authData.token);
      localStorage.setItem('user', JSON.stringify(authData));
      set({ user: authData, token: authData.token, isAuthenticated: true, isLoading: false });
    } catch (err: any) {
      const message = err.response?.data?.message || 'Registration failed';
      set({ error: message, isLoading: false });
      throw new Error(message);
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({ user: null, token: null, isAuthenticated: false });
  },

  loadFromStorage: () => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        set({ user, token, isAuthenticated: true });
      } catch {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
  },

  clearError: () => set({ error: null }),
}));
