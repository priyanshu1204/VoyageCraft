import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotificationStore } from '../store/notificationStore';
import toast from 'react-hot-toast';
import {
  Bell, BellOff, CheckCheck, Trash2, Settings, ArrowLeft, Eye, Clock, Wallet,
  Users, Sun, Plane, Building2, Moon, Zap, ChevronDown, ChevronUp
} from 'lucide-react';

const TYPE_LABELS: Record<string, { label: string; icon: string; color: string }> = {
  DEPARTURE_REMINDER: { label: 'Departure', icon: '✈️', color: '#6366f1' },
  CHECKIN_REMINDER: { label: 'Check-in', icon: '🏨', color: '#22c55e' },
  COLLABORATOR_CHANGE: { label: 'Collaborator', icon: '👥', color: '#3b82f6' },
  BUDGET_THRESHOLD: { label: 'Budget', icon: '💰', color: '#f59e0b' },
  WEATHER_ALERT: { label: 'Weather', icon: '🌤️', color: '#06b6d4' },
  DOCUMENT_REMINDER: { label: 'Document', icon: '📄', color: '#8b5cf6' },
  ITINERARY_UPDATE: { label: 'Itinerary', icon: '📋', color: '#10b981' },
  GENERAL: { label: 'General', icon: 'ℹ️', color: '#6b7280' },
};

const SEVERITY_COLORS: Record<string, string> = {
  LOW: '#3b82f6', MEDIUM: '#f59e0b', HIGH: '#f97316', CRITICAL: '#ef4444',
};

