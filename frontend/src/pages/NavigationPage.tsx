import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useNavigationStore } from '../store/navigationStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, Navigation, Plus, Trash2, Printer, MapPin, Clock, Route,
  Bus, Car, PersonStanding, Bike, X, RefreshCw, ArrowRight, FileText
} from 'lucide-react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet default icon issue
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const TRANSPORT_MODES = [
  { value: 'PUBLIC_TRANSPORT', label: 'Public Transport', icon: '🚌' },
  { value: 'RIDE_SHARE', label: 'Ride Share', icon: '🚗' },
  { value: 'TAXI', label: 'Taxi', icon: '🚕' },
  { value: 'DRIVING', label: 'Driving', icon: '🚙' },
  { value: 'WALKING', label: 'Walking', icon: '🚶' },
  { value: 'CYCLING', label: 'Cycling', icon: '🚴' },
];

function FitBounds({ positions }: { positions: [number, number][] }) {
  const map = useMap();
  useEffect(() => {
    if (positions.length > 0) {
      const bounds = L.latLngBounds(positions.map(p => L.latLng(p[0], p[1])));
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [positions, map]);
  return null;
}

export default function NavigationPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();
  const printRef = useRef<HTMLDivElement>(null);
  const { routes, daySheets, isLoading, fetchRoutes, addRoute, switchMode, deleteRoute, fetchDaySheets } = useNavigationStore();

  const [activeTab, setActiveTab] = useState<'map' | 'routes' | 'daysheets'>('map');
  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedDay, setSelectedDay] = useState<number>(1);

  // Form states
  const [fromLoc, setFromLoc] = useState('');
  const [fromLat, setFromLat] = useState('');
  const [fromLng, setFromLng] = useState('');
  const [toLoc, setToLoc] = useState('');
  const [toLat, setToLat] = useState('');
  const [toLng, setToLng] = useState('');
  const [mode, setMode] = useState('RIDE_SHARE');
  const [buffer, setBuffer] = useState('');
  const [directions, setDirections] = useState('');
  const [dayNum, setDayNum] = useState('1');

  useEffect(() => { fetchRoutes(tripId); fetchDaySheets(tripId); }, [tripId]);

  const handleAdd = async () => {
    if (!fromLoc.trim() || !toLoc.trim()) { toast.error('From and To locations required'); return; }
    try {
      await addRoute(tripId, {
        fromLocation: fromLoc, fromLatitude: fromLat ? parseFloat(fromLat) : null, fromLongitude: fromLng ? parseFloat(fromLng) : null,
        toLocation: toLoc, toLatitude: toLat ? parseFloat(toLat) : null, toLongitude: toLng ? parseFloat(toLng) : null,
        transportMode: mode, bufferMinutes: buffer ? parseInt(buffer) : undefined,
        directions, dayNumber: parseInt(dayNum),
      });
      await fetchDaySheets(tripId);
      toast.success('Route added!');
      setShowAddForm(false);
      setFromLoc(''); setFromLat(''); setFromLng(''); setToLoc(''); setToLat(''); setToLng(''); setDirections('');
    } catch { toast.error('Failed to add route'); }
  };

  const handlePrint = () => {
    const content = printRef.current;
    if (!content) return;
    const win = window.open('', '_blank');
    if (!win) return;
    win.document.write(`<html><head><title>Day Sheet - Day ${selectedDay}</title>
      <style>
        body { font-family: 'Segoe UI', sans-serif; padding: 24px; color: #1a1a2e; }
        h1 { font-size: 22px; border-bottom: 2px solid #6366f1; padding-bottom: 8px; }
        h2 { font-size: 16px; color: #6366f1; margin-top: 16px; }
        .route { padding: 10px; margin: 8px 0; border: 1px solid #e5e7eb; border-radius: 8px; }
        .route-header { font-weight: 600; font-size: 14px; }
        .route-meta { font-size: 12px; color: #6b7280; margin-top: 4px; }
        .summary { background: #f0f0ff; padding: 12px; border-radius: 8px; margin-top: 16px; font-size: 13px; }
        @media print { body { padding: 0; } }
      </style></head><body>${content.innerHTML}</body></html>`);
    win.document.close();
    win.print();
  };

  // Collect map positions from all routes
  const mapPositions: [number, number][] = [];
  routes.forEach(r => {
    if (r.fromLatitude && r.fromLongitude) mapPositions.push([r.fromLatitude, r.fromLongitude]);
    if (r.toLatitude && r.toLongitude) mapPositions.push([r.toLatitude, r.toLongitude]);
  });

  const dayRoutes = routes.filter(r => r.dayNumber === selectedDay);
  const currentDaySheet = daySheets.find(ds => ds.dayNumber === selectedDay);

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${id}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, flexWrap: 'wrap', gap: 10 }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 10 }}>
          <Navigation size={24} style={{ color: '#6366f1' }} /> Navigation Planner
        </h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-primary" onClick={() => setShowAddForm(true)}><Plus size={14} /> Add Route</button>
          <button className="btn btn-secondary" onClick={handlePrint}><Printer size={14} /> Print Day Sheet</button>
        </div>
      </div>

      {/* Day Selector */}
      <div style={{ display: 'flex', gap: 6, marginBottom: 16, overflowX: 'auto', paddingBottom: 4 }}>
        {daySheets.map(ds => (
          <button key={ds.dayNumber}
            className={`btn btn-sm ${selectedDay === ds.dayNumber ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setSelectedDay(ds.dayNumber)}
            style={{ whiteSpace: 'nowrap', borderRadius: 20 }}>
            Day {ds.dayNumber} <span style={{ fontSize: 10, opacity: 0.7, marginLeft: 4 }}>{ds.date}</span>
          </button>
        ))}
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'var(--bg-card)', padding: 4, borderRadius: 12 }}>
        {[
          { key: 'map' as const, label: 'Map View', icon: <MapPin size={14} /> },
          { key: 'routes' as const, label: `Routes (${dayRoutes.length})`, icon: <Route size={14} /> },
          { key: 'daysheets' as const, label: 'Day Sheet', icon: <FileText size={14} /> },
        ].map(tab => (
          <button key={tab.key}
            className={`btn btn-sm ${activeTab === tab.key ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setActiveTab(tab.key)}
            style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, borderRadius: 10 }}>
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* Add Form */}
      {showAddForm && (
        <div className="card" style={{ marginBottom: 16, padding: 20, border: '2px solid rgba(99,102,241,0.3)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3 style={{ fontSize: 16, fontWeight: 600 }}>Add Navigation Route</h3>
            <button className="btn btn-ghost btn-sm" onClick={() => setShowAddForm(false)}><X size={16} /></button>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 10 }}>
            <div>
              <label style={{ fontSize: 11, fontWeight: 600, display: 'block', marginBottom: 4 }}>📍 From</label>
              <input className="input" placeholder="From location *" value={fromLoc} onChange={e => setFromLoc(e.target.value)} />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4, marginTop: 4 }}>
                <input className="input" placeholder="Latitude" value={fromLat} onChange={e => setFromLat(e.target.value)} />
                <input className="input" placeholder="Longitude" value={fromLng} onChange={e => setFromLng(e.target.value)} />
              </div>
            </div>
            <div>
              <label style={{ fontSize: 11, fontWeight: 600, display: 'block', marginBottom: 4 }}>📍 To</label>
              <input className="input" placeholder="To location *" value={toLoc} onChange={e => setToLoc(e.target.value)} />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4, marginTop: 4 }}>
                <input className="input" placeholder="Latitude" value={toLat} onChange={e => setToLat(e.target.value)} />
                <input className="input" placeholder="Longitude" value={toLng} onChange={e => setToLng(e.target.value)} />
              </div>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: 10, marginBottom: 10 }}>
            <select className="input" value={mode} onChange={e => setMode(e.target.value)}>
              {TRANSPORT_MODES.map(m => <option key={m.value} value={m.value}>{m.icon} {m.label}</option>)}
            </select>
            <input className="input" type="number" placeholder="Buffer (min)" value={buffer} onChange={e => setBuffer(e.target.value)} />
            <input className="input" type="number" placeholder="Day #" value={dayNum} onChange={e => setDayNum(e.target.value)} />
          </div>
          <input className="input" placeholder="Directions / notes (optional)" value={directions} onChange={e => setDirections(e.target.value)} style={{ width: '100%', marginBottom: 10 }} />
          <button className="btn btn-primary" onClick={handleAdd}>Save Route</button>
        </div>
      )}

      {/* MAP TAB */}
      {activeTab === 'map' && (
        <div className="card" style={{ padding: 0, overflow: 'hidden', borderRadius: 16, height: 450 }}>
          <MapContainer center={mapPositions.length > 0 ? mapPositions[0] : [20.5937, 78.9629]} zoom={5}
            style={{ height: '100%', width: '100%' }}>
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' />
            {mapPositions.length > 0 && <FitBounds positions={mapPositions} />}
            {dayRoutes.map(r => (
              <span key={r.id}>
                {r.fromLatitude && r.fromLongitude && (
                  <Marker position={[r.fromLatitude, r.fromLongitude]}>
                    <Popup><strong>{r.fromLocation}</strong><br />→ {r.toLocation}<br />{r.transportModeIcon} {r.formattedTime}</Popup>
                  </Marker>
                )}
                {r.toLatitude && r.toLongitude && (
                  <Marker position={[r.toLatitude, r.toLongitude]}>
                    <Popup><strong>{r.toLocation}</strong></Popup>
                  </Marker>
                )}
                {r.fromLatitude && r.fromLongitude && r.toLatitude && r.toLongitude && (
                  <Polyline positions={[[r.fromLatitude, r.fromLongitude], [r.toLatitude, r.toLongitude]]}
                    pathOptions={{ color: '#6366f1', weight: 3, dashArray: '8,8' }} />
                )}
              </span>
            ))}
          </MapContainer>
        </div>
      )}

      {/* ROUTES TAB */}
      {activeTab === 'routes' && (
        <>
          {dayRoutes.length === 0 ? (
            <div className="empty-state">
              <Route size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
              <h3>No routes for Day {selectedDay}</h3>
              <p>Add a navigation route to plan your movements</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {dayRoutes.map((r, idx) => (
                <div key={r.id} className="card nav-card" style={{ padding: '16px 20px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                        <span style={{ fontSize: 11, background: 'rgba(99,102,241,0.1)', color: '#6366f1', padding: '2px 8px', borderRadius: 12, fontWeight: 700 }}>#{idx + 1}</span>
                        <span style={{ fontSize: 14, fontWeight: 600 }}>{r.fromLocation}</span>
                        <ArrowRight size={14} style={{ color: '#6366f1' }} />
                        <span style={{ fontSize: 14, fontWeight: 600 }}>{r.toLocation}</span>
                      </div>
                      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', fontSize: 12, color: 'var(--text-secondary)' }}>
                        <span>{r.transportModeIcon} {r.transportMode.replace('_', ' ')}</span>
                        <span>📏 {r.distanceKm} km</span>
                        <span>⏱️ {r.formattedTime}</span>
                      </div>
                      {r.directions && <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 6, fontStyle: 'italic' }}>📝 {r.directions}</div>}
                    </div>
                    <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                      {/* Transport mode toggle */}
                      <select className="input" value={r.transportMode} style={{ fontSize: 11, padding: '4px 6px', width: 'auto' }}
                        onChange={async e => { try { await switchMode(r.id, e.target.value, tripId); await fetchDaySheets(tripId); toast.success('Mode switched!'); } catch { toast.error('Failed'); } }}>
                        {TRANSPORT_MODES.map(m => <option key={m.value} value={m.value}>{m.icon} {m.label}</option>)}
                      </select>
                      <button className="btn btn-danger btn-sm" onClick={async () => { await deleteRoute(r.id, tripId); await fetchDaySheets(tripId); toast.success('Deleted'); }}>
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* DAY SHEETS TAB (Printable) */}
      {activeTab === 'daysheets' && (
        <div ref={printRef}>
          {currentDaySheet ? (
            <div className="card" style={{ padding: 24 }}>
              <h3 style={{ fontSize: 18, fontWeight: 700, borderBottom: '2px solid #6366f1', paddingBottom: 8, marginBottom: 16 }}>
                🗓️ Day {currentDaySheet.dayNumber} — {currentDaySheet.date}
              </h3>
              <div style={{ fontSize: 13, color: 'var(--text-secondary)', marginBottom: 16 }}>
                📍 Destination: <strong>{currentDaySheet.destination}</strong>
              </div>

              {currentDaySheet.routes.length === 0 ? (
                <div style={{ padding: 20, textAlign: 'center', color: 'var(--text-muted)' }}>No routes planned for this day.</div>
              ) : (
                currentDaySheet.routes.map((r, idx) => (
                  <div key={r.id} style={{ padding: '12px 16px', margin: '8px 0', border: '1px solid var(--border-color)', borderRadius: 10, background: 'var(--bg-input)' }}>
                    <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 4 }}>
                      {r.transportModeIcon} {r.fromLocation} → {r.toLocation}
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                      {r.transportMode.replace('_', ' ')} • {r.distanceKm} km • {r.formattedTime}
                    </div>
                    {r.directions && <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>📝 {r.directions}</div>}
                  </div>
                ))
              )}

              <div style={{ marginTop: 16, padding: 12, borderRadius: 10, background: 'rgba(99,102,241,0.06)', border: '1px solid rgba(99,102,241,0.15)', fontSize: 13 }}>
                <strong>Summary:</strong> {currentDaySheet.summary}<br />
                📏 Total Distance: {currentDaySheet.totalDistanceKm} km &nbsp;&nbsp; ⏱️ Total Travel: {currentDaySheet.totalTravelMinutes} min
              </div>
            </div>
          ) : (
            <div className="empty-state">
              <FileText size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
              <h3>No day sheet data</h3>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
