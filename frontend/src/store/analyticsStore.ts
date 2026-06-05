import { create } from 'zustand';
import { analyticsApi } from '../api/analyticsApi';
import type { TripAnalytics } from '../types/analytics';

interface AnalyticsState {
  analytics: TripAnalytics | null;
  isLoading: boolean;
  error: string | null;
  fetchAnalytics: (tripId: number) => Promise<void>;
}

export const useAnalyticsStore = create<AnalyticsState>((set) => ({
  analytics: null,
  isLoading: false,
  error: null,

  fetchAnalytics: async (tripId) => {
    set({ isLoading: true, error: null });
    try {
      const data = await analyticsApi.getTripAnalytics(tripId);
      set({ analytics: data, isLoading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.message || 'Failed to load analytics', isLoading: false });
    }
  },
}));
