import { create } from 'zustand';
import { weatherApi } from '../api/weatherApi';
import type { WeatherForecast, WeatherAlert, SeasonalAdvice } from '../types/weather';

interface WeatherState {
  forecasts: WeatherForecast[];
  alerts: WeatherAlert[];
  seasonalAdvice: SeasonalAdvice[];
  isLoading: boolean;

  generateForecast: (tripId: number) => Promise<void>;
  fetchForecasts: (tripId: number) => Promise<void>;
  fetchAlerts: (tripId: number) => Promise<void>;
  fetchSeasonalAdvice: (tripId: number) => Promise<void>;
}

export const useWeatherStore = create<WeatherState>((set) => ({
  forecasts: [],
  alerts: [],
  seasonalAdvice: [],
  isLoading: false,

  generateForecast: async (tripId) => {
    set({ isLoading: true });
    try {
      const forecasts = await weatherApi.generateForecast(tripId);
      set({ forecasts });
    } finally {
      set({ isLoading: false });
    }
  },

  fetchForecasts: async (tripId) => {
    set({ isLoading: true });
    try {
      const forecasts = await weatherApi.getTripForecasts(tripId);
      set({ forecasts });
    } finally {
      set({ isLoading: false });
    }
  },

  fetchAlerts: async (tripId) => {
    try {
      const alerts = await weatherApi.getAlerts(tripId);
      set({ alerts });
    } catch { /* ignore */ }
  },

  fetchSeasonalAdvice: async (tripId) => {
    try {
      const seasonalAdvice = await weatherApi.getSeasonalAdvice(tripId);
      set({ seasonalAdvice });
    } catch { /* ignore */ }
  },
}));
