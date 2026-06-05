import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useStayStore } from '../store/stayStore';
import { useTripStore } from '../store/tripStore';
import toast from 'react-hot-toast';
import { format, eachDayOfInterval, parseISO, isWithinInterval, addDays } from 'date-fns';
import {
  ArrowLeft, Plus, Trash2, Building2, Home, Star, X, CalendarDays,
  List, Phone, Mail, ChevronDown, ChevronUp, FileText, DollarSign, Moon,
} from 'lucide-react';
import type { StayType, StayRequest, StayResponse } from '../types/stay';

const TYPE_META: Record<StayType, { icon: React.ReactNode; label: string; color: string }> = {
  HOTEL:     { icon: <Building2 size={16} />, label: 'Hotel',     color: '#6366f1' },
  HOSTEL:    { icon: <Building2 size={16} />, label: 'Hostel',    color: '#f59e0b' },
  HOMESTAY:  { icon: <Home size={16} />,      label: 'Homestay',  color: '#22c55e' },
  AIRBNB:    { icon: <Home size={16} />,      label: 'Airbnb',    color: '#ef4444' },
  RESORT:    { icon: <Building2 size={16} />, label: 'Resort',    color: '#8b5cf6' },
  APARTMENT: { icon: <Home size={16} />,      label: 'Apartment', color: '#3b82f6' },
  OTHER:     { icon: <Building2 size={16} />, label: 'Other',     color: '#64748b' },
};

function StarRating({ rating }: { rating: number }) {
  return (
    <div style={{ display: 'flex', gap: 2 }}>
      {[1, 2, 3, 4, 5].map(i => (
        <Star key={i} size={13} fill={i <= rating ? '#f59e0b' : 'transparent'} color={i <= rating ? '#f59e0b' : 'var(--text-muted)'} />
      ))}
    </div>
  );
}

