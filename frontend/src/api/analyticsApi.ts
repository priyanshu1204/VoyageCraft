import api from './axios';
import type { TripAnalytics } from '../types/analytics';

export const analyticsApi = {
  getTripAnalytics: (tripId: number) =>
    api.get<{ data: TripAnalytics }>(`/analytics/trip/${tripId}`).then(r => r.data.data),
};
