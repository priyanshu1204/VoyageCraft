import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTripStore } from '../store/tripStore';
import { tripApi } from '../api/tripApi';
import { MapPin, Calendar, DollarSign, PlusCircle, ArrowRight, Bell, Check, X } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import type { CollaboratorResponse } from '../types/trip';

export default function DashboardPage() {
  const { dashboard, fetchDashboard, isLoading } = useTripStore();
  const navigate = useNavigate();
  const [invitations, setInvitations] = useState<CollaboratorResponse[]>([]);
  const [respondingId, setRespondingId] = useState<number | null>(null);

  useEffect(() => { fetchDashboard(); loadInvitations(); }, [fetchDashboard]);

  const loadInvitations = async () => {
    try {
      const res = await tripApi.getPendingInvitations();
      setInvitations(res.data.data || []);
    } catch { /* ignore */ }
  };

  const handleRespond = async (inv: CollaboratorResponse, accept: boolean) => {
    if (!inv.tripId) return;
    setRespondingId(inv.id);
    try {
      await tripApi.respondToInvitation(inv.tripId, inv.id, accept);
      toast.success(accept ? 'Invitation accepted! Trip added to your list.' : 'Invitation declined.');
      loadInvitations();
      if (accept) fetchDashboard();
    } catch { toast.error('Failed to respond'); }
    finally { setRespondingId(null); }
  };

  if (isLoading && !dashboard) return <div className="spinner" />;

  return (
    <div className="animate-in">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2>Dashboard</h2>
          <p>Welcome back! Here's your travel overview.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/trips/create')} id="dashboard-create-trip">
          <PlusCircle size={18} /> New Trip
        </button>
      </div>

      {/* Pending Invitations Banner */}
      {invitations.length > 0 && (
        <div className="animate-in" style={{ marginBottom: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
            <Bell size={18} style={{ color: '#f59e0b' }} />
            <h3 style={{ fontSize: 16, fontWeight: 600, margin: 0 }}>
              Pending Invitations ({invitations.length})
            </h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {invitations.map(inv => (
              <div key={inv.id} style={{
                padding: '14px 18px',
                background: 'linear-gradient(135deg, rgba(99,102,241,0.08), rgba(139,92,246,0.08))',
                border: '1px solid rgba(99,102,241,0.2)',
                borderRadius: 'var(--radius-md)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 12,
              }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>
                    🗺️ {inv.tripTitle || `Trip #${inv.tripId}`}
                  </div>
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>
                    Invited by <strong>{inv.invitedByName || 'Unknown'}</strong> as{' '}
                    <span className="badge badge-indigo" style={{ fontSize: 10, padding: '1px 6px' }}>{inv.role}</span>
                    {inv.invitedAt && <> · {format(new Date(inv.invitedAt), 'MMM d, yyyy')}</>}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => handleRespond(inv, true)}
                    disabled={respondingId === inv.id}
                  >
                    <Check size={14} /> Accept
                  </button>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => handleRespond(inv, false)}
                    disabled={respondingId === inv.id}
                  >
                    <X size={14} /> Decline
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="stats-grid animate-in animate-in-delay-1">
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(99,102,241,0.15)' }}>
            <MapPin size={20} style={{ color: '#6366f1' }} />
          </div>
          <div className="stat-label">Total Trips</div>
          <div className="stat-value">{dashboard?.totalTrips ?? 0}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(59,130,246,0.15)' }}>
            <Calendar size={20} style={{ color: '#3b82f6' }} />
          </div>
          <div className="stat-label">Planning</div>
          <div className="stat-value">{dashboard?.planningTrips ?? 0}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(34,197,94,0.15)' }}>
            <MapPin size={20} style={{ color: '#22c55e' }} />
          </div>
          <div className="stat-label">Active</div>
          <div className="stat-value">{dashboard?.activeTrips ?? 0}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(148,163,184,0.15)' }}>
            <DollarSign size={20} style={{ color: '#94a3b8' }} />
          </div>
          <div className="stat-label">Completed</div>
          <div className="stat-value">{dashboard?.completedTrips ?? 0}</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        {/* Upcoming Trips */}
        <div className="card animate-in animate-in-delay-2">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 600 }}>Upcoming Trips</h3>
            <button className="btn btn-ghost btn-sm" onClick={() => navigate('/trips')}>
              View all <ArrowRight size={14} />
            </button>
          </div>
          {(!dashboard?.upcomingTrips || dashboard.upcomingTrips.length === 0) ? (
            <div className="empty-state" style={{ padding: '20px' }}>
              <p>No upcoming trips yet</p>
              <button className="btn btn-primary btn-sm" onClick={() => navigate('/trips/create')}>Plan one now</button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {dashboard.upcomingTrips.map((trip) => (
                <div key={trip.id} className="card" style={{ padding: '14px', cursor: 'pointer' }}
                  onClick={() => navigate(`/trips/${trip.id}`)}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '14px' }}>{trip.title}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Calendar size={12} />
                        {format(new Date(trip.startDate), 'MMM d')} - {format(new Date(trip.endDate), 'MMM d, yyyy')}
                      </div>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span className="badge badge-indigo">
                        {trip.daysUntilTrip > 0 ? `${trip.daysUntilTrip}d away` : 'Today!'}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Trips */}
        <div className="card animate-in animate-in-delay-3">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 600 }}>Recent Trips</h3>
          </div>
          {(!dashboard?.recentTrips || dashboard.recentTrips.length === 0) ? (
            <div className="empty-state" style={{ padding: '20px' }}>
              <p>No trips created yet</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {dashboard.recentTrips.map((trip) => (
                <div key={trip.id} className="card" style={{ padding: '14px', cursor: 'pointer' }}
                  onClick={() => navigate(`/trips/${trip.id}`)}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '14px' }}>{trip.title}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
                        {trip.destinations.length} destination{trip.destinations.length !== 1 ? 's' : ''} · {trip.totalDays} days
                      </div>
                    </div>
                    <span className={`trip-status status-${trip.status.toLowerCase()}`}
                      style={{ padding: '3px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: 600 }}>
                      {trip.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
