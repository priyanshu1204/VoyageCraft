import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTransportStore } from '../store/transportStore';
import { useTripStore } from '../store/tripStore';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import {
  ArrowLeft, Plane, Train, Bus, Car, Plus, Trash2, Search,
  AlertTriangle, AlertCircle, Clock, MapPin, ChevronDown, ChevronUp, Globe, X,
} from 'lucide-react';
import type { TransportType, TransportRequest, MockTransportOption, TransportResponse } from '../types/transport';

const TYPE_META: Record<TransportType, { icon: React.ReactNode; label: string; emoji: string; color: string }> = {
  FLIGHT:     { icon: <Plane size={16} />,  label: 'Flight',     emoji: '✈️', color: '#6366f1' },
  TRAIN:      { icon: <Train size={16} />,  label: 'Train',      emoji: '🚆', color: '#22c55e' },
  BUS:        { icon: <Bus size={16} />,    label: 'Bus',        emoji: '🚌', color: '#f59e0b' },
  CAR_RENTAL: { icon: <Car size={16} />,    label: 'Car Rental', emoji: '🚗', color: '#3b82f6' },
};

const COMMON_TIMEZONES = [
  'UTC', 'Asia/Kolkata', 'Asia/Tokyo', 'Asia/Dubai', 'Asia/Singapore',
  'Europe/London', 'Europe/Paris', 'Europe/Berlin',
  'America/New_York', 'America/Chicago', 'America/Los_Angeles',
  'Australia/Sydney', 'Pacific/Auckland',
];

function formatDuration(mins: number) {
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
}

