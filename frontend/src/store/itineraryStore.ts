import { create } from 'zustand';
import { itineraryApi } from '../api/itineraryApi';
import type {
  ItineraryResponse,
  ItinerarySummaryResponse,
  ItineraryCompareResponse,
  GenerateItineraryRequest,
} from '../types/itinerary';

interface ItineraryState {
  history: ItinerarySummaryResponse[];
  currentItinerary: ItineraryResponse | null;
  compareResult: ItineraryCompareResponse | null;
  isLoading: boolean;
  isGenerating: boolean;

  generateItinerary: (data: GenerateItineraryRequest) => Promise<ItineraryResponse>;
  fetchHistory: (tripId: number) => Promise<void>;
  fetchItinerary: (id: number) => Promise<void>;
  activateItinerary: (id: number) => Promise<void>;
  deleteItinerary: (id: number) => Promise<void>;
  compareVersions: (idA: number, idB: number) => Promise<void>;
  clearCompare: () => void;
  clearCurrent: () => void;
}

export const useItineraryStore = create<ItineraryState>((set, get) => ({
  history: [],
  currentItinerary: null,
  compareResult: null,
  isLoading: false,
  isGenerating: false,

  generateItinerary: async (data) => {
    set({ isGenerating: true });
    try {
      const res = await itineraryApi.generate(data);
      const generated = res.data.data;
      set({ currentItinerary: generated });
      // Refresh history
      await get().fetchHistory(data.tripId);
      return generated;
    } finally {
      set({ isGenerating: false });
    }
  },

  fetchHistory: async (tripId) => {
    set({ isLoading: true });
    try {
      const res = await itineraryApi.getHistory(tripId);
      set({ history: res.data.data });
    } finally {
      set({ isLoading: false });
    }
  },

  fetchItinerary: async (id) => {
    set({ isLoading: true });
    try {
      const res = await itineraryApi.getOne(id);
      set({ currentItinerary: res.data.data });
    } finally {
      set({ isLoading: false });
    }
  },

  activateItinerary: async (id) => {
    const res = await itineraryApi.activate(id);
    const updated = res.data.data;
    set((state) => ({
      currentItinerary: updated,
      history: state.history.map((h) => ({
        ...h,
        isActive: h.id === id,
        status: h.id === id ? 'ACTIVE' : 'ARCHIVED',
      })),
    }));
  },

  deleteItinerary: async (id) => {
    await itineraryApi.delete(id);
    set((state) => ({
      history: state.history.filter((h) => h.id !== id),
      currentItinerary: state.currentItinerary?.id === id ? null : state.currentItinerary,
    }));
  },

  compareVersions: async (idA, idB) => {
    set({ isLoading: true });
    try {
      const res = await itineraryApi.compare(idA, idB);
      set({ compareResult: res.data.data });
    } finally {
      set({ isLoading: false });
    }
  },

  clearCompare: () => set({ compareResult: null }),
  clearCurrent: () => set({ currentItinerary: null }),
}));
