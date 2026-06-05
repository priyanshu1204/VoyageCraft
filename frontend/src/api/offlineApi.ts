import api from './axios';
import type { SyncRequest, SyncResponse, OfflineTripSnapshot } from '../types/offline';

export const offlineApi = {
  // Generate a full snapshot for offline use
  generateSnapshot: (tripId: number, reducedMode = false) =>
    api.post<{ data: SyncResponse }>(`/offline/snapshot/${tripId}?reducedMode=${reducedMode}`).then(r => r.data.data),

  // Sync offline changes back to server
  syncChanges: (request: SyncRequest) =>
    api.post<{ data: SyncResponse }>('/offline/sync', request).then(r => r.data.data),

  // List all cached snapshots
  getSnapshots: () =>
    api.get<{ data: OfflineTripSnapshot[] }>('/offline/snapshots').then(r => r.data.data),

  // Get pending conflict count
  getConflictCount: () =>
    api.get<{ data: { pendingConflicts: number } }>('/offline/conflicts/count').then(r => r.data.data),

  // Resolve a conflict
  resolveConflict: (syncLogId: number, resolution: 'ACCEPT_CLIENT' | 'ACCEPT_SERVER') =>
    api.put(`/offline/conflicts/${syncLogId}/resolve?resolution=${resolution}`),

  // Remove offline cache
  removeCache: (tripId: number) =>
    api.delete(`/offline/cache/${tripId}`),
};
