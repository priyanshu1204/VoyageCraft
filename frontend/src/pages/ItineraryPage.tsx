import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useItineraryStore } from '../store/itineraryStore';
import { useTripStore } from '../store/tripStore';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import {
  ArrowLeft, Zap, Clock, MapPin, Calendar, Sparkles, CheckCircle2,
  Trash2, GitCompare, ChevronDown, ChevronUp, Activity, Star,
} from 'lucide-react';
import type { ItinerarySummaryResponse, ItineraryDayResponse, ItineraryItemResponse } from '../types/itinerary';

const PACE_OPTIONS = [
  { value: 'RELAXED', icon: '🌿', label: 'Relaxed', desc: '3 activities/day · Start at 9 AM', color: '#22c55e' },
  { value: 'STANDARD', icon: '⚡', label: 'Standard', desc: '5 activities/day · Start at 8 AM', color: '#6366f1' },
  { value: 'INTENSE', icon: '🔥', label: 'Intense', desc: '7 activities/day · Start at 7 AM', color: '#ef4444' },
] as const;

const CATEGORY_ICONS: Record<string, string> = {
  ATTRACTION: '🏛️', FOOD: '🍜', SHOPPING: '🛍️', NATURE: '🌿',
  ADVENTURE: '🧗', LEISURE: '☀️', CULTURAL: '🎭', NIGHTLIFE: '🌙', TRANSPORT: '🚌',
};

function formatTime(t?: string) {
  if (!t) return '';
  // backend sends "HH:MM:SS"
  const parts = t.split(':');
  if (parts.length < 2) return t;
  const h = parseInt(parts[0]);
  const m = parts[1];
  const ampm = h >= 12 ? 'PM' : 'AM';
  return `${h % 12 || 12}:${m} ${ampm}`;
}

