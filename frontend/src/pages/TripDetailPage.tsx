import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTripStore } from '../store/tripStore';
import { useAuthStore } from '../store/authStore';
import { tripApi } from '../api/tripApi';
import { CollaboratorRole } from '../types/trip';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { MapPin, Calendar, DollarSign, Users, Trash2, UserPlus, ArrowLeft, Clock, Zap, Sparkles, Plane, Building2, Compass, Wallet, Vote, Package, Download, Sun, Shield, Navigation, Bell, BarChart3 } from 'lucide-react';
import { useOfflineStore } from '../store/offlineStore';
import { useNotificationStore } from '../store/notificationStore';

export default function TripDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { currentTrip, fetchTrip, deleteTrip, isLoading } = useTripStore();
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [showInvite, setShowInvite] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<CollaboratorRole>('VIEWER');
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => { if (id) fetchTrip(parseInt(id)); }, [id, fetchTrip]);

  const handleDelete = async () => {
    if (!confirmDelete) { setConfirmDelete(true); return; }
    if (!id || deleting) return;
    setDeleting(true);
    try {
      await deleteTrip(parseInt(id));
      toast.success('Trip deleted');
      navigate('/trips');
    } catch {
      toast.error('Failed to delete');
      setDeleting(false);
      setConfirmDelete(false);
    }
  };

  const handleInvite = async () => {
    if (!id || !inviteEmail) return;
    try {
      await tripApi.inviteCollaborator(parseInt(id), { email: inviteEmail, role: inviteRole });
      toast.success('Invitation sent!'); setInviteEmail(''); setShowInvite(false);
      fetchTrip(parseInt(id));
    } catch (err: any) { toast.error(err.response?.data?.message || 'Failed to invite'); }
  };

  const handleRemoveCollab = async (collabId: number) => {
    if (!id || !confirm('Remove this collaborator?')) return;
    try { await tripApi.removeCollaborator(parseInt(id), collabId); toast.success('Removed'); fetchTrip(parseInt(id)); } catch { toast.error('Failed'); }
  };

  if (isLoading && !currentTrip) return <div className="spinner" />;
  if (!currentTrip) return <div className="empty-state"><h3>Trip not found</h3></div>;

  const t = currentTrip;
  const isOwner = user?.userId === t.createdBy.id;
  const isEditor = t.collaborators.some(
    c => c.user.id === user?.userId && c.role === 'EDITOR' && c.invitationStatus === 'ACCEPTED'
  );
  const canEdit = isOwner || isEditor;

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate('/trips')} style={{ marginBottom: 16 }}><ArrowLeft size={18} /> Back to Trips</button>

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 32 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
            <h2 style={{ fontSize: 28, fontWeight: 700 }}>{t.title}</h2>
            <span className={`trip-status status-${t.status.toLowerCase()}`} style={{ padding: '4px 12px', borderRadius: 20, fontSize: 11, fontWeight: 600 }}>{t.status}</span>
            {isEditor && <span className="badge badge-green" style={{ fontSize: 10, padding: '2px 8px' }}>EDITOR</span>}
          </div>
          {t.description && <p style={{ color: 'var(--text-secondary)', maxWidth: 600 }}>{t.description}</p>}
        </div>
        {canEdit && (
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <button className="btn btn-primary btn-sm" onClick={() => navigate(`/trips/${id}/itinerary`)}>
              <Sparkles size={14} /> Plan Itinerary
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/transport`)}>
              <Plane size={14} /> Transport
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/stays`)}>
              <Building2 size={14} /> Stays
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/activities`)}>
              <Compass size={14} /> Activities
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/budget`)}>
              <Wallet size={14} /> Budget
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/collaborate`)}>
              <Vote size={14} /> Collaborate
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/packing`)}>
              <Package size={14} /> Packing
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/weather`)}>
              <Sun size={14} /> Weather
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/documents`)}>
              <Shield size={14} /> Visa & Docs
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/navigation`)}>
              <Navigation size={14} /> Navigate
            </button>
            <button className="btn btn-secondary btn-sm" onClick={async () => { try { await useNotificationStore.getState().generateAlerts(parseInt(id!)); toast.success('Alerts generated!'); navigate(`/trips/${id}/alerts`); } catch { toast.error('Failed'); } }}>
              <Bell size={14} /> Alerts
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/analytics`)}>
              <BarChart3 size={14} /> Analytics
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/trips/${id}/quick-actions`)}>
              <Zap size={14} /> Quick Actions
            </button>
            <button className="btn btn-secondary btn-sm" onClick={async () => { try { await useOfflineStore.getState().downloadTrip(parseInt(id!)); toast.success('Trip saved for offline!'); } catch { toast.error('Failed'); } }}>
              <Download size={14} /> Save Offline
            </button>
            {isOwner && !confirmDelete && <button className="btn btn-danger btn-sm" onClick={handleDelete} disabled={deleting}><Trash2 size={16} /> Delete</button>}
            {isOwner && confirmDelete && <>
              <button className="btn btn-danger btn-sm" onClick={handleDelete} disabled={deleting}>{deleting ? 'Deleting...' : 'Confirm Delete?'}</button>
              <button className="btn btn-secondary btn-sm" onClick={() => setConfirmDelete(false)} disabled={deleting}>Cancel</button>
            </>}
          </div>
        )}
      </div>

      {/* Stats */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
        <div className="stat-card"><div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}><Calendar size={18} style={{ color: 'var(--accent-primary)' }} /><span style={{ fontSize: 13, color: 'var(--text-muted)' }}>Dates</span></div><div style={{ fontWeight: 600 }}>{format(new Date(t.startDate), 'MMM d')} - {format(new Date(t.endDate), 'MMM d, yyyy')}</div></div>
        <div className="stat-card"><div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}><Clock size={18} style={{ color: '#3b82f6' }} /><span style={{ fontSize: 13, color: 'var(--text-muted)' }}>Duration</span></div><div style={{ fontWeight: 600 }}>{t.totalDays} days</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{t.daysUntilTrip > 0 ? `${t.daysUntilTrip} days away` : t.daysUntilTrip === 0 ? 'Today!' : 'Past'}</div></div>
        <div className="stat-card"><div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}><DollarSign size={18} style={{ color: '#22c55e' }} /><span style={{ fontSize: 13, color: 'var(--text-muted)' }}>Budget</span></div><div style={{ fontWeight: 600 }}>{t.budgetTotal ? `${t.currency} ${t.budgetTotal.toLocaleString()}` : 'Not set'}</div></div>
        <div className="stat-card"><div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}><Zap size={18} style={{ color: '#f59e0b' }} /><span style={{ fontSize: 13, color: 'var(--text-muted)' }}>Pace</span></div><div style={{ fontWeight: 600 }}>{t.pace}</div></div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        {/* Destinations */}
        <div className="card">
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8 }}><MapPin size={18} style={{ color: 'var(--accent-primary)' }} /> Destinations ({t.destinations.length})</h3>
          {t.destinations.length === 0 ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>No destinations added yet</p> : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {t.destinations.map((d, i) => (
                <div key={d.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 14px', background: 'var(--bg-input)', borderRadius: 'var(--radius-sm)' }}>
                  <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--accent-gradient)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, color: 'white', flexShrink: 0 }}>{i + 1}</div>
                  <div style={{ flex: 1 }}><div style={{ fontWeight: 600, fontSize: 14 }}>{d.destinationName}</div>{d.country && <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{d.country}</div>}</div>
                  {i < t.destinations.length - 1 && <div style={{ color: 'var(--text-muted)', fontSize: 12 }}>→</div>}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Collaborators */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h3 style={{ fontSize: 16, fontWeight: 600, display: 'flex', alignItems: 'center', gap: 8 }}><Users size={18} style={{ color: 'var(--accent-primary)' }} /> Team ({t.collaborators.length})</h3>
            {isOwner && <button className="btn btn-secondary btn-sm" onClick={() => setShowInvite(!showInvite)}><UserPlus size={14} /> Invite</button>}
          </div>

          {showInvite && (
            <div style={{ display: 'flex', gap: 8, marginBottom: 16, padding: 12, background: 'var(--bg-input)', borderRadius: 'var(--radius-sm)' }}>
              <input className="input" placeholder="Email" value={inviteEmail} onChange={e => setInviteEmail(e.target.value)} style={{ flex: 1 }} />
              <select className="input" value={inviteRole} onChange={e => setInviteRole(e.target.value as CollaboratorRole)} style={{ width: 110 }}>
                <option value="VIEWER">Viewer</option><option value="EDITOR">Editor</option>
              </select>
              <button className="btn btn-primary btn-sm" onClick={handleInvite}>Send</button>
            </div>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {t.collaborators.map(c => (
              <div key={c.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 14px', background: 'var(--bg-input)', borderRadius: 'var(--radius-sm)' }}>
                <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--accent-gradient)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, color: 'white' }}>{c.user.firstName[0]}{c.user.lastName[0]}</div>
                <div style={{ flex: 1 }}><div style={{ fontWeight: 600, fontSize: 14 }}>{c.user.firstName} {c.user.lastName}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{c.user.email}</div></div>
                <span className={`badge ${c.role === 'OWNER' ? 'badge-yellow' : c.role === 'EDITOR' ? 'badge-green' : 'badge-indigo'}`}>{c.role}</span>
                <span className={`badge ${c.invitationStatus === 'ACCEPTED' ? 'badge-green' : 'badge-yellow'}`}>{c.invitationStatus}</span>
                {isOwner && c.role !== 'OWNER' && <button className="btn btn-ghost btn-sm" onClick={() => handleRemoveCollab(c.id)}><Trash2 size={14} style={{ color: 'var(--danger)' }} /></button>}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
