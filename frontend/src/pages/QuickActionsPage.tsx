import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuickActionStore } from '../store/quickActionStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, Zap, Plane, Building2, Clock, GripVertical, StickyNote, Camera,
  MapPin, Plus, Trash2, Pin, Bell, CheckCircle, Circle, ChevronUp, ChevronDown,
  ImageIcon,
} from 'lucide-react';

export default function QuickActionsPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();
  const {
    dashboard, notes, isLoading, fetchDashboard, toggleTransportCheckIn,
    toggleStayCheckIn, createNote, deleteNote,
  } = useQuickActionStore();

  const [activeTab, setActiveTab] = useState<'today' | 'checkin' | 'notes'>('today');
  const [showNoteForm, setShowNoteForm] = useState(false);
  const [noteTitle, setNoteTitle] = useState('');
  const [noteContent, setNoteContent] = useState('');
  const [notePhotos, setNotePhotos] = useState<string[]>([]);
  const [noteLocation, setNoteLocation] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => { fetchDashboard(tripId); }, [tripId]);

  // Request push permission on mount
  useEffect(() => {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission().then(p => {
        if (p === 'granted') toast.success('Push notifications enabled!');
      });
    }
  }, []);

  const handlePhotoCapture = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;
    Array.from(files).forEach(file => {
      const reader = new FileReader();
      reader.onload = () => {
        setNotePhotos(prev => [...prev, reader.result as string]);
      };
      reader.readAsDataURL(file);
    });
  };

  const handleCreateNote = async () => {
    if (!noteTitle.trim()) { toast.error('Title is required'); return; }
    try {
      await createNote({
        tripId,
        title: noteTitle.trim(),
        content: noteContent.trim() || undefined,
        photoUrls: notePhotos.length > 0 ? notePhotos.join('|||') : undefined,
        capturedLocationName: noteLocation || undefined,
        isSynced: navigator.onLine,
      });
      // Save to localStorage if offline
      if (!navigator.onLine) {
        const offlineNotes = JSON.parse(localStorage.getItem(`offline_notes_${tripId}`) || '[]');
        offlineNotes.push({ tripId, title: noteTitle, content: noteContent, photoUrls: notePhotos.join('|||'), capturedLocationName: noteLocation, timestamp: Date.now() });
        localStorage.setItem(`offline_notes_${tripId}`, JSON.stringify(offlineNotes));
      }
      toast.success('Note saved!');
      setNoteTitle(''); setNoteContent(''); setNotePhotos([]); setNoteLocation('');
      setShowNoteForm(false);
    } catch { toast.error('Failed to save note'); }
  };

  const handleCheckIn = async (item: { id: number; type: string; checkedIn: boolean }) => {
    try {
      if (item.type === 'FLIGHT') await toggleTransportCheckIn(item.id);
      else await toggleStayCheckIn(item.id);
      toast.success(item.checkedIn ? 'Checked out!' : 'Checked in! ✈️');

      // Fire push notification
      if ('Notification' in window && Notification.permission === 'granted') {
        new Notification('VoyageCraft', {
          body: item.checkedIn ? `Checked out successfully` : `You're checked in! Have a great trip!`,
          icon: '/vite.svg',
        });
      }
    } catch { toast.error('Check-in failed'); }
  };

  if (isLoading) return <div className="animate-in" style={{ textAlign: 'center', padding: 60 }}><div className="spinner" /></div>;

  const d = dashboard;

  return (
    <div className="animate-in quick-actions-page">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 12 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      {/* Header */}
      <div className="page-header" style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #f59e0b, #ef4444)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Zap size={22} color="white" />
          </div>
          <div>
            <h2 style={{ margin: 0 }}>Quick Actions</h2>
            <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: 13 }}>{d?.tripTitle || 'Trip'} · Mobile Dashboard</p>
          </div>
        </div>
        {d && d.unreadNotifications > 0 && (
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/notifications')} style={{ position: 'relative' }}>
            <Bell size={14} />
            <span style={{ position: 'absolute', top: -4, right: -4, background: '#ef4444', color: 'white', borderRadius: '50%', width: 18, height: 18, fontSize: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>
              {d.unreadNotifications}
            </span>
          </button>
        )}
      </div>

      {/* Tab Switcher */}
      <div style={{ display: 'flex', gap: 6, marginBottom: 20, background: 'var(--bg-input)', borderRadius: 'var(--radius-md)', padding: 4 }}>
        {([
          { key: 'today', icon: <Clock size={14} />, label: "Today's Plan" },
          { key: 'checkin', icon: <Plane size={14} />, label: 'Check-In' },
          { key: 'notes', icon: <StickyNote size={14} />, label: 'Notes' },
        ] as const).map(tab => (
          <button key={tab.key}
            className={`btn btn-sm ${activeTab === tab.key ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setActiveTab(tab.key)}
            style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6 }}>
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* ── TODAY'S PLAN ── */}
      {activeTab === 'today' && (
        <div className="card">
          <h3 style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
            <Clock size={18} style={{ color: '#6366f1' }} /> Today's Schedule
          </h3>
          {d && d.todaySchedule.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {d.todaySchedule.map((item, i) => (
                <div key={`${item.type}-${item.id}`}
                  className="qa-schedule-item"
                  style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', cursor: 'grab' }}>
                  <GripVertical size={16} style={{ color: 'var(--text-muted)', flexShrink: 0 }} />
                  <div style={{ width: 8, height: 8, borderRadius: '50%', background: item.type === 'TRANSPORT' ? '#f59e0b' : item.type === 'ACTIVITY' ? '#22c55e' : '#6366f1', flexShrink: 0 }} />
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{item.title}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)', display: 'flex', gap: 8, marginTop: 2 }}>
                      {item.startTime && <span>🕐 {item.startTime.slice(0, 5)}{item.endTime ? ` - ${item.endTime.slice(0, 5)}` : ''}</span>}
                      {item.location && <span>📍 {item.location}</span>}
                    </div>
                  </div>
                  <span style={{ fontSize: 10, padding: '2px 8px', borderRadius: 12, background: item.type === 'TRANSPORT' ? 'rgba(245,158,11,0.15)' : item.type === 'ACTIVITY' ? 'rgba(34,197,94,0.15)' : 'rgba(99,102,241,0.15)', color: item.type === 'TRANSPORT' ? '#f59e0b' : item.type === 'ACTIVITY' ? '#22c55e' : '#6366f1', fontWeight: 600 }}>
                    {item.type === 'TRANSPORT' ? '✈️' : item.type === 'ACTIVITY' ? '🎯' : '📋'} {item.category || item.type}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state" style={{ padding: 32 }}>
              <Clock size={36} style={{ color: 'var(--text-muted)' }} />
              <p>No activities scheduled for today. Add items to your itinerary to see them here.</p>
            </div>
          )}
        </div>
      )}

      {/* ── CHECK-IN SHORTCUTS ── */}
      {activeTab === 'checkin' && (
        <div className="card">
          <h3 style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
            <Plane size={18} style={{ color: '#f59e0b' }} /> Upcoming Check-Ins
          </h3>
          {d && d.upcomingCheckIns.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {d.upcomingCheckIns.map(item => (
                <div key={`${item.type}-${item.id}`}
                  style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '14px 16px', background: item.checkedIn ? 'rgba(34,197,94,0.08)' : 'var(--bg-main)', borderRadius: 'var(--radius-md)', border: item.checkedIn ? '1px solid rgba(34,197,94,0.3)' : '1px solid var(--border-subtle)', transition: 'all 0.3s ease' }}>
                  {/* Type Icon */}
                  <div style={{ width: 40, height: 40, borderRadius: 10, background: item.type === 'FLIGHT' ? 'linear-gradient(135deg, #3b82f6, #6366f1)' : 'linear-gradient(135deg, #f59e0b, #ef4444)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    {item.type === 'FLIGHT' ? <Plane size={18} color="white" /> : <Building2 size={18} color="white" />}
                  </div>
                  {/* Info */}
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{item.title}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)', display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 2 }}>
                      {item.subtitle && <span>{item.subtitle}</span>}
                      {item.bookingReference && <span>🔖 {item.bookingReference}</span>}
                      <span>📅 {new Date(item.dateTime).toLocaleDateString()}</span>
                    </div>
                  </div>
                  {/* One-Tap Check-In */}
                  <button
                    className={`btn btn-sm ${item.checkedIn ? 'btn-secondary' : 'btn-primary'}`}
                    onClick={() => handleCheckIn(item)}
                    style={{ display: 'flex', alignItems: 'center', gap: 6, minWidth: 100, justifyContent: 'center' }}>
                    {item.checkedIn ? <><CheckCircle size={14} /> Checked In</> : <><Circle size={14} /> Check In</>}
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state" style={{ padding: 32 }}>
              <Plane size={36} style={{ color: 'var(--text-muted)' }} />
              <p>No upcoming flights or stays within the next 3 days.</p>
            </div>
          )}
        </div>
      )}

      {/* ── NOTES (with offline + photos) ── */}
      {activeTab === 'notes' && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: 8, margin: 0 }}>
              <StickyNote size={18} style={{ color: '#ec4899' }} /> Quick Notes
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setShowNoteForm(!showNoteForm)}>
              {showNoteForm ? <ChevronUp size={14} /> : <Plus size={14} />}
              {showNoteForm ? 'Close' : 'New Note'}
            </button>
          </div>

          {/* Note Form */}
          {showNoteForm && (
            <div style={{ padding: 16, background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', marginBottom: 16, border: '1px dashed var(--border-color)' }}>
              <input
                type="text" className="input" placeholder="Note title..."
                value={noteTitle} onChange={e => setNoteTitle(e.target.value)}
                style={{ marginBottom: 10, fontWeight: 600 }}
              />
              <textarea
                className="input" placeholder="Write your note..."
                value={noteContent} onChange={e => setNoteContent(e.target.value)}
                rows={3} style={{ marginBottom: 10, resize: 'vertical' }}
              />
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
                <MapPin size={14} style={{ color: 'var(--text-muted)' }} />
                <input
                  type="text" className="input" placeholder="Location name (optional)"
                  value={noteLocation} onChange={e => setNoteLocation(e.target.value)}
                  style={{ flex: 1 }}
                />
              </div>

              {/* Photo Capture */}
              <div style={{ marginBottom: 12 }}>
                <button className="btn btn-secondary btn-sm" onClick={() => fileInputRef.current?.click()}>
                  <Camera size={14} /> Add Photos
                </button>
                <input
                  ref={fileInputRef} type="file" accept="image/*" multiple capture="environment"
                  onChange={handlePhotoCapture} style={{ display: 'none' }}
                />
                {notePhotos.length > 0 && (
                  <div style={{ display: 'flex', gap: 8, marginTop: 10, flexWrap: 'wrap' }}>
                    {notePhotos.map((photo, i) => (
                      <div key={i} style={{ position: 'relative' }}>
                        <img src={photo} alt={`Photo ${i + 1}`} style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 8 }} />
                        <button
                          onClick={() => setNotePhotos(prev => prev.filter((_, idx) => idx !== i))}
                          style={{ position: 'absolute', top: -4, right: -4, background: '#ef4444', color: 'white', borderRadius: '50%', width: 18, height: 18, border: 'none', fontSize: 10, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          ×
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {!navigator.onLine && (
                <div style={{ padding: '6px 10px', background: 'rgba(245,158,11,0.15)', borderRadius: 8, fontSize: 12, color: '#f59e0b', marginBottom: 10 }}>
                  📴 You're offline — note will be saved locally and synced when online.
                </div>
              )}

              <button className="btn btn-primary btn-sm" onClick={handleCreateNote} style={{ width: '100%' }}>
                Save Note
              </button>
            </div>
          )}

          {/* Notes List */}
          {notes.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {notes.map(note => (
                <div key={note.id} style={{ padding: '12px 14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', borderLeft: note.isPinned ? '3px solid #f59e0b' : '3px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
                    <div style={{ fontWeight: 600, fontSize: 14, display: 'flex', alignItems: 'center', gap: 6 }}>
                      {note.isPinned && <Pin size={12} style={{ color: '#f59e0b' }} />}
                      {note.title}
                    </div>
                    <div style={{ display: 'flex', gap: 4 }}>
                      {!note.isSynced && <span style={{ fontSize: 10, color: '#f59e0b' }}>📴 offline</span>}
                      <button className="btn btn-ghost" style={{ padding: 2 }} onClick={async () => { try { await deleteNote(note.id); toast.success('Deleted'); } catch { toast.error('Failed'); } }}>
                        <Trash2 size={12} style={{ color: 'var(--text-muted)' }} />
                      </button>
                    </div>
                  </div>
                  {note.content && <p style={{ fontSize: 13, color: 'var(--text-muted)', margin: '4px 0' }}>{note.content}</p>}
                  {note.photoUrls && (
                    <div style={{ display: 'flex', gap: 6, marginTop: 8, flexWrap: 'wrap' }}>
                      {note.photoUrls.split('|||').filter(Boolean).map((url, i) => (
                        <img key={i} src={url} alt={`Photo ${i + 1}`} style={{ width: 50, height: 50, objectFit: 'cover', borderRadius: 6, border: '1px solid var(--border-subtle)' }} />
                      ))}
                    </div>
                  )}
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 6, display: 'flex', gap: 10 }}>
                    {note.capturedLocationName && <span>📍 {note.capturedLocationName}</span>}
                    <span>🕐 {new Date(note.createdAt).toLocaleString()}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state" style={{ padding: 32 }}>
              <StickyNote size={36} style={{ color: 'var(--text-muted)' }} />
              <p>No notes yet. Capture a quick thought or photo!</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
