import api from './axios';
import type { PackingItem, PackingItemRequest, PackingSummary, PackingTemplateItem, TravelDocument, TravelDocumentRequest, ClimateType } from '../types/packing';

export const packingApi = {
  // ── Packing Items ─────────────────────────────────────────────────
  getItems: (tripId: number) =>
    api.get<{ data: PackingItem[] }>(`/packing/items/trip/${tripId}`).then(r => r.data.data),
  addItem: (request: PackingItemRequest) =>
    api.post<{ data: PackingItem }>('/packing/items', request).then(r => r.data.data),
  updateItem: (id: number, request: PackingItemRequest) =>
    api.put<{ data: PackingItem }>(`/packing/items/${id}`, request).then(r => r.data.data),
  togglePacked: (id: number) =>
    api.patch<{ data: PackingItem }>(`/packing/items/${id}/toggle`).then(r => r.data.data),
  deleteItem: (id: number) =>
    api.delete(`/packing/items/${id}`),
  getSummary: (tripId: number) =>
    api.get<{ data: PackingSummary }>(`/packing/summary/${tripId}`).then(r => r.data.data),

  // ── Templates ─────────────────────────────────────────────────────
  applyTemplate: (tripId: number, climate: ClimateType) =>
    api.post<{ data: PackingItem[] }>(`/packing/template/${tripId}?climate=${climate}`).then(r => r.data.data),
  previewTemplate: (climate: ClimateType) =>
    api.get<{ data: PackingTemplateItem[] }>(`/packing/template/preview?climate=${climate}`).then(r => r.data.data),
  getClimates: () =>
    api.get<{ data: string[] }>('/packing/climates').then(r => r.data.data),

  // ── Travel Documents ──────────────────────────────────────────────
  getDocuments: (tripId: number) =>
    api.get<{ data: TravelDocument[] }>(`/packing/documents/trip/${tripId}`).then(r => r.data.data),
  addDocument: (request: TravelDocumentRequest) =>
    api.post<{ data: TravelDocument }>('/packing/documents', request).then(r => r.data.data),
  updateDocument: (id: number, request: TravelDocumentRequest) =>
    api.put<{ data: TravelDocument }>(`/packing/documents/${id}`, request).then(r => r.data.data),
  deleteDocument: (id: number) =>
    api.delete(`/packing/documents/${id}`),
  getExpiringByTrip: (tripId: number) =>
    api.get<{ data: TravelDocument[] }>(`/packing/documents/expiring/trip/${tripId}`).then(r => r.data.data),
  getMyExpiring: () =>
    api.get<{ data: TravelDocument[] }>('/packing/documents/expiring/me').then(r => r.data.data),
};
