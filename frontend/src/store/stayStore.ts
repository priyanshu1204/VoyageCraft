import { create } from 'zustand';
import { stayApi } from '../api/stayApi';
import type { StayResponse, StayRequest } from '../types/stay';

interface StayState {
  stays: StayResponse[];
  isLoading: boolean;

  fetchStays: (tripId: number) => Promise<void>;
  addStay: (data: StayRequest) => Promise<void>;
  updateStay: (id: number, data: StayRequest) => Promise<void>;
  deleteStay: (id: number, tripId: number) => Promise<void>;
}

export const useStayStore = create<StayState>((set, get) => ({
  stays: [],
  isLoading: false,

  fetchStays: async (tripId) => {
    set({ isLoading: true });
    try {
      const res = await stayApi.getAll(tripId);
      set({ stays: res.data.data });
    } finally {
      set({ isLoading: false });
    }
  },

  addStay: async (data) => {
    await stayApi.add(data);
    await get().fetchStays(data.tripId);
  },

  updateStay: async (id, data) => {
    await stayApi.update(id, data);
    await get().fetchStays(data.tripId);
  },

  deleteStay: async (id, tripId) => {
    await stayApi.delete(id);
    await get().fetchStays(tripId);
  },
}));
