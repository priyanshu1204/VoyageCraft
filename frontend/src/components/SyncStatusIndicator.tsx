import { useOfflineStore } from '../store/offlineStore';
import { Wifi, WifiOff, RefreshCw, AlertTriangle } from 'lucide-react';

export default function SyncStatusIndicator() {
  const { connectionStatus, pendingChanges, pendingConflicts } = useOfflineStore();

  const statusConfig = {
    online: { icon: <Wifi size={14} />, label: 'Online', className: 'sync-status-online' },
    offline: { icon: <WifiOff size={14} />, label: 'Offline', className: 'sync-status-offline' },
    syncing: { icon: <RefreshCw size={14} className="sync-spin" />, label: 'Syncing...', className: 'sync-status-syncing' },
  };

  const config = statusConfig[connectionStatus];

  return (
    <div className="sync-status-bar">
      <div className={`sync-indicator ${config.className}`}>
        {config.icon}
        <span>{config.label}</span>
      </div>

      {pendingChanges.length > 0 && (
        <div className="sync-badge sync-badge-pending">
          <RefreshCw size={10} /> {pendingChanges.length} pending
        </div>
      )}

      {pendingConflicts > 0 && (
        <div className="sync-badge sync-badge-conflict">
          <AlertTriangle size={10} /> {pendingConflicts} conflict{pendingConflicts > 1 ? 's' : ''}
        </div>
      )}
    </div>
  );
}