export default function NotificationsPage() {
  const navigate = useNavigate();
  const { notifications, unreadCount, preferences, isLoading,
    fetchNotifications, fetchUnreadCount, markAsRead, markAllAsRead, dismiss, fetchPreferences, updatePreferences } = useNotificationStore();

  const [activeTab, setActiveTab] = useState<'alerts' | 'settings'>('alerts');
  const [filterType, setFilterType] = useState<string>('ALL');
  const [showPrefs, setShowPrefs] = useState(false);

  useEffect(() => { fetchNotifications(); fetchUnreadCount(); fetchPreferences(); }, []);

  const filtered = filterType === 'ALL' ? notifications : notifications.filter(n => n.notificationType === filterType);

  const handleTogglePref = async (key: string, value: boolean) => {
    try {
      await updatePreferences({ [key]: value } as any);
      toast.success('Preference updated');
    } catch { toast.error('Failed'); }
  };

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(-1)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, flexWrap: 'wrap', gap: 10 }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 10 }}>
          <Bell size={24} style={{ color: '#6366f1' }} /> Notifications
          {unreadCount > 0 && (
            <span style={{ fontSize: 12, background: '#ef4444', color: '#fff', borderRadius: 20, padding: '2px 10px', fontWeight: 700 }}>{unreadCount}</span>
          )}
        </h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-secondary btn-sm" onClick={async () => { await markAllAsRead(); toast.success('All marked as read'); }}>
            <CheckCheck size={14} /> Mark All Read
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'var(--bg-card)', padding: 4, borderRadius: 12 }}>
        {[
          { key: 'alerts' as const, label: `Alerts (${notifications.length})`, icon: <Bell size={14} /> },
          { key: 'settings' as const, label: 'Preferences', icon: <Settings size={14} /> },
        ].map(tab => (
          <button key={tab.key}
            className={`btn btn-sm ${activeTab === tab.key ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setActiveTab(tab.key)}
            style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, borderRadius: 10 }}>
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* ALERTS TAB */}
      {activeTab === 'alerts' && (
        <>
          {/* Type filter */}
          <div style={{ display: 'flex', gap: 6, marginBottom: 16, overflowX: 'auto', paddingBottom: 4 }}>
            <button className={`btn btn-sm ${filterType === 'ALL' ? 'btn-primary' : 'btn-ghost'}`}
              onClick={() => setFilterType('ALL')} style={{ borderRadius: 20 }}>All</button>
            {Object.entries(TYPE_LABELS).map(([key, val]) => (
              <button key={key} className={`btn btn-sm ${filterType === key ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setFilterType(key)} style={{ borderRadius: 20, whiteSpace: 'nowrap' }}>
                {val.icon} {val.label}
              </button>
            ))}
          </div>

          {filtered.length === 0 ? (
            <div className="empty-state">
              <BellOff size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
              <h3>No notifications</h3>
              <p>Go to a trip and click "Generate Alerts" to check for upcoming reminders</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {filtered.map(n => {
                const typeInfo = TYPE_LABELS[n.notificationType] || TYPE_LABELS.GENERAL;
                return (
                  <div key={n.id} className="card notif-card" style={{
                    padding: '14px 20px',
                    borderLeft: `4px solid ${SEVERITY_COLORS[n.severity] || '#6b7280'}`,
                    opacity: n.readStatus ? 0.7 : 1,
                    background: n.readStatus ? 'var(--bg-card)' : 'var(--bg-card-hover)',
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                          <span style={{ fontSize: 16 }}>{n.severityIcon}</span>
                          <span style={{ fontSize: 14, fontWeight: 700 }}>{n.title}</span>
                          <span style={{ fontSize: 10, padding: '1px 8px', borderRadius: 12,
                            background: `${typeInfo.color}20`, color: typeInfo.color, fontWeight: 600 }}>
                            {typeInfo.label}
                          </span>
                          {!n.readStatus && <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#6366f1' }} />}
                        </div>
                        <div style={{ fontSize: 13, color: 'var(--text-secondary)', marginBottom: 4 }}>{n.message}</div>
                        <div style={{ display: 'flex', gap: 12, fontSize: 11, color: 'var(--text-muted)' }}>
                          {n.tripTitle && <span>📍 {n.tripTitle}</span>}
                          <span>🕐 {new Date(n.createdAt).toLocaleString()}</span>
                          <span style={{ padding: '1px 6px', borderRadius: 6,
                            background: `${SEVERITY_COLORS[n.severity]}15`, color: SEVERITY_COLORS[n.severity], fontWeight: 600 }}>
                            {n.severity}
                          </span>
                        </div>
                      </div>
                      <div style={{ display: 'flex', gap: 4, marginLeft: 12 }}>
                        {!n.readStatus && (
                          <button className="btn btn-ghost btn-sm" onClick={async () => { await markAsRead(n.id); toast.success('Marked read'); }}
                            title="Mark as read"><Eye size={14} /></button>
                        )}
                        {n.actionUrl && (
                          <button className="btn btn-ghost btn-sm" onClick={() => navigate(n.actionUrl)}
                            title="Go to"><Zap size={14} style={{ color: '#6366f1' }} /></button>
                        )}
                        <button className="btn btn-ghost btn-sm" onClick={async () => { await dismiss(n.id); toast.success('Dismissed'); }}
                          title="Dismiss"><Trash2 size={14} style={{ color: 'var(--danger)' }} /></button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}

      {/* SETTINGS TAB */}
      {activeTab === 'settings' && preferences && (
        <div className="card" style={{ padding: 24 }}>
          <h3 style={{ fontSize: 18, fontWeight: 700, marginBottom: 20 }}>🔔 Notification Preferences</h3>

          {/* Alert Toggles */}
          <div style={{ marginBottom: 24 }}>
            <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12, color: 'var(--text-secondary)' }}>Alert Types</h4>
            {[
              { key: 'departureReminders', label: 'Departure Reminders', desc: 'Get reminded before your trip departure', icon: <Plane size={16} /> },
              { key: 'checkinReminders', label: 'Check-in Reminders', desc: 'Reminders to check in to your accommodation', icon: <Building2 size={16} /> },
              { key: 'collaboratorAlerts', label: 'Collaborator Changes', desc: 'When collaborators join, leave, or make changes', icon: <Users size={16} /> },
              { key: 'budgetAlerts', label: 'Budget Alerts', desc: 'When spending approaches the threshold', icon: <Wallet size={16} /> },
              { key: 'weatherAlerts', label: 'Weather Alerts', desc: 'Severe weather warnings for your destinations', icon: <Sun size={16} /> },
            ].map(pref => (
              <div key={pref.key} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '12px 16px', borderRadius: 10, background: 'var(--bg-input)', marginBottom: 6
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ color: '#6366f1' }}>{pref.icon}</span>
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 600 }}>{pref.label}</div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{pref.desc}</div>
                  </div>
                </div>
                <label style={{ position: 'relative', display: 'inline-block', width: 44, height: 24, cursor: 'pointer' }}>
                  <input type="checkbox" checked={(preferences as any)[pref.key]}
                    onChange={e => handleTogglePref(pref.key, e.target.checked)}
                    style={{ opacity: 0, width: 0, height: 0 }} />
                  <span style={{
                    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                    background: (preferences as any)[pref.key] ? '#6366f1' : '#374151',
                    borderRadius: 24, transition: '0.3s',
                  }}>
                    <span style={{
                      position: 'absolute', height: 18, width: 18, left: (preferences as any)[pref.key] ? 22 : 3, bottom: 3,
                      background: 'white', borderRadius: '50%', transition: '0.3s',
                    }} />
                  </span>
                </label>
              </div>
            ))}
          </div>

          {/* Budget Threshold */}
          <div style={{ marginBottom: 24 }}>
            <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12, color: 'var(--text-secondary)' }}>Budget Threshold</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 16px', borderRadius: 10, background: 'var(--bg-input)' }}>
              <Wallet size={16} style={{ color: '#f59e0b' }} />
              <span style={{ fontSize: 13, flex: 1 }}>Alert when spending reaches</span>
              <select className="input" value={preferences.budgetThresholdPercent} style={{ width: 80 }}
                onChange={e => handleTogglePref('budgetThresholdPercent', parseInt(e.target.value) as any)}>
                {[50, 60, 70, 75, 80, 85, 90, 95, 100].map(v => <option key={v} value={v}>{v}%</option>)}
              </select>
            </div>
          </div>

          {/* Quiet Hours */}
          <div>
            <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12, color: 'var(--text-secondary)' }}>Quiet Hours</h4>
            <div style={{ padding: '12px 16px', borderRadius: 10, background: 'var(--bg-input)', marginBottom: 6 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <Moon size={16} style={{ color: '#8b5cf6' }} />
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 600 }}>Enable Quiet Hours</div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>No notifications during these hours</div>
                  </div>
                </div>
                <label style={{ position: 'relative', display: 'inline-block', width: 44, height: 24, cursor: 'pointer' }}>
                  <input type="checkbox" checked={preferences.quietHoursEnabled}
                    onChange={e => handleTogglePref('quietHoursEnabled', e.target.checked)}
                    style={{ opacity: 0, width: 0, height: 0 }} />
                  <span style={{
                    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                    background: preferences.quietHoursEnabled ? '#8b5cf6' : '#374151',
                    borderRadius: 24, transition: '0.3s',
                  }}>
                    <span style={{
                      position: 'absolute', height: 18, width: 18, left: preferences.quietHoursEnabled ? 22 : 3, bottom: 3,
                      background: 'white', borderRadius: '50%', transition: '0.3s',
                    }} />
                  </span>
                </label>
              </div>
              {preferences.quietHoursEnabled && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginTop: 8 }}>
                  <div>
                    <label style={{ fontSize: 11, fontWeight: 600, display: 'block', marginBottom: 4 }}>Start Time</label>
                    <input className="input" type="time" value={preferences.quietHoursStart || '22:00'}
                      onChange={e => handleTogglePref('quietHoursStart', e.target.value as any)} />
                  </div>
                  <div>
                    <label style={{ fontSize: 11, fontWeight: 600, display: 'block', marginBottom: 4 }}>End Time</label>
                    <input className="input" type="time" value={preferences.quietHoursEnd || '07:00'}
                      onChange={e => handleTogglePref('quietHoursEnd', e.target.value as any)} />
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
