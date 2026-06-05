import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useActivityStore } from '../store/activityStore';
import { useTripStore } from '../store/tripStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, Plus, Trash2, Search, X, Clock, MapPin, Tag, Star, Bell,
  ChevronDown, ChevronUp, Compass, DollarSign, AlertTriangle, CheckCircle,
  Hourglass, XCircle, Sparkles,
} from 'lucide-react';
import type { ActivityCategory, ReservationStatus, ActivityRequest, ActivityResponse } from '../types/activity';

const CATEGORY_META: Record<ActivityCategory, { label: string; emoji: string; color: string }> = {
  ADVENTURE:  { label: 'Adventure',  emoji: '🧗', color: '#ef4444' },
  FAMILY:     { label: 'Family',     emoji: '👨‍👩‍👧‍👦', color: '#8b5cf6' },
  CULTURAL:   { label: 'Culture',    emoji: '🏛️', color: '#6366f1' },
  FOOD:       { label: 'Food',       emoji: '🍽️', color: '#f59e0b' },
  NATURE:     { label: 'Nature',     emoji: '🌿', color: '#22c55e' },
  NIGHTLIFE:  { label: 'Nightlife',  emoji: '🌙', color: '#a855f7' },
  ATTRACTION: { label: 'Attraction', emoji: '🎡', color: '#3b82f6' },
  SHOPPING:   { label: 'Shopping',   emoji: '🛍️', color: '#ec4899' },
  LEISURE:    { label: 'Leisure',    emoji: '☀️', color: '#14b8a6' },
  WORKSHOP:   { label: 'Workshop',   emoji: '🎨', color: '#f97316' },
  TRANSPORT:  { label: 'Transport',  emoji: '🚌', color: '#64748b' },
};

const STATUS_META: Record<ReservationStatus, { label: string; icon: React.ReactNode; color: string }> = {
  PENDING:    { label: 'Pending',    icon: <Hourglass size={12} />,   color: '#f59e0b' },
  CONFIRMED:  { label: 'Confirmed',  icon: <CheckCircle size={12} />, color: '#22c55e' },
  WAITLISTED: { label: 'Waitlisted', icon: <AlertTriangle size={12} />, color: '#f97316' },
  CANCELLED:  { label: 'Cancelled',  icon: <XCircle size={12} />,     color: '#ef4444' },
};

