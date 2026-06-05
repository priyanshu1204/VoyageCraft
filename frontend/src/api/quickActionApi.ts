import api from './axios';
import type { QuickActionDashboard, QuickNote, QuickNoteRequest, ReorderRequest } from '../types/quickAction';

export const quickActionApi = {
  getDashboard: (tripId: number) =>
    api.get<{ data: QuickActionDashboard }>(`/quick-actions/dashboard/${tripId}`).then(r => r.data.data),

  toggleTransportCheckIn: (id: number) =>
    api.post(`/quick-actions/check-in/transport/${id}`).then(r => r.data),

  toggleStayCheckIn: (id: number) =>
    api.post(`/quick-actions/check-in/stay/${id}`).then(r => r.data),

  reorderDayItems: (request: ReorderRequest) =>
    api.post('/quick-actions/reorder', request).then(r => r.data),

  getNotes: (tripId: number) =>
    api.get<{ data: QuickNote[] }>(`/quick-actions/notes/${tripId}`).then(r => r.data.data),

  createNote: (request: QuickNoteRequest) =>
    api.post<{ data: QuickNote }>('/quick-actions/notes', request).then(r => r.data.data),

  updateNote: (id: number, request: QuickNoteRequest) =>
    api.put<{ data: QuickNote }>(`/quick-actions/notes/${id}`, request).then(r => r.data.data),

  deleteNote: (id: number) =>
    api.delete(`/quick-actions/notes/${id}`).then(r => r.data),

  registerPushToken: (token: string, deviceType = 'web') =>
    api.post('/quick-actions/push-token', { token, deviceType }).then(r => r.data),
};
