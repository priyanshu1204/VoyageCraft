import { create } from 'zustand';
import { navigationApi } from '../api/navigationApi';
import type { NavigationRoute, NavigationRouteRequest, DaySheet } from '../types/navigation';

interface NavigationState {
  routes: NavigationRoute[];
  daySheets: DaySheet[];
  isLoading: boolean;

  fetchRoutes: (tripId: number) => Promise<void>;
  addRoute: (tripId: number, data: NavigationRouteRequest) => Promise<void>;
  updateRoute: (routeId: number, data: NavigationRouteRequest, tripId: number) => Promise<void>;
  switchMode: (routeId: number, mode: string, tripId: number) => Promise<void>;
  deleteRoute: (routeId: number, tripId: number) => Promise<void>;
  fetchDaySheets: (tripId: number) => Promise<void>;
}

export const useNavigationStore = create<NavigationState>((set) => ({
  routes: [],
  daySheets: [],
  isLoading: false,

  fetchRoutes: async (tripId) => {
    set({ isLoading: true });
    try {
      const routes = await navigationApi.getTripRoutes(tripId);
      set({ routes });
    } finally { set({ isLoading: false }); }
  },

  addRoute: async (tripId, data) => {
    await navigationApi.addRoute(tripId, data);
    const routes = await navigationApi.getTripRoutes(tripId);
    set({ routes });
  },

  updateRoute: async (routeId, data, tripId) => {
    await navigationApi.updateRoute(routeId, data);
    const routes = await navigationApi.getTripRoutes(tripId);
    set({ routes });
  },

  switchMode: async (routeId, mode, tripId) => {
    await navigationApi.switchMode(routeId, mode);
    const routes = await navigationApi.getTripRoutes(tripId);
    set({ routes });
  },

  deleteRoute: async (routeId, tripId) => {
    await navigationApi.deleteRoute(routeId);
    const routes = await navigationApi.getTripRoutes(tripId);
    set({ routes });
  },

  fetchDaySheets: async (tripId) => {
    const daySheets = await navigationApi.getDaySheets(tripId);
    set({ daySheets });
  },
}));
