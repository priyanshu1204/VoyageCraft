import api from './axios';
import type { NavigationRoute, NavigationRouteRequest, DaySheet } from '../types/navigation';

export const navigationApi = {
  addRoute: (tripId: number, data: NavigationRouteRequest) =>
    api.post<{ data: NavigationRoute }>(`/navigation/trip/${tripId}`, data).then(r => r.data.data),

  updateRoute: (routeId: number, data: NavigationRouteRequest) =>
    api.put<{ data: NavigationRoute }>(`/navigation/${routeId}`, data).then(r => r.data.data),

  switchMode: (routeId: number, mode: string) =>
    api.patch<{ data: NavigationRoute }>(`/navigation/${routeId}/mode/${mode}`).then(r => r.data.data),

  getTripRoutes: (tripId: number) =>
    api.get<{ data: NavigationRoute[] }>(`/navigation/trip/${tripId}`).then(r => r.data.data),

  getDayRoutes: (tripId: number, dayNumber: number) =>
    api.get<{ data: NavigationRoute[] }>(`/navigation/trip/${tripId}/day/${dayNumber}`).then(r => r.data.data),

  deleteRoute: (routeId: number) =>
    api.delete(`/navigation/${routeId}`),

  getDaySheets: (tripId: number) =>
    api.get<{ data: DaySheet[] }>(`/navigation/trip/${tripId}/daysheets`).then(r => r.data.data),
};
