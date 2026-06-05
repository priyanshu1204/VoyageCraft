import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useOfflineStore } from '../store/offlineStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, Wifi, WifiOff, Download, Trash2, RefreshCw, AlertTriangle,
  CheckCircle, Clock, HardDrive, Zap, Shield, Database
} from 'lucide-react';

export default function OfflinePage() {
  const navigate = useNavigate();
  const {
    isOnline, connectionStatus, snapshots, pendingChanges, pendingConflicts,
    lastSyncResult, reducedMode, isLoading,
    fetchSnapshots, downloadTrip, removeTrip, syncNow, setReducedMode,
    fetchConflictCount,
  } = useOfflineStore();

  const [syncingTripId, setSyncingTripId] = useState<number | null>(null);

  useEffect(() => {
    fetchSnapshots();
    fetchConflictCount();
  }, []);

  const handleDownload = async (tripId: number) => {
    try {
      await downloadTrip(tripId);
      toast.success('Trip downloaded for offline use!');
    } catch {
      toast.error('Failed to download trip');
    }
  };

  const handleSync = async (tripId: number) => {
    setSyncingTripId(tripId);
    try {
      const result = await syncNow(tripId);
      if (result.conflicts.length > 0) {
        toast.error(`${result.conflicts.length} conflict(s) detected!`);
      } else {
        toast.success(`Synced! ${result.appliedChanges} change(s) applied.`);
      }
    } catch {
      toast.error('Sync failed. Will retry when online.');
    } finally {
      setSyncingTripId(null);
    }
  };

  const handleRemove = async (tripId: number) => {
    try {
      await removeTrip(tripId);
      toast.success('Offline data removed');
    } catch {
      toast.error('Failed to remove');
    }
  };

  const formatBytes = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  const formatDate = (dateStr: string): string => {
    return new Date(dateStr).toLocaleString('en-IN', {
      day: 'numeric', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  };

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate('/dashboard')} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Dashboard
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 10 }}>
          <Database size={24} style={{ color: 'var(--accent-primary)' }} /> Offline Mode
        </h2>
        <div className={`sync-indicator ${isOnline ? 'sync-status-online' : 'sync-status-offline'}`}
          style={{ padding: '8px 16px', borderRadius: 20, fontSize: 13, fontWeight: 600 }}>
          {isOnline ? <Wifi size={16} /> : <WifiOff size={16} />}
          {isOnline ? 'Connected' : 'Offline'}
        </div>
      </div>

      {/* ── Settings Card ─────────────────────────── */}
      <div className="card" style={{ marginBottom: 20, padding: '16px 20px' }}>
        <h3 style={{ fontSize: 15, fontWeight: 600, marginBottom: 12, display: 'flex', alignItems: 'center', gap: 8 }}>
          <Zap size={16} style={{ color: '#f59e0b' }} /> Data Usage Settings
        </h3>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: 14, fontWeight: 500 }}>Reduced Data Mode</div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
              Download smaller snapshots without detailed activity and transport data
            </div>
          </div>
          <button
            className={`btn btn-sm ${reducedMode ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setReducedMode(!reducedMode)}
          >
            {reducedMode ? 'ON' : 'OFF'}
          </button>
        </div>
      </div>

      {/* ── Sync Status Summary ────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12, marginBottom: 20 }}>
        <div className="card offline-stat-card">
          <div className="offline-stat-icon" style={{ background: 'rgba(34, 197, 94, 0.1)', color: '#22c55e' }}>
            <HardDrive size={20} />
          </div>
          <div className="offline-stat-value">{snapshots.length}</div>
          <div className="offline-stat-label">Cached Trips</div>
        </div>
        <div className="card offline-stat-card">
          <div className="offline-stat-icon" style={{ background: 'rgba(99, 102, 241, 0.1)', color: '#6366f1' }}>
            <RefreshCw size={20} />
          </div>
          <div className="offline-stat-value">{pendingChanges.length}</div>
          <div className="offline-stat-label">Pending Changes</div>
        </div>
        <div className="card offline-stat-card">
          <div className="offline-stat-icon" style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }}>
            <AlertTriangle size={20} />
          </div>
          <div className="offline-stat-value">{pendingConflicts}</div>
          <div className="offline-stat-label">Conflicts</div>
        </div>
      </div>

      {/* ── Last Sync Result ──────────────────────── */}
      {lastSyncResult && (
        <div className={`card ${lastSyncResult.conflicts.length > 0 ? 'doc-status-warning' : 'doc-status-valid'}`}
          style={{ marginBottom: 20, padding: '14px 18px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
            {lastSyncResult.conflicts.length > 0
              ? <AlertTriangle size={16} style={{ color: '#f59e0b' }} />
              : <CheckCircle size={16} style={{ color: '#22c55e' }} />}
            <span style={{ fontWeight: 600, fontSize: 14 }}>{lastSyncResult.syncStatusMessage}</span>
          </div>
          <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            Applied: {lastSyncResult.appliedChanges} | Rejected: {lastSyncResult.rejectedChanges} |
            Version: v{lastSyncResult.serverVersion} | Synced: {formatDate(lastSyncResult.syncedAt)}
          </div>

          {/* ── Conflict Resolution ──────────────────── */}
          {lastSyncResult.conflicts.length > 0 && (
            <div style={{ marginTop: 12 }}>
              <h4 style={{ fontSize: 13, fontWeight: 600, marginBottom: 8, color: '#f59e0b' }}>
                Resolve Conflicts:
              </h4>
              {lastSyncResult.conflicts.map((c, i) => (
                <div key={i} className="card" style={{ padding: '10px 14px', marginBottom: 8, background: 'var(--bg-input)' }}>
                  <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 4 }}>
                    {c.entityType} #{c.entityId}
                  </div>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 8 }}>{c.message}</div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-primary btn-sm" style={{ fontSize: 11 }}>
                      <Shield size={12} /> Keep My Changes
                    </button>
                    <button className="btn btn-secondary btn-sm" style={{ fontSize: 11 }}>
                      <Download size={12} /> Use Server Version
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ── Cached Trips List ─────────────────────── */}
      <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>
        Downloaded Trips ({snapshots.length})
      </h3>

      {snapshots.length === 0 ? (
        <div className="empty-state">
          <Download size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
          <h3>No trips cached yet</h3>
          <p>Go to a trip and click "Save for Offline" to cache it</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {snapshots.map(snap => (
            <div key={snap.tripId} className="card offline-trip-card">
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                  <span style={{ fontWeight: 600, fontSize: 15 }}>{snap.tripTitle}</span>
                  {snap.reducedMode && (
                    <span className="template-badge">Reduced</span>
                  )}
                  <span className="offline-version-badge">v{snap.versionNumber}</span>
                </div>
                <div style={{ display: 'flex', gap: 16, fontSize: 12, color: 'var(--text-muted)' }}>
                  <span><Clock size={11} /> Last synced: {formatDate(snap.lastSyncedAt)}</span>
                  <span><HardDrive size={11} /> Size: {formatBytes(snap.dataSizeBytes)}</span>
                </div>
              </div>
              <div style={{ display: 'flex', gap: 6 }}>
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => handleSync(snap.tripId)}
                  disabled={!isOnline || syncingTripId === snap.tripId}
                >
                  {syncingTripId === snap.tripId
                    ? <><RefreshCw size={12} className="sync-spin" /> Syncing</>
                    : <><RefreshCw size={12} /> Sync</>}
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => handleDownload(snap.tripId)}
                  disabled={!isOnline}
                >
                  <Download size={12} /> Refresh
                </button>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => handleRemove(snap.tripId)}
                  style={{ padding: 4 }}
                >
                  <Trash2 size={14} style={{ color: 'var(--danger)' }} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
