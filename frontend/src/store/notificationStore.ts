import { create } from 'zustand';
import { notificationApi } from '../api/notificationApi';
import type { NotificationData, NotificationPreference } from '../types/notification';

interface NotificationState {
  notifications: NotificationData[];
  unreadCount: number;
  preferences: NotificationPreference | null;
  isLoading: boolean;

  fetchNotifications: () => Promise<void>;
  fetchUnreadCount: () => Promise<void>;
  markAsRead: (id: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  dismiss: (id: number) => Promise<void>;
  generateAlerts: (tripId: number) => Promise<void>;
  fetchPreferences: () => Promise<void>;
  updatePreferences: (prefs: Partial<NotificationPreference>) => Promise<void>;
}

export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],
  unreadCount: 0,
  preferences: null,
  isLoading: false,

  fetchNotifications: async () => {
    set({ isLoading: true });
    try {
      const notifications = await notificationApi.getAll();
      set({ notifications });
    } finally { set({ isLoading: false }); }
  },

  fetchUnreadCount: async () => {
    try {
      const unreadCount = await notificationApi.getUnreadCount();
      set({ unreadCount });
    } catch { /* ignore */ }
  },

  markAsRead: async (id) => {
    await notificationApi.markAsRead(id);
    const notifications = await notificationApi.getAll();
    const unreadCount = await notificationApi.getUnreadCount();
    set({ notifications, unreadCount });
  },

  markAllAsRead: async () => {
    await notificationApi.markAllAsRead();
    const notifications = await notificationApi.getAll();
    set({ notifications, unreadCount: 0 });
  },

  dismiss: async (id) => {
    await notificationApi.dismiss(id);
    const notifications = await notificationApi.getAll();
    const unreadCount = await notificationApi.getUnreadCount();
    set({ notifications, unreadCount });
  },

  generateAlerts: async (tripId) => {
    await notificationApi.generateAlerts(tripId);
    const notifications = await notificationApi.getAll();
    const unreadCount = await notificationApi.getUnreadCount();
    set({ notifications, unreadCount });
  },

  fetchPreferences: async () => {
    const preferences = await notificationApi.getPreferences();
    set({ preferences });
  },

  updatePreferences: async (prefs) => {
    const preferences = await notificationApi.updatePreferences(prefs);
    set({ preferences });
  },
}));