// ── Activity Card ──────────────────────────────────────────────────────
function ActivityCard({ act, onDelete, onStatusChange }: {
  act: ActivityResponse; onDelete: () => void; onStatusChange: (status: ReservationStatus) => void;
}) {
  const [open, setOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const catMeta = CATEGORY_META[act.category] || CATEGORY_META.ATTRACTION;
  const statusMeta = STATUS_META[act.reservationStatus];

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation(); e.preventDefault();
    setDeleting(true); onDelete();
  };

  return (
    <div className="activity-card">
      <div className="stay-header" onClick={() => setOpen(o => !o)} style={{ cursor: 'pointer' }}>
        <div className="segment-type-badge" style={{ background: catMeta.color + '20', color: catMeta.color }}>
          <span>{catMeta.emoji}</span> {catMeta.label}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ fontWeight: 600, fontSize: 15 }}>{act.name}</span>
            {act.priority >= 2 && <span style={{ fontSize: 10, background: '#ef444420', color: '#ef4444', padding: '2px 6px', borderRadius: 4, fontWeight: 600 }}>MUST-DO</span>}
            {act.priority === 1 && <span style={{ fontSize: 10, background: '#f59e0b20', color: '#f59e0b', padding: '2px 6px', borderRadius: 4, fontWeight: 600 }}>HIGH</span>}
          </div>
          {act.location && <div style={{ fontSize: 12, color: 'var(--text-muted)' }}><MapPin size={11} style={{ marginRight: 3 }} />{act.location}</div>}
        </div>
        <div style={{ textAlign: 'right', flexShrink: 0 }}>
          {act.activityDate && <div style={{ fontSize: 13, fontWeight: 500 }}>{act.activityDate}</div>}
          {act.startTime && <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{act.startTime}{act.endTime ? ` - ${act.endTime}` : ''}</div>}
        </div>
        <div className="reservation-badge" style={{ background: statusMeta.color + '20', color: statusMeta.color }}>
          {statusMeta.icon} {statusMeta.label}
        </div>
        {act.cost != null && <div style={{ fontWeight: 700, fontSize: 15, color: 'var(--success)', flexShrink: 0 }}>${act.cost}</div>}
        <button className="btn btn-danger btn-sm" disabled={deleting} onClick={handleRemove} style={{ flexShrink: 0 }}>
          <Trash2 size={13} /> {deleting ? '...' : 'Remove'}
        </button>
        <div style={{ cursor: 'pointer' }}>{open ? <ChevronUp size={14} /> : <ChevronDown size={14} />}</div>
      </div>

      {open && (
        <div className="segment-details">
          <div className="segment-detail-grid">
            {act.description && <div className="seg-detail" style={{ gridColumn: '1 / -1' }}><span className="seg-label">Description</span><span>{act.description}</span></div>}
            {act.openingHours && <div className="seg-detail"><span className="seg-label"><Clock size={12} /> Hours</span><span>{act.openingHours}</span></div>}
            {act.seasonalNotes && <div className="seg-detail"><span className="seg-label">🌦️ Seasonal</span><span>{act.seasonalNotes}</span></div>}
            {act.tagList.length > 0 && (
              <div className="seg-detail" style={{ gridColumn: '1 / -1' }}>
                <span className="seg-label"><Tag size={12} /> Tags</span>
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                  {act.tagList.map((t, i) => (
                    <span key={i} className="activity-tag">{t.trim()}</span>
                  ))}
                </div>
              </div>
            )}
            {act.bookingReference && <div className="seg-detail"><span className="seg-label">Booking Ref</span><span>{act.bookingReference}</span></div>}
            {act.reminderAt && <div className="seg-detail"><span className="seg-label"><Bell size={12} /> Reminder</span><span>{act.reminderAt.replace('T', ' at ')}</span></div>}
            {act.alternativeList.length > 0 && (
              <div className="seg-detail" style={{ gridColumn: '1 / -1' }}>
                <span className="seg-label">🔄 Alternatives</span>
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                  {act.alternativeList.map((a, i) => (
                    <span key={i} style={{ fontSize: 12, background: 'var(--bg-input)', padding: '3px 8px', borderRadius: 4 }}>{a.trim()}</span>
                  ))}
                </div>
              </div>
            )}
            {act.notes && <div className="seg-detail" style={{ gridColumn: '1 / -1' }}><span className="seg-label">Notes</span><span>{act.notes}</span></div>}
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 6, marginTop: 12 }}>
            <select className="input" style={{ width: 'auto', padding: '6px 10px', fontSize: 12 }}
              value={act.reservationStatus}
              onClick={e => e.stopPropagation()}
              onChange={e => { e.stopPropagation(); onStatusChange(e.target.value as ReservationStatus); }}>
              <option value="PENDING">⏳ Pending</option>
              <option value="CONFIRMED">✅ Confirmed</option>
              <option value="WAITLISTED">⏱️ Waitlisted</option>
              <option value="CANCELLED">❌ Cancelled</option>
            </select>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Add Activity Modal ─────────────────────────────────────────────────
