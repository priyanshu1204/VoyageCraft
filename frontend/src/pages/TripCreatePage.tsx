import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTripStore } from '../store/tripStore';
import { tripApi } from '../api/tripApi';
import { TripPace, DestinationRequest, TemplateResponse } from '../types/trip';
import toast from 'react-hot-toast';
import { MapPin, Plus, Trash2, Sparkles, AlertCircle } from 'lucide-react';

const steps = ['Details', 'Destinations', 'Budget', 'Review'];

export default function TripCreatePage() {
  const [step, setStep] = useState(0);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [dateError, setDateError] = useState('');

  const handleStartDateChange = (value: string) => {
    setStartDate(value);
    if (endDate && value > endDate) {
      setEndDate('');
      setDateError('End date was reset because it was before the new start date.');
    } else {
      setDateError('');
    }
  };

  const handleEndDateChange = (value: string) => {
    if (startDate && value < startDate) {
      setDateError('End date must be after the start date.');
      return;
    }
    if (startDate && value === startDate) {
      setDateError('End date must be at least one day after the start date.');
      return;
    }
    setEndDate(value);
    setDateError('');
  };
  const [pace, setPace] = useState<TripPace>('STANDARD');
  const [currency, setCurrency] = useState('USD');
  const [budgetTotal, setBudgetTotal] = useState('');
  const [destinations, setDestinations] = useState<DestinationRequest[]>([{ destinationName: '', country: '', orderIndex: 0 }]);
  const [templates, setTemplates] = useState<TemplateResponse[]>([]);
  const [showTemplates, setShowTemplates] = useState(false);
  const { createTrip, isLoading } = useTripStore();
  const navigate = useNavigate();

  useEffect(() => { tripApi.getTemplates().then(r => setTemplates(r.data.data)).catch(() => {}); }, []);

  const addDest = () => setDestinations([...destinations, { destinationName: '', country: '', orderIndex: destinations.length }]);
  const removeDest = (i: number) => { if (destinations.length > 1) setDestinations(destinations.filter((_, idx) => idx !== i)); };
  const updateDest = (i: number, f: string, v: string) => { const u = [...destinations]; (u[i] as any)[f] = v; setDestinations(u); };

  const useTemplate = (t: TemplateResponse) => {
    setTitle(t.name); setDescription(t.description || '');
    if (t.budgetEstimate) setBudgetTotal(t.budgetEstimate.toString());
    try { const p = JSON.parse(t.destinationsJson || '[]'); if (p.length) setDestinations(p.map((d: any, i: number) => ({ destinationName: d.name, country: d.country, orderIndex: i }))); } catch {}
    setShowTemplates(false); toast.success('Template applied!');
  };

  const submit = async () => {
    try {
      const trip = await createTrip({ title, description, startDate, endDate, pace, currency, budgetTotal: budgetTotal ? parseFloat(budgetTotal) : undefined, destinations: destinations.filter(d => d.destinationName.trim()) });
      toast.success('Trip created!'); navigate(`/trips/${trip.id}`);
    } catch { toast.error('Failed to create trip'); }
  };

  const canNext = () => { if (step === 0) return title && startDate && endDate && !dateError && endDate > startDate; if (step === 1) return destinations.some(d => d.destinationName.trim()); return true; };

  return (
    <div className="animate-in">
      <div className="page-header"><h2>Create New Trip</h2><p>Plan your next adventure step by step</p></div>
      <div className="wizard">
        <div className="wizard-steps">
          {steps.map((s, i) => (<div key={s} className={`wizard-step ${i === step ? 'active' : ''} ${i < step ? 'completed' : ''}`}><div className="step-number">{i < step ? '✓' : i + 1}</div><div className="step-label">{s}</div></div>))}
        </div>

        {step === 0 && (<div className="card" style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(99,102,241,0.08)', border: '1px solid rgba(99,102,241,0.2)' }}><div style={{ display: 'flex', alignItems: 'center', gap: 12 }}><Sparkles size={20} style={{ color: 'var(--accent-primary)' }} /><div><div style={{ fontWeight: 600, fontSize: 14 }}>Start from a template</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Pre-built itineraries</div></div></div><button className="btn btn-secondary btn-sm" onClick={() => setShowTemplates(!showTemplates)}>{showTemplates ? 'Hide' : 'Browse'}</button></div>)}

        {showTemplates && (<div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 12, marginBottom: 24 }}>{templates.map(t => (<div key={t.id} className="card" style={{ padding: 16, cursor: 'pointer' }} onClick={() => useTemplate(t)}><div className="badge badge-indigo" style={{ marginBottom: 8 }}>{t.category}</div><div style={{ fontWeight: 600, fontSize: 14 }}>{t.name}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{t.durationDays}d · ${t.budgetEstimate?.toLocaleString()}</div></div>))}</div>)}

        <div className="wizard-content card">
          {step === 0 && (<div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}><div className="input-group"><label>Trip Title *</label><input className="input" placeholder="e.g., Summer Europe Adventure" value={title} onChange={e => setTitle(e.target.value)} /></div><div className="input-group"><label>Description</label><textarea className="input" rows={3} placeholder="Describe your trip..." value={description} onChange={e => setDescription(e.target.value)} style={{ resize: 'vertical' }} /></div><div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}><div className="input-group"><label>Start Date *</label><input className="input" type="date" value={startDate} onChange={e => handleStartDateChange(e.target.value)} min={new Date().toISOString().split('T')[0]} /></div><div className="input-group"><label>End Date *</label><input className={`input ${dateError ? 'input-error' : ''}`} type="date" value={endDate} onChange={e => handleEndDateChange(e.target.value)} min={startDate || new Date().toISOString().split('T')[0]} disabled={!startDate} /></div></div>{dateError && (<p className="validation-error"><AlertCircle size={13} /> {dateError}</p>)}{!startDate && (<p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: -8 }}>Please select a start date first before choosing an end date.</p>)}<div className="input-group"><label>Pace</label><div style={{ display: 'flex', gap: 8 }}>{(['RELAXED','STANDARD','INTENSE'] as TripPace[]).map(p => (<button key={p} className={`btn ${pace===p?'btn-primary':'btn-secondary'} btn-sm`} onClick={() => setPace(p)} type="button">{p.charAt(0)+p.slice(1).toLowerCase()}</button>))}</div></div></div>)}

          {step === 1 && (<div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>{destinations.map((d, i) => (<div key={i} style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}><div className="input-group" style={{ flex: 2 }}><label>Destination {i+1}</label><div style={{ position: 'relative' }}><MapPin size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} /><input className="input" placeholder="City" value={d.destinationName} onChange={e => updateDest(i, 'destinationName', e.target.value)} style={{ paddingLeft: 38, width: '100%' }} /></div></div><div className="input-group" style={{ flex: 1 }}><label>Country</label><input className="input" placeholder="Country" value={d.country||''} onChange={e => updateDest(i, 'country', e.target.value)} style={{ width: '100%' }} /></div><button className="btn btn-ghost btn-sm" onClick={() => removeDest(i)} type="button"><Trash2 size={16} style={{ color: 'var(--danger)' }} /></button></div>))}<button className="btn btn-secondary btn-sm" onClick={addDest} type="button" style={{ alignSelf: 'flex-start' }}><Plus size={16} /> Add Destination</button></div>)}

          {step === 2 && (<div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}><div className="input-group"><label>Total Budget</label><input className="input" type="number" placeholder="0.00" value={budgetTotal} onChange={e => setBudgetTotal(e.target.value)} /></div><div className="input-group"><label>Currency</label><select className="input" value={currency} onChange={e => setCurrency(e.target.value)}><option value="USD">USD</option><option value="EUR">EUR</option><option value="GBP">GBP</option><option value="INR">INR</option><option value="JPY">JPY</option></select></div></div>)}

          {step === 3 && (<div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}><h3 style={{ fontSize: 18, fontWeight: 600 }}>Review</h3><div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}><div className="card" style={{ padding: 14 }}><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Title</div><div style={{ fontWeight: 600 }}>{title}</div></div><div className="card" style={{ padding: 14 }}><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Dates</div><div style={{ fontWeight: 600 }}>{startDate} → {endDate}</div></div><div className="card" style={{ padding: 14 }}><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Pace</div><div style={{ fontWeight: 600 }}>{pace}</div></div><div className="card" style={{ padding: 14 }}><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Budget</div><div style={{ fontWeight: 600 }}>{budgetTotal ? `${currency} ${parseFloat(budgetTotal).toLocaleString()}` : 'Not set'}</div></div></div><div className="card" style={{ padding: 14 }}><div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 8 }}>Destinations</div><div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>{destinations.filter(d => d.destinationName.trim()).map((d, i) => (<span key={i} className="badge badge-indigo"><MapPin size={12} style={{ marginRight: 4 }} />{d.destinationName}{d.country ? `, ${d.country}` : ''}</span>))}</div></div></div>)}
        </div>

        <div className="wizard-actions">
          <button className="btn btn-secondary" onClick={() => step > 0 ? setStep(step-1) : navigate('/trips')} type="button">{step === 0 ? 'Cancel' : 'Back'}</button>
          {step < steps.length - 1 ? (<button className="btn btn-primary" onClick={() => setStep(step+1)} disabled={!canNext()} type="button">Continue</button>) : (<button className="btn btn-primary" onClick={submit} disabled={isLoading} type="button" id="trip-create-submit">{isLoading ? 'Creating...' : '🚀 Create Trip'}</button>)}
        </div>
      </div>
    </div>
  );
}
