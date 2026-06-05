import { create } from 'zustand';
import { transportApi } from '../api/transportApi';
import type {
  TransportResponse,
  TransportConflictResponse,
  MockTransportOption,
  TransportRequest,
  TransportType,
} from '../types/transport';

interface TransportState {
  segments: TransportResponse[];
  conflicts: TransportConflictResponse[];
  searchResults: MockTransportOption[];
  isLoading: boolean;
  isSearching: boolean;

  fetchSegments: (tripId: number) => Promise<void>;
  addSegment: (data: TransportRequest) => Promise<void>;
  updateSegment: (id: number, data: TransportRequest) => Promise<void>;
  deleteSegment: (id: number, tripId: number) => Promise<void>;
  detectConflicts: (tripId: number) => Promise<void>;
  searchMock: (params: { type: TransportType; from: string; to: string; date: string; fromTimezone?: string; toTimezone?: string }) => Promise<void>;
  clearSearch: () => void;
}

export const useTransportStore = create<TransportState>((set, get) => ({
  segments: [],
  conflicts: [],
  searchResults: [],
  isLoading: false,
  isSearching: false,

  fetchSegments: async (tripId) => {
    set({ isLoading: true });
    try {
      const res = await transportApi.getAll(tripId);
      set({ segments: res.data.data });
    } finally {
      set({ isLoading: false });
    }
  },

  addSegment: async (data) => {
    await transportApi.add(data);
    await get().fetchSegments(data.tripId);
    await get().detectConflicts(data.tripId);
  },

  updateSegment: async (id, data) => {
    await transportApi.update(id, data);
    await get().fetchSegments(data.tripId);
    await get().detectConflicts(data.tripId);
  },

  deleteSegment: async (id, tripId) => {
    await transportApi.delete(id);
    await get().fetchSegments(tripId);
    await get().detectConflicts(tripId);
  },

  detectConflicts: async (tripId) => {
    try {
      const res = await transportApi.detectConflicts(tripId);
      set({ conflicts: res.data.data });
    } catch {
      set({ conflicts: [] });
    }
  },

  searchMock: async (params) => {
    set({ isSearching: true });
    try {
      const res = await transportApi.searchMock(params);
      set({ searchResults: res.data.data });
    } finally {
      set({ isSearching: false });
    }
  },

  clearSearch: () => set({ searchResults: [] }),
}));
