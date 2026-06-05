import { create } from 'zustand';
import { packingApi } from '../api/packingApi';
import type { PackingItem, PackingItemRequest, PackingSummary, PackingTemplateItem, TravelDocument, TravelDocumentRequest, ClimateType } from '../types/packing';

interface PackingState {
  items: PackingItem[];
  summary: PackingSummary | null;
  documents: TravelDocument[];
  expiringDocs: TravelDocument[];
  templatePreview: PackingTemplateItem[];
  isLoading: boolean;

  fetchItems: (tripId: number) => Promise<void>;
  addItem: (request: PackingItemRequest) => Promise<void>;
  updateItem: (id: number, request: PackingItemRequest) => Promise<void>;
  togglePacked: (id: number) => Promise<void>;
  deleteItem: (id: number, tripId: number) => Promise<void>;
  fetchSummary: (tripId: number) => Promise<void>;
  applyTemplate: (tripId: number, climate: ClimateType) => Promise<void>;
  previewTemplate: (climate: ClimateType) => Promise<void>;

  fetchDocuments: (tripId: number) => Promise<void>;
  addDocument: (request: TravelDocumentRequest) => Promise<void>;
  updateDocument: (id: number, request: TravelDocumentRequest) => Promise<void>;
  deleteDocument: (id: number, tripId: number) => Promise<void>;
  fetchExpiringDocs: (tripId: number) => Promise<void>;
}

export const usePackingStore = create<PackingState>((set) => ({
  items: [],
  summary: null,
  documents: [],
  expiringDocs: [],
  templatePreview: [],
  isLoading: false,

  fetchItems: async (tripId) => {
    set({ isLoading: true });
    try { const items = await packingApi.getItems(tripId); set({ items }); } finally { set({ isLoading: false }); }
  },
  addItem: async (request) => {
    await packingApi.addItem(request);
    const items = await packingApi.getItems(request.tripId);
    const summary = await packingApi.getSummary(request.tripId);
    set({ items, summary });
  },
  updateItem: async (id, request) => {
    await packingApi.updateItem(id, request);
    const items = await packingApi.getItems(request.tripId);
    const summary = await packingApi.getSummary(request.tripId);
    set({ items, summary });
  },
  togglePacked: async (id) => {
    const updated = await packingApi.togglePacked(id);
    set(s => ({
      items: s.items.map(i => i.id === id ? updated : i),
      summary: s.summary ? {
        ...s.summary,
        packedItems: s.summary.packedItems + (updated.packed ? 1 : -1),
        packedPercent: s.summary.totalItems > 0
          ? ((s.summary.packedItems + (updated.packed ? 1 : -1)) / s.summary.totalItems) * 100
          : 0,
      } : null,
    }));
  },
  deleteItem: async (id, tripId) => {
    await packingApi.deleteItem(id);
    const items = await packingApi.getItems(tripId);
    const summary = await packingApi.getSummary(tripId);
    set({ items, summary });
  },
  fetchSummary: async (tripId) => {
    const summary = await packingApi.getSummary(tripId);
    set({ summary });
  },
  applyTemplate: async (tripId, climate) => {
    set({ isLoading: true });
    try {
      await packingApi.applyTemplate(tripId, climate);
      const items = await packingApi.getItems(tripId);
      const summary = await packingApi.getSummary(tripId);
      set({ items, summary });
    } finally { set({ isLoading: false }); }
  },
  previewTemplate: async (climate) => {
    const templatePreview = await packingApi.previewTemplate(climate);
    set({ templatePreview });
  },

  fetchDocuments: async (tripId) => {
    set({ isLoading: true });
    try { const documents = await packingApi.getDocuments(tripId); set({ documents }); } finally { set({ isLoading: false }); }
  },
  addDocument: async (request) => {
    await packingApi.addDocument(request);
    const documents = await packingApi.getDocuments(request.tripId);
    set({ documents });
  },
  updateDocument: async (id, request) => {
    await packingApi.updateDocument(id, request);
    const documents = await packingApi.getDocuments(request.tripId);
    set({ documents });
  },
  deleteDocument: async (id, tripId) => {
    await packingApi.deleteDocument(id);
    const documents = await packingApi.getDocuments(tripId);
    set({ documents });
  },
  fetchExpiringDocs: async (tripId) => {
    const expiringDocs = await packingApi.getExpiringByTrip(tripId);
    set({ expiringDocs });
  },
}));
