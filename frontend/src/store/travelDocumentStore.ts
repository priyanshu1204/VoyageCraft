import { create } from 'zustand';
import { travelDocApi } from '../api/travelDocumentApi';
import type { TravelDocumentData, TravelDocumentRequest, CountryLibraryEntry } from '../types/travelDocument';

interface TravelDocState {
  documents: TravelDocumentData[];
  countryLibrary: CountryLibraryEntry[];
  isLoading: boolean;

  fetchDocuments: (tripId: number) => Promise<void>;
  createDocument: (tripId: number, data: TravelDocumentRequest) => Promise<void>;
  updateDocument: (docId: number, data: TravelDocumentRequest) => Promise<void>;
  deleteDocument: (docId: number, tripId: number) => Promise<void>;
  toggleChecklist: (itemId: number, tripId: number) => Promise<void>;
  addChecklist: (docId: number, item: { itemName: string; itemType: string; description?: string; documentLink?: string }, tripId: number) => Promise<void>;
  deleteChecklist: (itemId: number, tripId: number) => Promise<void>;
  addReminder: (docId: number, reminder: { title: string; note?: string; reminderDate: string }, tripId: number) => Promise<void>;
  dismissReminder: (reminderId: number, tripId: number) => Promise<void>;
  deleteReminder: (reminderId: number, tripId: number) => Promise<void>;
  fetchCountryLibrary: () => Promise<void>;
}

export const useTravelDocStore = create<TravelDocState>((set) => ({
  documents: [],
  countryLibrary: [],
  isLoading: false,

  fetchDocuments: async (tripId) => {
    set({ isLoading: true });
    try {
      const documents = await travelDocApi.getTripDocs(tripId);
      set({ documents });
    } finally { set({ isLoading: false }); }
  },

  createDocument: async (tripId, data) => {
    await travelDocApi.create(tripId, data);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  updateDocument: async (docId, data) => {
    await travelDocApi.update(docId, data);
  },

  deleteDocument: async (docId, tripId) => {
    await travelDocApi.deleteDoc(docId);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  toggleChecklist: async (itemId, tripId) => {
    await travelDocApi.toggleChecklist(itemId);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  addChecklist: async (docId, item, tripId) => {
    await travelDocApi.addChecklist(docId, item);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  deleteChecklist: async (itemId, tripId) => {
    await travelDocApi.deleteChecklist(itemId);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  addReminder: async (docId, reminder, tripId) => {
    await travelDocApi.addReminder(docId, reminder);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  dismissReminder: async (reminderId, tripId) => {
    await travelDocApi.dismissReminder(reminderId);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  deleteReminder: async (reminderId, tripId) => {
    await travelDocApi.deleteReminder(reminderId);
    const documents = await travelDocApi.getTripDocs(tripId);
    set({ documents });
  },

  fetchCountryLibrary: async () => {
    try {
      const countryLibrary = await travelDocApi.getCountryLibrary();
      set({ countryLibrary });
    } catch { /* ignore */ }
  },
}));
