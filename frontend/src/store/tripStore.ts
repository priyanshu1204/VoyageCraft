import { create } from 'zustand';
import { TripResponse, TripDashboardResponse, TripRequest } from '../types/trip';
import { tripApi } from '../api/tripApi';

interface TripState {
  trips: TripResponse[];
  currentTrip: TripResponse | null;
  dashboard: TripDashboardResponse | null;
  isLoading: boolean;
  error: string | null;
  fetchTrips: () => Promise<void>;
  fetchDashboard: () => Promise<void>;
  fetchTrip: (id: number) => Promise<void>;
  createTrip: (data: TripRequest) => Promise<TripResponse>;
  updateTrip: (id: number, data: TripRequest) => Promise<void>;
  deleteTrip: (id: number) => Promise<void>;
  clearError: () => void;
}

export const useTripStore = create<TripState>((set) => ({
  trips: [],
  currentTrip: null,
  dashboard: null,
  isLoading: false,
  error: null,

  fetchTrips: async () => {
    set({ isLoading: true });
    try {
      const res = await tripApi.getAll();
      set({ trips: res.data.data, isLoading: false });
    } catch (err: any) {
      set({ error: err.response?.data?.message || 'Failed to fetch trips', isLoading: false });
    }
  },

  fetchDashboard: async () => {
    set({ isLoading: true });
    try {
      const res = await tripApi.getDashboard();
      set({ dashboard: res.data.data, isLoading: false });
    } catch (err: any) {
      set({ error: err.response?.data?.message || 'Failed to fetch dashboard', isLoading: false });
    }
  },

  fetchTrip: async (id) => {
    set({ isLoading: true });
    try {
      const res = await tripApi.getById(id);
      set({ currentTrip: res.data.data, isLoading: false });
    } catch (err: any) {
      set({ error: err.response?.data?.message || 'Failed to fetch trip', isLoading: false });
    }
  },

  createTrip: async (data) => {
    set({ isLoading: true });
    try {
      const res = await tripApi.create(data);
      const newTrip = res.data.data;
      set((state) => ({ trips: [newTrip, ...state.trips], isLoading: false }));
      return newTrip;
    } catch (err: any) {
      set({ error: err.response?.data?.message || 'Failed to create trip', isLoading: false });
      throw err;
    }
  },

  updateTrip: async (id, data) => {
    set({ isLoading: true });
    try {
      const res = await tripApi.update(id, data);
      const updated = res.data.data;
      set((state) => ({
        trips: state.trips.map((t) => (t.id === id ? updated : t)),
        currentTrip: state.currentTrip?.id === id ? updated : state.currentTrip,
        isLoading: false,
      }));
    } catch (err: any) {
      set({ error: err.response?.data?.message || 'Failed to update trip', isLoading: false });
      throw err;
    }
  },

  deleteTrip: async (id) => {
    set({ isLoading: true });
    try {
      await tripApi.delete(id);
      set((state) => ({
        trips: state.trips.filter((t) => t.id !== id),
        currentTrip: state.currentTrip?.id === id ? null : state.currentTrip,
        isLoading: false,
      }));
    } catch (err: any) {
      set({ error: err.response?.data?.message || 'Failed to delete trip', isLoading: false });
      throw err;
    }
  },

  clearError: () => set({ error: null }),
}));
