import api from './axios';
import type { ActivityRequest, ActivityResponse, ActivityCategory } from '../types/activity';
import type { ApiResponse } from '../types/trip';

export const activityApi = {
  add: (data: ActivityRequest) =>
    api.post<ApiResponse<ActivityResponse>>('/activities', data),

  update: (id: number, data: ActivityRequest) =>
    api.put<ApiResponse<ActivityResponse>>(`/activities/${id}`, data),

  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/activities/${id}`),

  getAll: (tripId: number) =>
    api.get<ApiResponse<ActivityResponse[]>>(`/activities/trip/${tripId}`),

  getByCategory: (tripId: number, category: ActivityCategory) =>
    api.get<ApiResponse<ActivityResponse[]>>(`/activities/trip/${tripId}/category/${category}`),

  getWaitlisted: (tripId: number) =>
    api.get<ApiResponse<ActivityResponse[]>>(`/activities/trip/${tripId}/waitlisted`),

  getReminders: (tripId: number) =>
    api.get<ApiResponse<ActivityResponse[]>>(`/activities/trip/${tripId}/reminders`),

  searchCatalog: (destination: string, category?: ActivityCategory) =>
    api.get<ApiResponse<ActivityResponse[]>>('/activities/catalog', { params: { destination, category } }),
};
