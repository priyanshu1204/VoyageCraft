import api from './axios';
import type { NotificationData, NotificationPreference } from '../types/notification';

export const notificationApi = {
  getAll: () =>
    api.get<{ data: NotificationData[] }>('/notifications').then(r => r.data.data),

  getUnread: () =>
    api.get<{ data: NotificationData[] }>('/notifications/unread').then(r => r.data.data),

  getUnreadCount: () =>
    api.get<{ data: { count: number } }>('/notifications/unread/count').then(r => r.data.data.count),

  getByType: (type: string) =>
    api.get<{ data: NotificationData[] }>(`/notifications/type/${type}`).then(r => r.data.data),

  markAsRead: (id: number) =>
    api.patch(`/notifications/${id}/read`),

  markAllAsRead: () =>
    api.patch('/notifications/read-all'),

  dismiss: (id: number) =>
    api.patch(`/notifications/${id}/dismiss`),

  generateAlerts: (tripId: number) =>
    api.post<{ data: NotificationData[] }>(`/notifications/generate/${tripId}`).then(r => r.data.data),

  getPreferences: () =>
    api.get<{ data: NotificationPreference }>('/notifications/preferences').then(r => r.data.data),

  updatePreferences: (prefs: Partial<NotificationPreference>) =>
    api.put<{ data: NotificationPreference }>('/notifications/preferences', prefs).then(r => r.data.data),
};
