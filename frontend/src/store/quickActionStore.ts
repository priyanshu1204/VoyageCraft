import { create } from 'zustand';
import { quickActionApi } from '../api/quickActionApi';
import type { QuickActionDashboard, QuickNote, QuickNoteRequest, ReorderRequest } from '../types/quickAction';

interface QuickActionState {
  dashboard: QuickActionDashboard | null;
  notes: QuickNote[];
  isLoading: boolean;
  error: string | null;

  fetchDashboard: (tripId: number) => Promise<void>;
  toggleTransportCheckIn: (id: number) => Promise<void>;
  toggleStayCheckIn: (id: number) => Promise<void>;
  reorderDayItems: (request: ReorderRequest) => Promise<void>;
  fetchNotes: (tripId: number) => Promise<void>;
  createNote: (request: QuickNoteRequest) => Promise<void>;
  updateNote: (id: number, request: QuickNoteRequest) => Promise<void>;
  deleteNote: (id: number) => Promise<void>;
}

export const useQuickActionStore = create<QuickActionState>((set, get) => ({
  dashboard: null,
  notes: [],
  isLoading: false,
  error: null,

  fetchDashboard: async (tripId) => {
    set({ isLoading: true, error: null });
    try {
      const data = await quickActionApi.getDashboard(tripId);
      set({ dashboard: data, notes: data.recentNotes || [], isLoading: false });
    } catch (e: any) {
      set({ error: e.response?.data?.message || 'Failed to load dashboard', isLoading: false });
    }
  },

  toggleTransportCheckIn: async (id) => {
    await quickActionApi.toggleTransportCheckIn(id);
    const d = get().dashboard;
    if (d) {
      set({
        dashboard: {
          ...d,
          upcomingCheckIns: d.upcomingCheckIns.map(c =>
            c.id === id && c.type === 'FLIGHT' ? { ...c, checkedIn: !c.checkedIn } : c
          ),
        },
      });
    }
  },

  toggleStayCheckIn: async (id) => {
    await quickActionApi.toggleStayCheckIn(id);
    const d = get().dashboard;
    if (d) {
      set({
        dashboard: {
          ...d,
          upcomingCheckIns: d.upcomingCheckIns.map(c =>
            c.id === id && c.type === 'STAY' ? { ...c, checkedIn: !c.checkedIn } : c
          ),
        },
      });
    }
  },

  reorderDayItems: async (request) => {
    await quickActionApi.reorderDayItems(request);
  },

  fetchNotes: async (tripId) => {
    const data = await quickActionApi.getNotes(tripId);
    set({ notes: data });
  },

  createNote: async (request) => {
    const note = await quickActionApi.createNote(request);
    set(s => ({ notes: [note, ...s.notes] }));
  },

  updateNote: async (id, request) => {
    const note = await quickActionApi.updateNote(id, request);
    set(s => ({ notes: s.notes.map(n => n.id === id ? note : n) }));
  },

  deleteNote: async (id) => {
    await quickActionApi.deleteNote(id);
    set(s => ({ notes: s.notes.filter(n => n.id !== id) }));
  },
}));