function AddActivityModal({ tripId, onClose, prefill }: { tripId: number; onClose: () => void; prefill?: ActivityResponse | null }) {
  const { addActivity } = useActivityStore();
  const [form, setForm] = useState<ActivityRequest>({
    tripId,
    name: prefill?.name || '',
    description: prefill?.description || '',
    category: prefill?.category || 'ATTRACTION',
    tags: prefill?.tags || '',
    location: prefill?.location || '',
    address: '',
    activityDate: '',
    startTime: '',
    endTime: '',
    openingHours: prefill?.openingHours || '',
    seasonalNotes: prefill?.seasonalNotes || '',
    cost: prefill?.cost || undefined,
    currency: 'USD',
    reservationStatus: 'PENDING',
    bookingReference: '',
    bookingUrl: '',
    reminderAt: '',
    notes: '',
    alternatives: '',
    priority: prefill?.priority || 0,
  });
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    if (!form.name) { toast.error('Activity name is required'); return; }
    setSaving(true);
    try {
      await addActivity(form);
      toast.success('Activity added!');
      onClose();
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to add');
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 650 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h3 style={{ margin: 0 }}>🎯 Add Activity</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
          <div className="input-group">
            <label>Category</label>
            <select className="input" value={form.category} onChange={e => setForm({ ...form, category: e.target.value as ActivityCategory })}>
              {Object.entries(CATEGORY_META).map(([k, v]) => (
                <option key={k} value={k}>{v.emoji} {v.label}</option>
              ))}
            </select>
          </div>
          <div className="input-group">
            <label>Name *</label>
            <input className="input" placeholder="e.g. Snorkeling Tour" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
          </div>
          <div className="input-group" style={{ gridColumn: '1 / -1' }}>
            <label>Description</label>
            <input className="input" placeholder="Brief description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Location</label>
            <input className="input" placeholder="e.g. Goa Beach" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Tags (comma-separated)</label>
            <input className="input" placeholder="family,outdoor,adventure" value={form.tags} onChange={e => setForm({ ...form, tags: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Date</label>
            <input className="input" type="date" value={form.activityDate} onChange={e => setForm({ ...form, activityDate: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Cost ($)</label>
            <input className="input" type="number" placeholder="0.00" value={form.cost ?? ''} onChange={e => setForm({ ...form, cost: e.target.value ? parseFloat(e.target.value) : undefined })} />
          </div>
          <div className="input-group">
            <label>Start Time</label>
            <input className="input" type="time" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} />
          </div>
          <div className="input-group">
            <label>End Time</label>
            <input className="input" type="time" value={form.endTime} onChange={e => setForm({ ...form, endTime: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Opening Hours</label>
            <input className="input" placeholder="9:00 AM - 6:00 PM" value={form.openingHours} onChange={e => setForm({ ...form, openingHours: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Seasonal Notes</label>
            <input className="input" placeholder="Best Oct-Mar" value={form.seasonalNotes} onChange={e => setForm({ ...form, seasonalNotes: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Reservation Status</label>
            <select className="input" value={form.reservationStatus} onChange={e => setForm({ ...form, reservationStatus: e.target.value as ReservationStatus })}>
              <option value="PENDING">⏳ Pending</option>
              <option value="CONFIRMED">✅ Confirmed</option>
              <option value="WAITLISTED">⏱️ Waitlisted</option>
              <option value="CANCELLED">❌ Cancelled</option>
            </select>
          </div>
          <div className="input-group">
            <label>Priority</label>
            <select className="input" value={form.priority} onChange={e => setForm({ ...form, priority: parseInt(e.target.value) })}>
              <option value="0">Normal</option>
              <option value="1">⭐ High</option>
              <option value="2">🔥 Must-Do</option>
            </select>
          </div>
          <div className="input-group">
            <label>Reminder</label>
            <input className="input" type="datetime-local" value={form.reminderAt} onChange={e => setForm({ ...form, reminderAt: e.target.value })} />
          </div>
          <div className="input-group" style={{ gridColumn: '1 / -1' }}>
            <label>Alternatives (comma-separated)</label>
            <input className="input" placeholder="Plan B activities if this doesn't work out" value={form.alternatives} onChange={e => setForm({ ...form, alternatives: e.target.value })} />
          </div>
          <div className="input-group" style={{ gridColumn: '1 / -1' }}>
            <label>Notes</label>
            <textarea className="input" rows={2} placeholder="Any notes" value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 20 }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Add Activity'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Catalog Search ─────────────────────────────────────────────────────
function CatalogPanel({ onAdd }: { onAdd: (act: ActivityResponse) => void }) {
  const { catalogResults, searchCatalog, clearCatalog, isSearching } = useActivityStore();
  const [dest, setDest] = useState('');
  const [cat, setCat] = useState<ActivityCategory>('ADVENTURE');

  const handleSearch = () => {
    if (!dest) { toast.error('Enter a destination'); return; }
    searchCatalog(dest, cat);
  };

  return (
    <div className="generate-panel animate-in">
      <h3><Sparkles size={18} style={{ marginRight: 6 }} />Discover Activities</h3>
      <p className="sub">Browse our catalog of curated activities by destination and category.</p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
        <div className="input-group">
          <label>Destination</label>
          <input className="input" placeholder="e.g. Goa, Paris, Tokyo" value={dest} onChange={e => setDest(e.target.value)} />
        </div>
        <div className="input-group">
          <label>Category</label>
          <select className="input" value={cat} onChange={e => { setCat(e.target.value as ActivityCategory); clearCatalog(); }}>
            {Object.entries(CATEGORY_META).filter(([k]) => k !== 'TRANSPORT').map(([k, v]) => (
              <option key={k} value={k}>{v.emoji} {v.label}</option>
            ))}
          </select>
        </div>
      </div>

      <button className="btn btn-primary" onClick={handleSearch} disabled={isSearching}>
        {isSearching ? 'Searching...' : <><Search size={15} /> Search Catalog</>}
      </button>

      {catalogResults.length > 0 && (
        <div style={{ marginTop: 20 }}>
          <h4 style={{ fontSize: 15, fontWeight: 600, marginBottom: 12 }}>{catalogResults.length} activities found</h4>
          <div className="search-results-list">
            {catalogResults.map((act, i) => {
              const catMeta = CATEGORY_META[act.category] || CATEGORY_META.ATTRACTION;
              return (
                <div key={i} className="search-result-card">
                  <div className="segment-type-badge" style={{ background: catMeta.color + '20', color: catMeta.color }}>
                    <span>{catMeta.emoji}</span> {catMeta.label}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{act.name}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>{act.description}</div>
                    <div style={{ display: 'flex', gap: 12, marginTop: 6, fontSize: 11, color: 'var(--text-secondary)' }}>
                      {act.openingHours && <span><Clock size={10} /> {act.openingHours}</span>}
                      {act.seasonalNotes && <span>🌦️ {act.seasonalNotes}</span>}
                    </div>
                    {act.tagList.length > 0 && (
                      <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap', marginTop: 6 }}>
                        {act.tagList.map((t, j) => <span key={j} className="activity-tag">{t}</span>)}
                      </div>
                    )}
                  </div>
                  {act.cost != null && <div style={{ fontWeight: 700, fontSize: 16, color: 'var(--success)' }}>${act.cost}</div>}
                  <button className="btn btn-secondary btn-sm" onClick={() => onAdd(act)}>
                    <Plus size={13} /> Add
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
export default function ActivitiesPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();

  const { activities, fetchActivities, deleteActivity, updateActivity, isLoading } = useActivityStore();
  const { currentTrip, fetchTrip } = useTripStore();

  const [tab, setTab] = useState<'all' | 'catalog' | 'waitlist'>('all');
  const [showAdd, setShowAdd] = useState(false);
  const [prefill, setPrefill] = useState<ActivityResponse | null>(null);
  const [filterCat, setFilterCat] = useState<string>('ALL');

  useEffect(() => {
    fetchActivities(tripId);
    if (!currentTrip || currentTrip.id !== tripId) fetchTrip(tripId);
  }, [tripId]);

  const handleDelete = async (actId: number) => {
    try {
      await deleteActivity(actId, tripId);
      toast.success('Activity removed');
    } catch { toast.error('Failed to remove'); }
  };

  const handleStatusChange = async (act: ActivityResponse, newStatus: ReservationStatus) => {
    try {
      await updateActivity(act.id, {
        tripId, name: act.name, description: act.description || '', category: act.category,
        tags: act.tags, location: act.location, activityDate: act.activityDate,
        startTime: act.startTime, endTime: act.endTime, openingHours: act.openingHours,
        seasonalNotes: act.seasonalNotes, cost: act.cost, reservationStatus: newStatus,
        bookingReference: act.bookingReference, reminderAt: act.reminderAt,
        notes: act.notes, alternatives: act.alternatives, priority: act.priority,
      });
      toast.success(`Status changed to ${newStatus}`);
    } catch { toast.error('Failed to update status'); }
  };

  const handleCatalogAdd = (act: ActivityResponse) => {
    setPrefill(act);
    setShowAdd(true);
    setTab('all');
  };

  const filtered = filterCat === 'ALL' ? activities : activities.filter(a => a.category === filterCat);
  const waitlisted = activities.filter(a => a.reservationStatus === 'WAITLISTED');
  const totalCost = activities.reduce((sum, a) => sum + (a.cost || 0), 0);
  const confirmed = activities.filter(a => a.reservationStatus === 'CONFIRMED').length;

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #f59e0b, #ef4444)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Compass size={22} color="white" />
          </div>
          <div>
            <h2>Activities & Reservations</h2>
            <p>{currentTrip?.title || `Trip #${tripId}`} · {activities.length} activit{activities.length !== 1 ? 'ies' : 'y'}</p>
          </div>
        </div>
      </div>

      {/* Stats bar */}
      <div className="itinerary-stats-bar" style={{ marginBottom: 24 }}>
        <div className="i-stat"><Compass size={15} style={{ color: '#f59e0b' }} /><span className="i-stat-val">{activities.length}</span><span className="i-stat-label">activities</span></div>
        <div className="i-stat"><CheckCircle size={15} style={{ color: '#22c55e' }} /><span className="i-stat-val">{confirmed}</span><span className="i-stat-label">confirmed</span></div>
        <div className="i-stat"><AlertTriangle size={15} style={{ color: '#f97316' }} /><span className="i-stat-val">{waitlisted.length}</span><span className="i-stat-label">waitlisted</span></div>
        <div className="i-stat"><DollarSign size={15} style={{ color: '#22c55e' }} /><span className="i-stat-val">${totalCost.toFixed(0)}</span><span className="i-stat-label">total cost</span></div>
      </div>

      {/* Tab Bar */}
      <div className="tab-row">
        <button className={`tab-btn ${tab === 'all' ? 'active' : ''}`} onClick={() => setTab('all')}>
          <Compass size={14} style={{ marginRight: 6 }} /> All Activities ({activities.length})
        </button>
        <button className={`tab-btn ${tab === 'catalog' ? 'active' : ''}`} onClick={() => setTab('catalog')}>
          <Search size={14} style={{ marginRight: 6 }} /> Discover
        </button>
        <button className={`tab-btn ${tab === 'waitlist' ? 'active' : ''}`} onClick={() => setTab('waitlist')}>
          <AlertTriangle size={14} style={{ marginRight: 6 }} /> Waitlist ({waitlisted.length})
        </button>
      </div>

      {/* TAB: All Activities */}
      {tab === 'all' && (
        <div className="animate-in">
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
            <select className="input" style={{ width: 'auto' }} value={filterCat} onChange={e => setFilterCat(e.target.value)}>
              <option value="ALL">All Categories</option>
              {Object.entries(CATEGORY_META).map(([k, v]) => <option key={k} value={k}>{v.emoji} {v.label}</option>)}
            </select>
            <button className="btn btn-primary btn-sm" onClick={() => { setPrefill(null); setShowAdd(true); }}>
              <Plus size={14} /> Add Activity
            </button>
          </div>

          {isLoading ? <div className="spinner" /> : filtered.length === 0 ? (
            <div className="empty-state">
              <Compass size={48} />
              <h3>No activities yet</h3>
              <p>Discover activities from our catalog or manually add your own plans.</p>
              <button className="btn btn-primary" onClick={() => setTab('catalog')}>
                <Sparkles size={16} /> Discover Activities
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {filtered.map(act => (
                <ActivityCard key={act.id} act={act} onDelete={() => handleDelete(act.id)}
                  onStatusChange={(status) => handleStatusChange(act, status)} />
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB: Catalog */}
      {tab === 'catalog' && <CatalogPanel onAdd={handleCatalogAdd} />}

      {/* TAB: Waitlist */}
      {tab === 'waitlist' && (
        <div className="animate-in">
          {waitlisted.length === 0 ? (
            <div className="empty-state">
              <AlertTriangle size={48} />
              <h3>No waitlisted activities</h3>
              <p>Activities marked as "Waitlisted" will appear here so you can track them.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {waitlisted.map(act => (
                <ActivityCard key={act.id} act={act} onDelete={() => handleDelete(act.id)}
                  onStatusChange={(status) => handleStatusChange(act, status)} />
              ))}
            </div>
          )}
        </div>
      )}

      {showAdd && <AddActivityModal tripId={tripId} onClose={() => { setShowAdd(false); setPrefill(null); }} prefill={prefill} />}
    </div>
  );
}