// ── Segment Card ───────────────────────────────────────────────────────
function SegmentCard({ seg, onDelete, hasConflict }: { seg: TransportResponse; onDelete: () => void; hasConflict: boolean }) {
  const [open, setOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const meta = TYPE_META[seg.type];

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    setDeleting(true);
    onDelete();
  };

  return (
    <div className={`transport-segment ${hasConflict ? 'segment-conflict' : ''}`}>
      <div className="segment-header" onClick={() => setOpen(o => !o)} style={{ cursor: 'pointer' }}>
        <div className="segment-type-badge" style={{ background: meta.color + '20', color: meta.color }}>
          {meta.icon} {meta.label}
        </div>
        <div className="segment-route">
          <span className="segment-from">{seg.departureLocation}</span>
          <span className="segment-arrow">→</span>
          <span className="segment-to">{seg.arrivalLocation}</span>
        </div>
        <div className="segment-times">
          <div style={{ fontSize: 13, fontWeight: 600 }}>{seg.departureTimeFormatted}</div>
          <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>to {seg.arrivalTimeFormatted}</div>
        </div>
        <div className="segment-duration">
          <Clock size={13} style={{ color: 'var(--text-muted)' }} />
          <span>{formatDuration(seg.durationMinutes)}</span>
        </div>
        <button
          className="btn btn-danger btn-sm"
          disabled={deleting}
          onClick={handleRemove}
          style={{ flexShrink: 0 }}
        >
          <Trash2 size={13} /> {deleting ? '...' : 'Remove'}
        </button>
        <div style={{ display: 'flex', gap: 6, alignItems: 'center', cursor: 'pointer' }}>
          {open ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
        </div>
      </div>

      {open && (
        <div className="segment-details">
          <div className="segment-detail-grid">
            {seg.provider && <div className="seg-detail"><span className="seg-label">Provider</span><span>{seg.provider}</span></div>}
            {seg.flightNumber && <div className="seg-detail"><span className="seg-label">Flight #</span><span>{seg.flightNumber}</span></div>}
            {seg.bookingReference && <div className="seg-detail"><span className="seg-label">Booking Ref</span><span>{seg.bookingReference}</span></div>}
            {seg.cost != null && <div className="seg-detail"><span className="seg-label">Cost</span><span>${seg.cost}</span></div>}
            <div className="seg-detail"><span className="seg-label"><Globe size={12} /> Dep. TZ</span><span>{seg.departureTimezone}</span></div>
            <div className="seg-detail"><span className="seg-label"><Globe size={12} /> Arr. TZ</span><span>{seg.arrivalTimezone}</span></div>
            {seg.notes && <div className="seg-detail" style={{ gridColumn: '1 / -1' }}><span className="seg-label">Notes</span><span>{seg.notes}</span></div>}
          </div>
        </div>
      )}
    </div>
  );
}

// ── Add Segment Modal ──────────────────────────────────────────────────
function AddSegmentModal({ tripId, onClose, prefill }: { tripId: number; onClose: () => void; prefill?: MockTransportOption | null }) {
  const { addSegment } = useTransportStore();
  const [form, setForm] = useState<TransportRequest>({
    tripId,
    type: prefill?.type || 'FLIGHT',
    provider: prefill?.provider || '',
    flightNumber: prefill?.flightNumber || '',
    departureLocation: prefill?.departureLocation || '',
    arrivalLocation: prefill?.arrivalLocation || '',
    departureTime: prefill?.departureTime || '',
    arrivalTime: prefill?.arrivalTime || '',
    departureTimezone: prefill?.departureTimezone || 'UTC',
    arrivalTimezone: prefill?.arrivalTimezone || 'UTC',
    bookingReference: '',
    cost: prefill?.cost || undefined,
    notes: '',
  });
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    if (!form.departureLocation || !form.arrivalLocation || !form.departureTime || !form.arrivalTime) {
      toast.error('Please fill all required fields'); return;
    }
    setSaving(true);
    try {
      await addSegment(form);
      toast.success('Transport segment added!');
      onClose();
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to add segment');
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 600 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h3 style={{ margin: 0 }}>Add Transport Segment</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
          <div className="input-group">
            <label>Type</label>
            <select className="input" value={form.type} onChange={e => setForm({ ...form, type: e.target.value as TransportType })}>
              <option value="FLIGHT">✈️ Flight</option>
              <option value="TRAIN">🚆 Train</option>
              <option value="BUS">🚌 Bus</option>
              <option value="CAR_RENTAL">🚗 Car Rental</option>
            </select>
          </div>
          <div className="input-group">
            <label>Provider</label>
            <input className="input" placeholder="e.g. AeroSwift" value={form.provider} onChange={e => setForm({ ...form, provider: e.target.value })} />
          </div>
          <div className="input-group">
            <label>From *</label>
            <input className="input" placeholder="Departure city" value={form.departureLocation} onChange={e => setForm({ ...form, departureLocation: e.target.value })} />
          </div>
          <div className="input-group">
            <label>To *</label>
            <input className="input" placeholder="Arrival city" value={form.arrivalLocation} onChange={e => setForm({ ...form, arrivalLocation: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Departure Time *</label>
            <input className="input" type="datetime-local" value={form.departureTime} onChange={e => setForm({ ...form, departureTime: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Arrival Time *</label>
            <input className="input" type="datetime-local" value={form.arrivalTime} onChange={e => setForm({ ...form, arrivalTime: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Departure Timezone</label>
            <select className="input" value={form.departureTimezone} onChange={e => setForm({ ...form, departureTimezone: e.target.value })}>
              {COMMON_TIMEZONES.map(tz => <option key={tz} value={tz}>{tz}</option>)}
            </select>
          </div>
          <div className="input-group">
            <label>Arrival Timezone</label>
            <select className="input" value={form.arrivalTimezone} onChange={e => setForm({ ...form, arrivalTimezone: e.target.value })}>
              {COMMON_TIMEZONES.map(tz => <option key={tz} value={tz}>{tz}</option>)}
            </select>
          </div>
          {form.type === 'FLIGHT' && (
            <div className="input-group">
              <label>Flight Number</label>
              <input className="input" placeholder="e.g. VA-342" value={form.flightNumber} onChange={e => setForm({ ...form, flightNumber: e.target.value })} />
            </div>
          )}
          <div className="input-group">
            <label>Cost ($)</label>
            <input className="input" type="number" placeholder="0.00" value={form.cost ?? ''} onChange={e => setForm({ ...form, cost: e.target.value ? parseFloat(e.target.value) : undefined })} />
          </div>
          <div className="input-group">
            <label>Booking Reference</label>
            <input className="input" placeholder="Optional" value={form.bookingReference} onChange={e => setForm({ ...form, bookingReference: e.target.value })} />
          </div>
          <div className="input-group" style={{ gridColumn: '1 / -1' }}>
            <label>Notes</label>
            <textarea className="input" rows={2} placeholder="Optional notes" value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 20 }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Add Segment'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Search Panel ───────────────────────────────────────────────────────
function SearchPanel({ tripId, onBook }: { tripId: number; onBook: (opt: MockTransportOption) => void }) {
  const { searchResults, searchMock, clearSearch, isSearching } = useTransportStore();
  const [searchType, setSearchType] = useState<TransportType>('FLIGHT');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [date, setDate] = useState('');
  const [fromTz, setFromTz] = useState('UTC');
  const [toTz, setToTz] = useState('UTC');

  const handleSearch = () => {
    if (!from || !to || !date) { toast.error('Fill in from, to, and date'); return; }
    searchMock({ type: searchType, from, to, date, fromTimezone: fromTz, toTimezone: toTz });
  };

  return (
    <div className="generate-panel animate-in">
      <h3>🔍 Search Transport Options</h3>
      <p className="sub">Find mock flights, trains, or buses between destinations. Select one to add it to your trip.</p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12, marginBottom: 16 }}>
        <div className="input-group">
          <label>Type</label>
          <select className="input" value={searchType} onChange={e => { setSearchType(e.target.value as TransportType); clearSearch(); }}>
            <option value="FLIGHT">✈️ Flight</option>
            <option value="TRAIN">🚆 Train</option>
            <option value="BUS">🚌 Bus</option>
            <option value="CAR_RENTAL">🚗 Car Rental</option>
          </select>
        </div>
        <div className="input-group">
          <label>From</label>
          <input className="input" placeholder="Departure city" value={from} onChange={e => setFrom(e.target.value)} />
        </div>
        <div className="input-group">
          <label>To</label>
          <input className="input" placeholder="Arrival city" value={to} onChange={e => setTo(e.target.value)} />
        </div>
        <div className="input-group">
          <label>Date</label>
          <input className="input" type="date" value={date} onChange={e => setDate(e.target.value)} />
        </div>
        <div className="input-group">
          <label>From Timezone</label>
          <select className="input" value={fromTz} onChange={e => setFromTz(e.target.value)}>
            {COMMON_TIMEZONES.map(tz => <option key={tz} value={tz}>{tz}</option>)}
          </select>
        </div>
        <div className="input-group">
          <label>To Timezone</label>
          <select className="input" value={toTz} onChange={e => setToTz(e.target.value)}>
            {COMMON_TIMEZONES.map(tz => <option key={tz} value={tz}>{tz}</option>)}
          </select>
        </div>
      </div>

      <button className="btn btn-primary" onClick={handleSearch} disabled={isSearching}>
        {isSearching ? 'Searching...' : <><Search size={15} /> Search</>}
      </button>

      {searchResults.length > 0 && (
        <div style={{ marginTop: 20 }}>
          <h4 style={{ fontSize: 15, fontWeight: 600, marginBottom: 12 }}>
            {searchResults.length} options found
          </h4>
          <div className="search-results-list">
            {searchResults.map((opt, i) => {
              const meta = TYPE_META[opt.type];
              return (
                <div key={i} className="search-result-card">
                  <div className="segment-type-badge" style={{ background: meta.color + '20', color: meta.color }}>
                    {meta.icon} {opt.provider}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>
                      {opt.departureLocation} → {opt.arrivalLocation}
                      {opt.flightNumber && <span style={{ marginLeft: 8, fontSize: 12, color: 'var(--text-muted)' }}>{opt.flightNumber}</span>}
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>
                      {opt.departureTime.replace('T', ' ')} → {opt.arrivalTime.replace('T', ' ')} · {formatDuration(opt.durationMinutes)} · {opt.stops} stop{opt.stops !== 1 ? 's' : ''}
                    </div>
                  </div>
                  <div style={{ fontWeight: 700, fontSize: 16, color: 'var(--success)' }}>${opt.cost}</div>
                  <button className="btn btn-secondary btn-sm" onClick={() => onBook(opt)}>
                    <Plus size={13} /> Book
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

// ── Main Page ──────────────────────────────────────────────────────────
export default function TransportPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();

  const { segments, conflicts, fetchSegments, detectConflicts, deleteSegment, isLoading } = useTransportStore();
  const { currentTrip, fetchTrip } = useTripStore();

  const [tab, setTab] = useState<'segments' | 'search'>('segments');
  const [showAdd, setShowAdd] = useState(false);
  const [prefill, setPrefill] = useState<MockTransportOption | null>(null);

  useEffect(() => {
    fetchSegments(tripId);
    detectConflicts(tripId);
    if (!currentTrip || currentTrip.id !== tripId) fetchTrip(tripId);
  }, [tripId]);

  const handleDelete = async (segId: number) => {
    try {
      await deleteSegment(segId, tripId);
      toast.success('Segment removed');
    } catch { toast.error('Failed to remove'); }
  };

  const handleBook = (opt: MockTransportOption) => {
    setPrefill(opt);
    setShowAdd(true);
    setTab('segments');
  };

  const conflictSegmentIds = new Set(conflicts.flatMap(c => [c.segmentAId, c.segmentBId]));
  const totalCost = segments.reduce((sum, s) => sum + (s.cost || 0), 0);

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      {/* Header */}
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #6366f1, #3b82f6)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Plane size={22} color="white" />
          </div>
          <div>
            <h2>Transportation</h2>
            <p>{currentTrip?.title || `Trip #${tripId}`} · {segments.length} segment{segments.length !== 1 ? 's' : ''}</p>
          </div>
        </div>
      </div>

      {/* Conflict Alerts */}
      {conflicts.length > 0 && (
        <div className="conflict-alerts animate-in" style={{ marginBottom: 24 }}>
          {conflicts.map((c, i) => (
            <div key={i} className={`conflict-alert conflict-${c.severity.toLowerCase()}`}>
              {c.severity === 'ERROR' ? <AlertCircle size={16} /> : <AlertTriangle size={16} />}
              <span>{c.message}</span>
            </div>
          ))}
        </div>
      )}

      {/* Stats bar */}
      <div className="itinerary-stats-bar" style={{ marginBottom: 24 }}>
        <div className="i-stat"><Plane size={15} style={{ color: '#6366f1' }} /><span className="i-stat-val">{segments.filter(s => s.type === 'FLIGHT').length}</span><span className="i-stat-label">flights</span></div>
        <div className="i-stat"><Train size={15} style={{ color: '#22c55e' }} /><span className="i-stat-val">{segments.filter(s => s.type === 'TRAIN').length}</span><span className="i-stat-label">trains</span></div>
        <div className="i-stat"><Bus size={15} style={{ color: '#f59e0b' }} /><span className="i-stat-val">{segments.filter(s => s.type === 'BUS').length}</span><span className="i-stat-label">buses</span></div>
        <div className="i-stat"><span className="i-stat-val" style={{ color: 'var(--success)' }}>${totalCost.toFixed(0)}</span><span className="i-stat-label">total cost</span></div>
        {conflicts.length > 0 && <div className="i-stat"><AlertTriangle size={15} style={{ color: 'var(--danger)' }} /><span className="i-stat-val" style={{ color: 'var(--danger)' }}>{conflicts.length}</span><span className="i-stat-label">conflict{conflicts.length > 1 ? 's' : ''}</span></div>}
      </div>

      {/* Tab Bar */}
      <div className="tab-row">
        <button className={`tab-btn ${tab === 'segments' ? 'active' : ''}`} onClick={() => setTab('segments')}>
          <MapPin size={14} style={{ marginRight: 6 }} /> My Segments ({segments.length})
        </button>
        <button className={`tab-btn ${tab === 'search' ? 'active' : ''}`} onClick={() => setTab('search')}>
          <Search size={14} style={{ marginRight: 6 }} /> Search & Book
        </button>
      </div>

      {/* TAB: Segments */}
      {tab === 'segments' && (
        <div className="animate-in">
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
            <button className="btn btn-primary btn-sm" onClick={() => { setPrefill(null); setShowAdd(true); }}>
              <Plus size={14} /> Add Segment
            </button>
          </div>

          {isLoading ? <div className="spinner" /> : segments.length === 0 ? (
            <div className="empty-state">
              <Plane size={48} />
              <h3>No transport segments yet</h3>
              <p>Search for flights, trains, or buses, or manually add your bookings.</p>
              <button className="btn btn-primary" onClick={() => setTab('search')}>
                <Search size={16} /> Search Transport
              </button>
            </div>
          ) : (
            <div className="transport-timeline">
              {segments.map((seg, i) => (
                <div key={seg.id}>
                  <SegmentCard seg={seg} onDelete={() => handleDelete(seg.id)} hasConflict={conflictSegmentIds.has(seg.id)} />
                  {i < segments.length - 1 && (
                    <div className="layover-indicator">
                      <div className="layover-line" />
                      <div className="layover-info">
                        <Clock size={12} />
                        {(() => {
                          const arrMs = new Date(seg.arrivalTime).getTime();
                          const depMs = new Date(segments[i + 1].departureTime).getTime();
                          const layoverMins = Math.round((depMs - arrMs) / 60000);
                          const isShort = layoverMins < 120;
                          return (
                            <span style={{ color: layoverMins < 0 ? 'var(--danger)' : isShort ? 'var(--warning)' : 'var(--text-muted)' }}>
                              {layoverMins < 0 ? `Overlap ${Math.abs(layoverMins)}m` : `${formatDuration(layoverMins)} layover`}
                              {seg.arrivalLocation !== segments[i + 1].departureLocation && ' ⚠️ Different locations'}
                            </span>
                          );
                        })()}
                      </div>
                      <div className="layover-line" />
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB: Search */}
      {tab === 'search' && <SearchPanel tripId={tripId} onBook={handleBook} />}

      {/* Add Modal */}
      {showAdd && <AddSegmentModal tripId={tripId} onClose={() => { setShowAdd(false); setPrefill(null); }} prefill={prefill} />}
    </div>
  );
}