// ── Activity Item ──────────────────────────────────────────────────────────────
function ActivityCard({ item, index }: { item: ItineraryItemResponse; index: number }) {
  const [open, setOpen] = useState(false);
  return (
    <div className={`activity-item cat-${item.category}`}>
      <div className="activity-time">
        <div className="time-start">{formatTime(item.startTime)}</div>
        <div className="time-end">{formatTime(item.endTime)}</div>
        <div style={{ fontSize: 10, color: 'var(--text-muted)', marginTop: 4 }}>
          {item.durationMinutes}m
        </div>
      </div>
      <div className="activity-timeline-dot" />
      <div className="activity-content">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div className="act-title">
            <span style={{ marginRight: 6 }}>{CATEGORY_ICONS[item.category] || '📍'}</span>
            {item.title}
          </div>
          <button
            onClick={() => setOpen(o => !o)}
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', padding: '0 4px' }}
          >
            {open ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
          </button>
        </div>
        {open && (
          <div>
            {item.description && <div className="act-desc">{item.description}</div>}
            <div className="activity-meta">
              {item.openingHours && <span className="act-tag">🕐 {item.openingHours}</span>}
              {item.bestSeason && <span className="act-tag season">🌸 {item.bestSeason}</span>}
              {item.costEstimate != null && (
                <span className="act-tag cost">
                  {item.costEstimate === 0 ? '🆓 Free' : `💰 $${item.costEstimate}`}
                </span>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// ── Day Card ───────────────────────────────────────────────────────────────────
function DayCard({ day }: { day: ItineraryDayResponse }) {
  return (
    <div className="day-card animate-in">
      <div className="day-header">
        <div className="day-number">{day.dayIndex}</div>
        <div className="day-info">
          <div className="day-date">{format(new Date(day.dayDate + 'T00:00:00'), 'EEEE, MMMM d')}</div>
          <div className="day-theme">{day.theme}</div>
          {day.destinationName && <div className="day-dest">📍 {day.destinationName}</div>}
        </div>
        <div style={{ marginLeft: 'auto', fontSize: 13, color: 'var(--text-muted)' }}>
          {day.items.length} activities
        </div>
      </div>
      <div className="day-body">
        <div className="activities-list">
          {day.items.map((item, i) => <ActivityCard key={item.id} item={item} index={i} />)}
        </div>
      </div>
    </div>
  );
}

// ── Version Card ───────────────────────────────────────────────────────────────
function VersionCard({
  v, isSelected, onView, onActivate, onDelete, onSelect, compareMode, compareSelected,
}: {
  v: ItinerarySummaryResponse;
  isSelected: boolean;
  onView: () => void;
  onActivate: () => void;
  onDelete: () => void;
  onSelect: () => void;
  compareMode: boolean;
  compareSelected: boolean;
}) {
  const pace = PACE_OPTIONS.find(p => p.value === v.pace);
  const cls = [
    'version-card',
    v.isActive ? 'active-version' : '',
    isSelected ? 'selected-version' : '',
    compareMode && compareSelected ? 'selected-version' : '',
  ].filter(Boolean).join(' ');

  return (
    <div className={cls} onClick={compareMode ? onSelect : onView}>
      <div className="version-num">v{v.versionNumber}</div>
      <div className="version-info">
        <div className="v-name">
          {v.versionName}
          {v.isActive && (
            <span style={{ marginLeft: 8, fontSize: 11, color: 'var(--success)', fontWeight: 700 }}>
              ✓ ACTIVE
            </span>
          )}
          {compareMode && compareSelected && (
            <span style={{ marginLeft: 8, fontSize: 11, color: 'var(--accent-primary)', fontWeight: 700 }}>
              ● SELECTED
            </span>
          )}
        </div>
        <div className="v-meta">
          {pace?.icon} {pace?.label} · {v.totalDays} days · {v.totalActivities} activities ·{' '}
          {format(new Date(v.createdAt), 'MMM d, yyyy')}
        </div>
      </div>
      {!compareMode && (
        <div className="version-actions" onClick={e => e.stopPropagation()}>
          {!v.isActive && (
            <button className="btn btn-secondary btn-sm" onClick={onActivate} title="Set as Active">
              <CheckCircle2 size={13} /> Activate
            </button>
          )}
          {!v.isActive && (
            <button className="btn btn-ghost btn-sm" onClick={onDelete} title="Delete version"
              style={{ color: 'var(--danger)' }}>
              <Trash2 size={13} />
            </button>
          )}
        </div>
      )}
    </div>
  );
}

// ── Compare View ───────────────────────────────────────────────────────────────
function CompareView() {
  const { compareResult, clearCompare } = useItineraryStore();
  if (!compareResult) return null;
  const { versionA, versionB, daysA, daysB, paceComparison, activityDifference } = compareResult;
  const paceA = PACE_OPTIONS.find(p => p.value === versionA.pace);
  const paceB = PACE_OPTIONS.find(p => p.value === versionB.pace);

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <h3 style={{ fontSize: 18, fontWeight: 700 }}>Version Comparison</h3>
          <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
            Pace: {paceComparison} · Activity difference: {activityDifference > 0 ? '+' : ''}{activityDifference}
          </div>
        </div>
        <button className="btn btn-ghost btn-sm" onClick={clearCompare}>✕ Close</button>
      </div>

      <div className="compare-grid">
        {/* Version A */}
        <div className="compare-col compare-version-a">
          <div className="compare-col-header">
            <div style={{ fontWeight: 700 }}>v{versionA.versionNumber} — {versionA.versionName}</div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
              {paceA?.icon} {paceA?.label} · {versionA.totalActivities} activities
            </div>
          </div>
          <div style={{ border: '1px solid rgba(99,102,241,0.3)', borderTop: 'none', borderRadius: '0 0 12px 12px', padding: 12, background: 'var(--bg-card)' }}>
            {daysA.map(d => <DayCard key={d.id} day={d} />)}
          </div>
        </div>

        {/* Version B */}
        <div className="compare-col compare-version-b">
          <div className="compare-col-header">
            <div style={{ fontWeight: 700 }}>v{versionB.versionNumber} — {versionB.versionName}</div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
              {paceB?.icon} {paceB?.label} · {versionB.totalActivities} activities
            </div>
          </div>
          <div style={{ border: '1px solid rgba(139,92,246,0.3)', borderTop: 'none', borderRadius: '0 0 12px 12px', padding: 12, background: 'var(--bg-card)' }}>
            {daysB.map(d => <DayCard key={d.id} day={d} />)}
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Main Page ──────────────────────────────────────────────────────────────────
export default function ItineraryPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();

  const {
    history, currentItinerary, compareResult,
    generateItinerary, fetchHistory, fetchItinerary,
    activateItinerary, deleteItinerary, compareVersions, clearCompare,
    isLoading, isGenerating,
  } = useItineraryStore();

  const { currentTrip, fetchTrip } = useTripStore();

  const [tab, setTab] = useState<'generate' | 'history' | 'view' | 'compare'>('generate');
  const [pace, setPace] = useState<'RELAXED' | 'STANDARD' | 'INTENSE'>('STANDARD');
  const [versionName, setVersionName] = useState('');
  const [compareIds, setCompareIds] = useState<number[]>([]);

  useEffect(() => {
    fetchHistory(tripId);
    if (!currentTrip || currentTrip.id !== tripId) fetchTrip(tripId);
  }, [tripId]);

  const handleGenerate = async () => {
    try {
      await generateItinerary({ tripId, pace, versionName: versionName || undefined });
      toast.success('✨ Itinerary generated successfully!');
      setVersionName('');
      setTab('view');
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Generation failed');
    }
  };

  const handleView = async (itId: number) => {
    await fetchItinerary(itId);
    setTab('view');
  };

  const handleActivate = async (itId: number) => {
    try {
      await activateItinerary(itId);
      toast.success('Itinerary activated!');
    } catch { toast.error('Failed to activate'); }
  };

  const handleDelete = async (itId: number) => {
    if (!confirm('Delete this itinerary version?')) return;
    try {
      await deleteItinerary(itId);
      toast.success('Version deleted');
      if (currentItinerary?.id === itId) setTab('history');
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Cannot delete active version');
    }
  };

  const handleSelectForCompare = (itId: number) => {
    setCompareIds(prev => {
      if (prev.includes(itId)) return prev.filter(x => x !== itId);
      if (prev.length >= 2) return [prev[1], itId];
      return [...prev, itId];
    });
  };

  const handleCompare = async () => {
    if (compareIds.length < 2) { toast.error('Select exactly 2 versions to compare'); return; }
    try {
      await compareVersions(compareIds[0], compareIds[1]);
      setTab('compare');
      setCompareIds([]);
    } catch { toast.error('Failed to compare versions'); }
  };

  const activeVersion = history.find(h => h.isActive);

  return (
    <div className="animate-in">
      {/* Page Header */}
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'var(--accent-gradient)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Sparkles size={22} color="white" />
          </div>
          <div>
            <h2>AI Itinerary Planner</h2>
            <p>{currentTrip?.title || `Trip #${tripId}`} · {history.length} version{history.length !== 1 ? 's' : ''} generated</p>
          </div>
        </div>
      </div>

      {/* Tab Bar */}
      <div className="tab-row">
        <button className={`tab-btn ${tab === 'generate' ? 'active' : ''}`} onClick={() => setTab('generate')}>
          <Sparkles size={14} style={{ marginRight: 6 }} />Generate
        </button>
        <button className={`tab-btn ${tab === 'history' ? 'active' : ''}`} onClick={() => { setTab('history'); setCompareIds([]); clearCompare(); }}>
          <Clock size={14} style={{ marginRight: 6 }} />Version History ({history.length})
        </button>
        {currentItinerary && (
          <button className={`tab-btn ${tab === 'view' ? 'active' : ''}`} onClick={() => setTab('view')}>
            <Calendar size={14} style={{ marginRight: 6 }} />
            {currentItinerary.versionName}
          </button>
        )}
        {compareResult && (
          <button className={`tab-btn ${tab === 'compare' ? 'active' : ''}`} onClick={() => setTab('compare')}>
            <GitCompare size={14} style={{ marginRight: 6 }} />Compare
          </button>
        )}
      </div>

      {/* ── TAB: Generate ── */}
      {tab === 'generate' && (
        <div className="generate-panel animate-in">
          <h3>✨ Generate New Itinerary</h3>
          <p className="sub">Our smart planner creates a day-by-day schedule based on your destinations, opening hours, and seasonal availability.</p>

          {activeVersion && (
            <div style={{ padding: '12px 16px', background: 'rgba(34,197,94,0.08)', border: '1px solid rgba(34,197,94,0.2)', borderRadius: 'var(--radius-md)', marginBottom: 24, fontSize: 13 }}>
              <CheckCircle2 size={14} style={{ color: 'var(--success)', marginRight: 8, verticalAlign: 'middle' }} />
              Active plan: <strong>{activeVersion.versionName}</strong> ({activeVersion.pace} · {activeVersion.totalActivities} activities)
            </div>
          )}

          {/* Pace selector */}
          <div className="input-group" style={{ marginBottom: 24 }}>
            <label>Travel Pace</label>
            <div className="pace-selector">
              {PACE_OPTIONS.map(opt => (
                <div
                  key={opt.value}
                  className={`pace-option ${pace === opt.value ? 'selected' : ''}`}
                  onClick={() => setPace(opt.value)}
                >
                  <div className="pace-icon">{opt.icon}</div>
                  <div className="pace-label" style={{ color: pace === opt.value ? opt.color : undefined }}>{opt.label}</div>
                  <div className="pace-desc">{opt.desc}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Version name */}
          <div className="input-group" style={{ marginBottom: 28 }}>
            <label>Version Name (optional)</label>
            <input
              className="input"
              placeholder={`e.g. "Relaxed Beach Holiday" (defaults to "Version ${history.length + 1}")`}
              value={versionName}
              onChange={e => setVersionName(e.target.value)}
            />
          </div>

          <button className="btn btn-primary btn-lg" onClick={handleGenerate} disabled={isGenerating}>
            {isGenerating ? (
              <><div className="spinner" style={{ width: 18, height: 18, margin: 0, border: '2px solid rgba(255,255,255,0.3)', borderTopColor: 'white' }} /> Generating...</>
            ) : (
              <><Sparkles size={18} /> Generate Itinerary</>
            )}
          </button>

          <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 12 }}>
            ℹ️ Each generation creates a new version. You can compare and activate your preferred plan anytime.
          </p>
        </div>
      )}

      {/* ── TAB: History ── */}
      {tab === 'history' && (
        <div className="animate-in">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <div style={{ fontSize: 14, color: 'var(--text-muted)' }}>
              {compareIds.length > 0
                ? `${compareIds.length}/2 versions selected for comparison`
                : 'Click a version to view, or enable compare mode'}
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              {compareIds.length === 2 && (
                <button className="btn btn-primary btn-sm" onClick={handleCompare}>
                  <GitCompare size={14} /> Compare Selected
                </button>
              )}
              {history.length >= 2 && compareIds.length === 0 && (
                <button className="btn btn-secondary btn-sm" onClick={() => setCompareIds([])}>
                  <GitCompare size={14} /> Compare Mode
                </button>
              )}
            </div>
          </div>

          {isLoading ? <div className="spinner" /> : history.length === 0 ? (
            <div className="empty-state">
              <Sparkles size={48} />
              <h3>No itineraries yet</h3>
              <p>Generate your first smart itinerary to get started!</p>
              <button className="btn btn-primary" onClick={() => setTab('generate')}>
                <Sparkles size={16} /> Generate Itinerary
              </button>
            </div>
          ) : (
            <div className="version-list">
              {history.map(v => (
                <VersionCard
                  key={v.id}
                  v={v}
                  isSelected={currentItinerary?.id === v.id}
                  compareMode={history.length >= 2}
                  compareSelected={compareIds.includes(v.id)}
                  onView={() => handleView(v.id)}
                  onActivate={() => handleActivate(v.id)}
                  onDelete={() => handleDelete(v.id)}
                  onSelect={() => handleSelectForCompare(v.id)}
                />
              ))}
            </div>
          )}
        </div>
      )}

      {/* ── TAB: View ── */}
      {tab === 'view' && currentItinerary && (
        <div className="animate-in">
          {/* Itinerary header */}
          <div className="card" style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                  <h3 style={{ fontSize: 20, fontWeight: 700 }}>{currentItinerary.versionName}</h3>
                  <span className={`badge ${currentItinerary.isActive ? 'badge-green' : 'badge-indigo'}`}>
                    {currentItinerary.status}
                  </span>
                </div>
                <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>{currentItinerary.notes}</div>
              </div>
              {!currentItinerary.isActive && (
                <button className="btn btn-secondary btn-sm" onClick={() => handleActivate(currentItinerary.id)}>
                  <CheckCircle2 size={14} /> Activate This Plan
                </button>
              )}
            </div>

            {/* Stats bar */}
            <div className="itinerary-stats-bar" style={{ marginTop: 20 }}>
              <div className="i-stat">
                <Calendar size={15} style={{ color: 'var(--accent-primary)' }} />
                <span className="i-stat-val">{currentItinerary.totalDays}</span>
                <span className="i-stat-label">days</span>
              </div>
              <div className="i-stat">
                <Activity size={15} style={{ color: '#f59e0b' }} />
                <span className="i-stat-val">{currentItinerary.totalActivities}</span>
                <span className="i-stat-label">activities</span>
              </div>
              <div className="i-stat">
                <Zap size={15} style={{ color: PACE_OPTIONS.find(p => p.value === currentItinerary.pace)?.color }} />
                <span className="i-stat-val">{currentItinerary.pace}</span>
                <span className="i-stat-label">pace</span>
              </div>
              <div className="i-stat">
                <Star size={15} style={{ color: '#8b5cf6' }} />
                <span className="i-stat-val">v{currentItinerary.versionNumber}</span>
                <span className="i-stat-label">version</span>
              </div>
            </div>
          </div>

          {/* Timeline */}
          <div className="itinerary-timeline">
            {currentItinerary.days.map(day => <DayCard key={day.id} day={day} />)}
          </div>
        </div>
      )}

      {/* ── TAB: Compare ── */}
      {tab === 'compare' && compareResult && <CompareView />}
    </div>
  );
}
