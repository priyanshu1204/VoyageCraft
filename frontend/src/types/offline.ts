export interface SyncChange {
  entityType: string;
  entityId: number | null;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  payload: string;
  timestamp: string;
}

export interface SyncRequest {
  tripId: number;
  clientVersion: number;
  lastSyncedAt: string;
  changes: SyncChange[];
  reducedMode: boolean;
}

export interface SyncConflict {
  entityType: string;
  entityId: number;
  clientData: string;
  serverData: string;
  message: string;
}

export interface SyncResponse {
  serverVersion: number;
  syncedAt: string;
  snapshotData: string;
  conflicts: SyncConflict[];
  appliedChanges: number;
  rejectedChanges: number;
  syncStatusMessage: string;
}

export interface OfflineTripSnapshot {
  tripId: number;
  tripTitle: string;
  versionNumber: number;
  lastSyncedAt: string;
  reducedMode: boolean;
  dataSizeBytes: number;
}

export type ConnectionStatus = 'online' | 'offline' | 'syncing';
