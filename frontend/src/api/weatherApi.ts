import api from './axios';
import type { WeatherForecast, WeatherAlert, SeasonalAdvice } from '../types/weather';

export const weatherApi = {
  generateForecast: (tripId: number) =>
    api.post<{ data: WeatherForecast[] }>(`/weather/generate/${tripId}`).then(r => r.data.data),

  getTripForecasts: (tripId: number) =>
    api.get<{ data: WeatherForecast[] }>(`/weather/trip/${tripId}`).then(r => r.data.data),

  getForecastByDate: (tripId: number, date: string) =>
    api.get<{ data: WeatherForecast[] }>(`/weather/trip/${tripId}/date/${date}`).then(r => r.data.data),

  getAlerts: (tripId: number) =>
    api.get<{ data: WeatherAlert[] }>(`/weather/alerts/${tripId}`).then(r => r.data.data),

  getSeasonalAdvice: (tripId: number) =>
    api.get<{ data: SeasonalAdvice[] }>(`/weather/seasonal/${tripId}`).then(r => r.data.data),
};
