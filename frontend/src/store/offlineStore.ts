import { create } from 'zustand';
import { offlineApi } from '../api/offlineApi';
import type { SyncChange, SyncResponse, OfflineTripSnapshot, ConnectionStatus } from '../types/offline';

interface OfflineState {
  isOnline: boolean;
  connectionStatus: ConnectionStatus;
  snapshots: OfflineTripSnapshot[];
  pendingChanges: SyncChange[];
  pendingConflicts: number;
  lastSyncResult: SyncResponse | null;
  reducedMode: boolean;
  isLoading: boolean;

  // Actions
  setOnlineStatus: (status: boolean) => void;
  setReducedMode: (enabled: boolean) => void;
  fetchSnapshots: () => Promise<void>;
  downloadTrip: (tripId: number) => Promise<void>;
  removeTrip: (tripId: number) => Promise<void>;
  addPendingChange: (change: SyncChange) => void;
  syncNow: (tripId: number) => Promise<SyncResponse>;
  resolveConflict: (syncLogId: number, resolution: 'ACCEPT_CLIENT' | 'ACCEPT_SERVER') => Promise<void>;
  fetchConflictCount: () => Promise<void>;
  clearPendingChanges: () => void;
}

// IndexedDB helper for offline storage
const DB_NAME = 'voyagecraft_offline';
const STORE_NAME = 'trip_snapshots';

function openDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'tripId' });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function saveToIndexedDB(tripId: number, data: string) {
  const db = await openDB();
  const tx = db.transaction(STORE_NAME, 'readwrite');
  tx.objectStore(STORE_NAME).put({ tripId, data, savedAt: new Date().toISOString() });
  return new Promise<void>((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

async function getFromIndexedDB(tripId: number): Promise<string | null> {
  const db = await openDB();
  const tx = db.transaction(STORE_NAME, 'readonly');
  const request = tx.objectStore(STORE_NAME).get(tripId);
  return new Promise((resolve, reject) => {
    request.onsuccess = () => resolve(request.result?.data || null);
    request.onerror = () => reject(request.error);
  });
}

async function removeFromIndexedDB(tripId: number) {
  const db = await openDB();
  const tx = db.transaction(STORE_NAME, 'readwrite');
  tx.objectStore(STORE_NAME).delete(tripId);
  return new Promise<void>((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

export const useOfflineStore = create<OfflineState>((set, get) => ({
  isOnline: navigator.onLine,
  connectionStatus: navigator.onLine ? 'online' : 'offline',
  snapshots: [],
  pendingChanges: [],
  pendingConflicts: 0,
  lastSyncResult: null,
  reducedMode: false,
  isLoading: false,

  setOnlineStatus: (status) => {
    set({
      isOnline: status,
      connectionStatus: status ? 'online' : 'offline',
    });
  },

  setReducedMode: (enabled) => set({ reducedMode: enabled }),

  fetchSnapshots: async () => {
    set({ isLoading: true });
    try {
      const snapshots = await offlineApi.getSnapshots();
      set({ snapshots });
    } catch {
      // If offline, load from local data
    } finally {
      set({ isLoading: false });
    }
  },

  downloadTrip: async (tripId) => {
    set({ isLoading: true, connectionStatus: 'syncing' });
    try {
      const result = await offlineApi.generateSnapshot(tripId, get().reducedMode);
      // Save to IndexedDB for true offline access
      await saveToIndexedDB(tripId, result.snapshotData);
      set({ lastSyncResult: result, connectionStatus: 'online' });
      // Refresh snapshot list
      await get().fetchSnapshots();
    } finally {
      set({ isLoading: false, connectionStatus: get().isOnline ? 'online' : 'offline' });
    }
  },

  removeTrip: async (tripId) => {
    try {
      await removeFromIndexedDB(tripId);
      await offlineApi.removeCache(tripId);
      await get().fetchSnapshots();
    } catch { /* ignore if offline */ }
  },

  addPendingChange: (change) => {
    set(s => ({ pendingChanges: [...s.pendingChanges, change] }));
  },

  syncNow: async (tripId) => {
    const { pendingChanges, reducedMode } = get();
    set({ connectionStatus: 'syncing', isLoading: true });

    try {
      const cachedData = await getFromIndexedDB(tripId);
      const clientVersion = cachedData ? JSON.parse(cachedData).versionNumber || 0 : 0;

      const result = await offlineApi.syncChanges({
        tripId,
        clientVersion,
        lastSyncedAt: new Date().toISOString(),
        changes: pendingChanges.filter(c => c.entityType !== ''),
        reducedMode,
      });

      // Update local cache with fresh data
      await saveToIndexedDB(tripId, result.snapshotData);

      set({
        lastSyncResult: result,
        pendingChanges: [],
        pendingConflicts: result.conflicts.length,
        connectionStatus: 'online',
      });

      return result;
    } finally {
      set({ isLoading: false, connectionStatus: get().isOnline ? 'online' : 'offline' });
    }
  },

  resolveConflict: async (syncLogId, resolution) => {
    await offlineApi.resolveConflict(syncLogId, resolution);
    await get().fetchConflictCount();
  },

  fetchConflictCount: async () => {
    try {
      const data = await offlineApi.getConflictCount();
      set({ pendingConflicts: data.pendingConflicts });
    } catch { /* offline */ }
  },

  clearPendingChanges: () => set({ pendingChanges: [] }),
}));

// Auto-detect online/offline status
if (typeof window !== 'undefined') {
  window.addEventListener('online', () => {
    useOfflineStore.getState().setOnlineStatus(true);
  });
  window.addEventListener('offline', () => {
    useOfflineStore.getState().setOnlineStatus(false);
  });
}