// ── Stay Card ──────────────────────────────────────────────────────────
function StayCard({ stay, onDelete }: { stay: StayResponse; onDelete: () => void }) {
  const [open, setOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const meta = TYPE_META[stay.type];

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    setDeleting(true);
    onDelete();
  };

  return (
    <div className="stay-card">
      <div className="stay-header" onClick={() => setOpen(o => !o)} style={{ cursor: 'pointer' }}>
        <div className="segment-type-badge" style={{ background: meta.color + '20', color: meta.color }}>
          {meta.icon} {meta.label}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 600, fontSize: 15 }}>{stay.name}</div>
          {stay.city && <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{stay.city}{stay.address ? ` · ${stay.address}` : ''}</div>}
        </div>
        <div style={{ textAlign: 'right', flexShrink: 0 }}>
          <div style={{ fontSize: 13, fontWeight: 600 }}>
            {format(parseISO(stay.checkInDate), 'MMM d')} → {format(parseISO(stay.checkOutDate), 'MMM d, yyyy')}
          </div>
          <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            {stay.totalNights} night{stay.totalNights !== 1 ? 's' : ''}
            {stay.checkInTime && ` · in ${stay.checkInTime}`}
            {stay.checkOutTime && ` · out ${stay.checkOutTime}`}
          </div>
        </div>
        {stay.starRating > 0 && <StarRating rating={stay.starRating} />}
        {stay.totalCost != null && (
          <div style={{ fontWeight: 700, fontSize: 16, color: 'var(--success)', flexShrink: 0 }}>${stay.totalCost}</div>
        )}
        <button className="btn btn-danger btn-sm" disabled={deleting} onClick={handleRemove} style={{ flexShrink: 0 }}>
          <Trash2 size={13} /> {deleting ? '...' : 'Remove'}
        </button>
        <div style={{ cursor: 'pointer' }}>
          {open ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
        </div>
      </div>

      {open && (
        <div className="segment-details">
          <div className="segment-detail-grid">
            {stay.costPerNight != null && <div className="seg-detail"><span className="seg-label">Per Night</span><span>${stay.costPerNight}</span></div>}
            {stay.bookingReference && <div className="seg-detail"><span className="seg-label">Booking Ref</span><span>{stay.bookingReference}</span></div>}
            {stay.contactPhone && <div className="seg-detail"><span className="seg-label"><Phone size={12} /> Phone</span><span>{stay.contactPhone}</span></div>}
            {stay.contactEmail && <div className="seg-detail"><span className="seg-label"><Mail size={12} /> Email</span><span>{stay.contactEmail}</span></div>}
            {stay.amenities && <div className="seg-detail" style={{ gridColumn: '1 / -1' }}><span className="seg-label">Amenities</span><span>{stay.amenities}</span></div>}
            {stay.cancellationPolicy && (
              <div className="seg-detail" style={{ gridColumn: '1 / -1' }}>
                <span className="seg-label"><FileText size={12} /> Cancellation Policy</span>
                <span style={{ fontSize: 13 }}>{stay.cancellationPolicy}</span>
              </div>
            )}
            {stay.notes && <div className="seg-detail" style={{ gridColumn: '1 / -1' }}><span className="seg-label">Notes</span><span>{stay.notes}</span></div>}
            {stay.bookingUrl && (
              <div className="seg-detail" style={{ gridColumn: '1 / -1' }}>
                <a href={stay.bookingUrl} target="_blank" rel="noreferrer" style={{ fontSize: 13 }}>View Booking →</a>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

// ── Add Stay Modal ─────────────────────────────────────────────────────
function AddStayModal({ tripId, onClose }: { tripId: number; onClose: () => void }) {
  const { addStay } = useStayStore();
  const [form, setForm] = useState<StayRequest>({
    tripId,
    type: 'HOTEL',
    name: '',
    address: '',
    city: '',
    checkInDate: '',
    checkInTime: '14:00',
    checkOutDate: '',
    checkOutTime: '11:00',
    costPerNight: undefined,
    totalCost: undefined,
    currency: 'USD',
    bookingReference: '',
    bookingUrl: '',
    contactPhone: '',
    contactEmail: '',
    notes: '',
    cancellationPolicy: '',
    amenities: '',
    starRating: 0,
  });
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    if (!form.name || !form.checkInDate || !form.checkOutDate) {
      toast.error('Please fill name, check-in, and check-out dates'); return;
    }
    setSaving(true);
    try {
      await addStay(form);
      toast.success('Stay added!');
      onClose();
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to add stay');
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 650 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h3 style={{ margin: 0 }}>🏨 Add Stay</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
          <div className="input-group">
            <label>Type</label>
            <select className="input" value={form.type} onChange={e => setForm({ ...form, type: e.target.value as StayType })}>
              <option value="HOTEL">🏨 Hotel</option>
              <option value="HOSTEL">🛏️ Hostel</option>
              <option value="HOMESTAY">🏡 Homestay</option>
              <option value="AIRBNB">🏠 Airbnb</option>
              <option value="RESORT">🏖️ Resort</option>
              <option value="APARTMENT">🏢 Apartment</option>
              <option value="OTHER">🏛️ Other</option>
            </select>
          </div>
          <div className="input-group">
            <label>Name *</label>
            <input className="input" placeholder="e.g. Marriott Downtown" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
          </div>
          <div className="input-group">
            <label>City</label>
            <input className="input" placeholder="e.g. Paris" value={form.city} onChange={e => setForm({ ...form, city: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Address</label>
            <input className="input" placeholder="Street address" value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Check-In Date *</label>
            <input className="input" type="date" value={form.checkInDate} onChange={e => setForm({ ...form, checkInDate: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Check-Out Date *</label>
            <input className="input" type="date" value={form.checkOutDate} onChange={e => setForm({ ...form, checkOutDate: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Check-In Time</label>
            <input className="input" type="time" value={form.checkInTime} onChange={e => setForm({ ...form, checkInTime: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Check-Out Time</label>
            <input className="input" type="time" value={form.checkOutTime} onChange={e => setForm({ ...form, checkOutTime: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Cost Per Night ($)</label>
            <input className="input" type="number" placeholder="0.00" value={form.costPerNight ?? ''} onChange={e => setForm({ ...form, costPerNight: e.target.value ? parseFloat(e.target.value) : undefined })} />
          </div>
          <div className="input-group">
            <label>Total Cost ($)</label>
            <input className="input" type="number" placeholder="0.00" value={form.totalCost ?? ''} onChange={e => setForm({ ...form, totalCost: e.target.value ? parseFloat(e.target.value) : undefined })} />
          </div>
          <div className="input-group">
            <label>Star Rating</label>
            <select className="input" value={form.starRating} onChange={e => setForm({ ...form, starRating: parseInt(e.target.value) })}>
              <option value="0">Not rated</option>
              <option value="1">⭐ 1 Star</option>
              <option value="2">⭐⭐ 2 Stars</option>
              <option value="3">⭐⭐⭐ 3 Stars</option>
              <option value="4">⭐⭐⭐⭐ 4 Stars</option>
              <option value="5">⭐⭐⭐⭐⭐ 5 Stars</option>
            </select>
          </div>
          <div className="input-group">
            <label>Booking Reference</label>
            <input className="input" placeholder="Optional" value={form.bookingReference} onChange={e => setForm({ ...form, bookingReference: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Contact Phone</label>
            <input className="input" placeholder="+1-234-567-8900" value={form.contactPhone} onChange={e => setForm({ ...form, contactPhone: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Contact Email</label>
            <input className="input" placeholder="hotel@example.com" value={form.contactEmail} onChange={e => setForm({ ...form, contactEmail: e.target.value })} />
          </div>
          <div className="input-group" style={{ gridColumn: '1 / -1' }}>
            <label>Amenities</label>
            <input className="input" placeholder="WiFi, Pool, Breakfast, Parking..." value={form.amenities} onChange={e => setForm({ ...form, amenities: e.target.value })} />
          </div>
          <div className="input-group" style={{ gridColumn: '1 / -1' }}>
            <label>Cancellation Policy</label>
            <textarea className="input" rows={2} placeholder="e.g. Free cancellation until 48h before check-in" value={form.cancellationPolicy} onChange={e => setForm({ ...form, cancellationPolicy: e.target.value })} />
          </div>
          <div className="input-group" style={{ gridColumn: '1 / -1' }}>
            <label>Notes</label>
            <textarea className="input" rows={2} placeholder="Any additional notes" value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 20 }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Add Stay'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Calendar View ──────────────────────────────────────────────────────
function CalendarView({ stays, tripStart, tripEnd }: { stays: StayResponse[]; tripStart: string; tripEnd: string }) {
  const days = useMemo(() => {
    try {
      return eachDayOfInterval({ start: parseISO(tripStart), end: parseISO(tripEnd) });
    } catch { return []; }
  }, [tripStart, tripEnd]);

  const getStayForDate = (date: Date): StayResponse | null => {
    return stays.find(s => {
      const start = parseISO(s.checkInDate);
      const end = addDays(parseISO(s.checkOutDate), -1); // checkout day is not a "night"
      return isWithinInterval(date, { start, end });
    }) || null;
  };

  if (days.length === 0) return <div className="empty-state"><p>Set trip dates to see the calendar</p></div>;

  return (
    <div className="stay-calendar">
      <div className="calendar-header-row">
        <div className="cal-head">Date</div>
        <div className="cal-head">Day</div>
        <div className="cal-head" style={{ flex: 3 }}>Accommodation</div>
      </div>
      {days.map((day, i) => {
        const stay = getStayForDate(day);
        const meta = stay ? TYPE_META[stay.type] : null;
        const isCheckIn = stay && format(day, 'yyyy-MM-dd') === stay.checkInDate;
        const isCheckOut = stays.some(s => format(day, 'yyyy-MM-dd') === s.checkOutDate);

        return (
          <div key={i} className={`calendar-row ${stay ? 'cal-booked' : 'cal-empty'} ${isCheckIn ? 'cal-checkin' : ''}`}>
            <div className="cal-date">
              <span style={{ fontWeight: 600 }}>{format(day, 'd')}</span>
              <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>{format(day, 'MMM')}</span>
            </div>
            <div className="cal-day">{format(day, 'EEE')}</div>
            <div className="cal-stay" style={{ flex: 3 }}>
              {stay ? (
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <div className="segment-type-badge" style={{ background: meta!.color + '20', color: meta!.color, padding: '3px 8px', fontSize: 11 }}>
                    {meta!.icon}
                  </div>
                  <span style={{ fontWeight: 500, fontSize: 13 }}>{stay.name}</span>
                  {isCheckIn && <span style={{ fontSize: 10, color: 'var(--success)', fontWeight: 600 }}>CHECK-IN</span>}
                  {isCheckOut && <span style={{ fontSize: 10, color: 'var(--warning)', fontWeight: 600 }}>CHECK-OUT</span>}
                </div>
              ) : (
                <span style={{ fontSize: 12, color: 'var(--text-muted)', fontStyle: 'italic' }}>
                  {isCheckOut ? '🔑 Check-out day' : 'No accommodation'}
                </span>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}

// ── Main Page ──────────────────────────────────────────────────────────
export default function StaysPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();

  const { stays, fetchStays, deleteStay, isLoading } = useStayStore();
  const { currentTrip, fetchTrip } = useTripStore();

  const [tab, setTab] = useState<'list' | 'calendar'>('list');
  const [showAdd, setShowAdd] = useState(false);

  useEffect(() => {
    fetchStays(tripId);
    if (!currentTrip || currentTrip.id !== tripId) fetchTrip(tripId);
  }, [tripId]);

  const handleDelete = async (stayId: number) => {
    try {
      await deleteStay(stayId, tripId);
      toast.success('Stay removed');
    } catch { toast.error('Failed to remove'); }
  };

  const totalCost = stays.reduce((sum, s) => sum + (s.totalCost || 0), 0);
  const totalNights = stays.reduce((sum, s) => sum + s.totalNights, 0);

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      {/* Header */}
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #8b5cf6, #6366f1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Building2 size={22} color="white" />
          </div>
          <div>
            <h2>Stays & Lodging</h2>
            <p>{currentTrip?.title || `Trip #${tripId}`} · {stays.length} stay{stays.length !== 1 ? 's' : ''}</p>
          </div>
        </div>
      </div>

      {/* Stats bar */}
      <div className="itinerary-stats-bar" style={{ marginBottom: 24 }}>
        <div className="i-stat"><Building2 size={15} style={{ color: '#6366f1' }} /><span className="i-stat-val">{stays.length}</span><span className="i-stat-label">stays</span></div>
        <div className="i-stat"><Moon size={15} style={{ color: '#8b5cf6' }} /><span className="i-stat-val">{totalNights}</span><span className="i-stat-label">nights</span></div>
        <div className="i-stat"><DollarSign size={15} style={{ color: '#22c55e' }} /><span className="i-stat-val">${totalCost.toFixed(0)}</span><span className="i-stat-label">total cost</span></div>
      </div>

      {/* Tab Bar */}
      <div className="tab-row">
        <button className={`tab-btn ${tab === 'list' ? 'active' : ''}`} onClick={() => setTab('list')}>
          <List size={14} style={{ marginRight: 6 }} /> List View ({stays.length})
        </button>
        <button className={`tab-btn ${tab === 'calendar' ? 'active' : ''}`} onClick={() => setTab('calendar')}>
          <CalendarDays size={14} style={{ marginRight: 6 }} /> Calendar View
        </button>
      </div>

      {/* TAB: List */}
      {tab === 'list' && (
        <div className="animate-in">
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
            <button className="btn btn-primary btn-sm" onClick={() => setShowAdd(true)}>
              <Plus size={14} /> Add Stay
            </button>
          </div>

          {isLoading ? <div className="spinner" /> : stays.length === 0 ? (
            <div className="empty-state">
              <Building2 size={48} />
              <h3>No stays added yet</h3>
              <p>Add your hotels, hostels, or Airbnb bookings to keep everything organized.</p>
              <button className="btn btn-primary" onClick={() => setShowAdd(true)}>
                <Plus size={16} /> Add Your First Stay
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {stays.map(stay => (
                <StayCard key={stay.id} stay={stay} onDelete={() => handleDelete(stay.id)} />
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB: Calendar */}
      {tab === 'calendar' && (
        <div className="animate-in">
          {currentTrip ? (
            <CalendarView stays={stays} tripStart={currentTrip.startDate} tripEnd={currentTrip.endDate} />
          ) : (
            <div className="spinner" />
          )}
        </div>
      )}

      {/* Add Modal */}
      {showAdd && <AddStayModal tripId={tripId} onClose={() => setShowAdd(false)} />}
    </div>
  );
}
