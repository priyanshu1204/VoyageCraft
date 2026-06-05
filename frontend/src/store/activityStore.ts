import { create } from 'zustand';
import { activityApi } from '../api/activityApi';
import type { ActivityResponse, ActivityRequest, ActivityCategory } from '../types/activity';

interface ActivityState {
  activities: ActivityResponse[];
  catalogResults: ActivityResponse[];
  isLoading: boolean;
  isSearching: boolean;

  fetchActivities: (tripId: number) => Promise<void>;
  addActivity: (data: ActivityRequest) => Promise<void>;
  updateActivity: (id: number, data: ActivityRequest) => Promise<void>;
  deleteActivity: (id: number, tripId: number) => Promise<void>;
  searchCatalog: (destination: string, category?: ActivityCategory) => Promise<void>;
  clearCatalog: () => void;
}

export const useActivityStore = create<ActivityState>((set, get) => ({
  activities: [],
  catalogResults: [],
  isLoading: false,
  isSearching: false,

  fetchActivities: async (tripId) => {
    set({ isLoading: true });
    try {
      const res = await activityApi.getAll(tripId);
      set({ activities: res.data.data });
    } finally {
      set({ isLoading: false });
    }
  },

  addActivity: async (data) => {
    await activityApi.add(data);
    await get().fetchActivities(data.tripId);
  },

  updateActivity: async (id, data) => {
    await activityApi.update(id, data);
    await get().fetchActivities(data.tripId);
  },

  deleteActivity: async (id, tripId) => {
    await activityApi.delete(id);
    await get().fetchActivities(tripId);
  },

  searchCatalog: async (destination, category) => {
    set({ isSearching: true });
    try {
      const res = await activityApi.searchCatalog(destination, category);
      set({ catalogResults: res.data.data });
    } finally {
      set({ isSearching: false });
    }
  },

  clearCatalog: () => set({ catalogResults: [] }),
}));
