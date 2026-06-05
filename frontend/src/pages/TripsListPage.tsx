import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTripStore } from '../store/tripStore';
import { PlusCircle, MapPin, Calendar, DollarSign, Users } from 'lucide-react';
import { format } from 'date-fns';

// Curated travel-themed background images for trip cards
const mockImages = [
  'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&h=300&fit=crop', // Beach
  'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&h=300&fit=crop', // Mountains
  'https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=600&h=300&fit=crop', // Hill Station
  'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600&h=300&fit=crop', // Train
  'https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=600&h=300&fit=crop', // City Skyline
  'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=600&h=300&fit=crop', // Lake & Nature
  'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600&h=300&fit=crop', // Road Trip
  'https://images.unsplash.com/photo-1436491865332-7a61a109db05?w=600&h=300&fit=crop', // Desert
];

const getCardBackground = (_trip: any, index: number): React.CSSProperties => {
  return {
    backgroundImage: `linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.5) 100%), url(${mockImages[index % mockImages.length]})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  };
};

export default function TripsListPage() {
  const { trips, fetchTrips, isLoading } = useTripStore();
  const navigate = useNavigate();

  useEffect(() => { fetchTrips(); }, [fetchTrips]);

  if (isLoading && trips.length === 0) return <div className="spinner" />;

  return (
    <div className="animate-in">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2>My Trips</h2>
          <p>{trips.length} trip{trips.length !== 1 ? 's' : ''} planned</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/trips/create')} id="trips-create-btn">
          <PlusCircle size={18} /> New Trip
        </button>
      </div>

      {trips.length === 0 ? (
        <div className="empty-state">
          <MapPin size={64} style={{ color: 'var(--accent-primary)' }} />
          <h3>No trips yet</h3>
          <p>Start planning your first adventure!</p>
          <button className="btn btn-primary" onClick={() => navigate('/trips/create')}>
            <PlusCircle size={18} /> Create Your First Trip
          </button>
        </div>
      ) : (
        <div className="trips-grid">
          {trips.map((trip, i) => (
            <div key={trip.id} className="trip-card" onClick={() => navigate(`/trips/${trip.id}`)}>
              <div className="trip-card-banner" style={getCardBackground(trip, i)}>
                <span className={`trip-status status-${trip.status.toLowerCase()}`}>{trip.status}</span>
                <div style={{ color: 'white', fontWeight: 700, fontSize: '12px', textShadow: '0 1px 3px rgba(0,0,0,0.3)' }}>
                  {trip.destinations.map(d => d.destinationName).join(' → ') || 'No destinations'}
                </div>
              </div>
              <div className="trip-card-body">
                <h3>{trip.title}</h3>
                {trip.description && <p>{trip.description.substring(0, 100)}{trip.description.length > 100 ? '...' : ''}</p>}
                <div className="trip-card-meta">
                  <div className="trip-meta-item">
                    <Calendar size={14} />
                    {format(new Date(trip.startDate), 'MMM d')} - {format(new Date(trip.endDate), 'MMM d')}
                  </div>
                  <div className="trip-meta-item">
                    <MapPin size={14} />
                    {trip.destinations.length} dest.
                  </div>
                  {trip.budgetTotal && (
                    <div className="trip-meta-item">
                      <DollarSign size={14} />
                      {trip.currency} {trip.budgetTotal.toLocaleString()}
                    </div>
                  )}
                  <div className="trip-meta-item">
                    <Users size={14} />
                    {trip.collaborators.length}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
