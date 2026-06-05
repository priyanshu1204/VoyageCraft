import api from './axios';
import type { TravelDocumentData, TravelDocumentRequest, ChecklistItem, Reminder, CountryLibraryEntry } from '../types/travelDocument';

export const travelDocApi = {
  create: (tripId: number, data: TravelDocumentRequest) =>
    api.post<{ data: TravelDocumentData }>(`/documents/trip/${tripId}`, data).then(r => r.data.data),

  update: (docId: number, data: TravelDocumentRequest) =>
    api.put<{ data: TravelDocumentData }>(`/documents/${docId}`, data).then(r => r.data.data),

  getTripDocs: (tripId: number) =>
    api.get<{ data: TravelDocumentData[] }>(`/documents/trip/${tripId}`).then(r => r.data.data),

  getDoc: (docId: number) =>
    api.get<{ data: TravelDocumentData }>(`/documents/${docId}`).then(r => r.data.data),

  deleteDoc: (docId: number) =>
    api.delete(`/documents/${docId}`),

  addChecklist: (docId: number, item: { itemName: string; itemType: string; description?: string; documentLink?: string }) =>
    api.post<{ data: ChecklistItem }>(`/documents/${docId}/checklist`, item).then(r => r.data.data),

  toggleChecklist: (itemId: number) =>
    api.patch<{ data: ChecklistItem }>(`/documents/checklist/${itemId}/toggle`).then(r => r.data.data),

  deleteChecklist: (itemId: number) =>
    api.delete(`/documents/checklist/${itemId}`),

  addReminder: (docId: number, reminder: { title: string; note?: string; reminderDate: string }) =>
    api.post<{ data: Reminder }>(`/documents/${docId}/reminders`, reminder).then(r => r.data.data),

  dismissReminder: (reminderId: number) =>
    api.patch<{ data: Reminder }>(`/documents/reminders/${reminderId}/dismiss`).then(r => r.data.data),

  deleteReminder: (reminderId: number) =>
    api.delete(`/documents/reminders/${reminderId}`),

  getUpcomingReminders: () =>
    api.get<{ data: Reminder[] }>(`/documents/reminders/upcoming`).then(r => r.data.data),

  getCountryLibrary: () =>
    api.get<{ data: CountryLibraryEntry[] }>(`/documents/library`).then(r => r.data.data),
};
