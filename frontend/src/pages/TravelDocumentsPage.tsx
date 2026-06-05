import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTravelDocStore } from '../store/travelDocumentStore';
import toast from 'react-hot-toast';
import type { CountryLibraryEntry } from '../types/travelDocument';
import {
  ArrowLeft, FileText, Plus, Trash2, CheckSquare, Square, ExternalLink,
  Bell, BellOff, Shield, Syringe, ClipboardList, Globe, BookOpen, Calendar, X, ChevronDown
} from 'lucide-react';

export default function TravelDocumentsPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();
  const { documents, countryLibrary, isLoading, fetchDocuments, createDocument, deleteDocument,
    toggleChecklist, addChecklist, deleteChecklist, addReminder, dismissReminder, deleteReminder, fetchCountryLibrary } = useTravelDocStore();

  const [activeTab, setActiveTab] = useState<'documents' | 'library'>('documents');
  const [showAddForm, setShowAddForm] = useState(false);
  const [showAddChecklist, setShowAddChecklist] = useState<number | null>(null);
  const [showAddReminder, setShowAddReminder] = useState<number | null>(null);
  const [selectedCountry, setSelectedCountry] = useState<CountryLibraryEntry | null>(null);

  // Form states
  const [countryName, setCountryName] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [visaReq, setVisaReq] = useState('');
  const [entryGuide, setEntryGuide] = useState('');
  const [officialLink, setOfficialLink] = useState('');
  const [notes, setNotes] = useState('');

  const [checkItemName, setCheckItemName] = useState('');
  const [checkItemType, setCheckItemType] = useState('VISA');
  const [checkDesc, setCheckDesc] = useState('');
  const [checkLink, setCheckLink] = useState('');

  const [reminderTitle, setReminderTitle] = useState('');
  const [reminderNote, setReminderNote] = useState('');
  const [reminderDate, setReminderDate] = useState('');

  useEffect(() => { fetchDocuments(tripId); fetchCountryLibrary(); }, [tripId]);

  const handleCreate = async () => {
    if (!countryName.trim()) { toast.error('Country name required'); return; }
    try {
      await createDocument(tripId, { countryName, countryCode, visaRequirements: visaReq, entryGuidelines: entryGuide, officialLink, additionalNotes: notes });
      toast.success('Document created!');
      setShowAddForm(false);
      setCountryName(''); setCountryCode(''); setVisaReq(''); setEntryGuide(''); setOfficialLink(''); setNotes('');
    } catch { toast.error('Failed to create document'); }
  };

  const handleAddFromLibrary = async (entry: CountryLibraryEntry) => {
    try {
      await createDocument(tripId, {
        countryName: entry.countryName, countryCode: entry.countryCode,
        visaRequirements: entry.visaRequirements, entryGuidelines: entry.entryGuidelines,
        officialLink: entry.officialLink, additionalNotes: '',
        checklistItems: [
          ...entry.suggestedVaccines.map(v => ({ itemName: v, itemType: 'VACCINE' })),
          ...entry.suggestedDocuments.map(d => ({ itemName: d, itemType: 'FORM' })),
        ],
      });
      toast.success(`${entry.countryName} guidelines added!`);
      setActiveTab('documents');
    } catch { toast.error('Failed to add'); }
  };

  const handleAddChecklist = async (docId: number) => {
    if (!checkItemName.trim()) { toast.error('Item name required'); return; }
    try {
      await addChecklist(docId, { itemName: checkItemName, itemType: checkItemType, description: checkDesc, documentLink: checkLink }, tripId);
      toast.success('Checklist item added');
      setShowAddChecklist(null); setCheckItemName(''); setCheckDesc(''); setCheckLink('');
    } catch { toast.error('Failed'); }
  };

  const handleAddReminder = async (docId: number) => {
    if (!reminderTitle.trim() || !reminderDate) { toast.error('Title and date required'); return; }
    try {
      await addReminder(docId, { title: reminderTitle, note: reminderNote, reminderDate }, tripId);
      toast.success('Reminder added');
      setShowAddReminder(null); setReminderTitle(''); setReminderNote(''); setReminderDate('');
    } catch { toast.error('Failed'); }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'VISA': return <Shield size={12} style={{ color: '#6366f1' }} />;
      case 'VACCINE': return <Syringe size={12} style={{ color: '#22c55e' }} />;
      case 'FORM': return <ClipboardList size={12} style={{ color: '#f59e0b' }} />;
      case 'INSURANCE': return <FileText size={12} style={{ color: '#3b82f6' }} />;
      case 'PASSPORT': return <BookOpen size={12} style={{ color: '#ef4444' }} />;
      default: return <FileText size={12} style={{ color: '#6b7280' }} />;
    }
  };

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${id}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 10 }}>
          <Shield size={24} style={{ color: '#6366f1' }} /> Visa & Entry Guidelines
        </h2>
        <button className="btn btn-primary" onClick={() => setShowAddForm(true)}>
          <Plus size={14} /> Add Country
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'var(--bg-card)', padding: 4, borderRadius: 12 }}>
        {[
          { key: 'documents' as const, label: `My Documents (${documents.length})`, icon: <FileText size={14} /> },
          { key: 'library' as const, label: 'Country Library', icon: <Globe size={14} /> },
        ].map(tab => (
          <button key={tab.key} className={`btn btn-sm ${activeTab === tab.key ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setActiveTab(tab.key)}
            style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, borderRadius: 10 }}>
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* Add Form Modal */}
      {showAddForm && (
        <div className="card" style={{ marginBottom: 16, padding: 20, border: '2px solid rgba(99,102,241,0.3)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3 style={{ fontSize: 16, fontWeight: 600 }}>Add Country Guidelines</h3>
            <button className="btn btn-ghost btn-sm" onClick={() => setShowAddForm(false)}><X size={16} /></button>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 10, marginBottom: 10 }}>
            <input className="input" placeholder="Country Name *" value={countryName} onChange={e => setCountryName(e.target.value)} />
            <input className="input" placeholder="Country Code (e.g. IN)" value={countryCode} onChange={e => setCountryCode(e.target.value)} />
          </div>
          <textarea className="input" placeholder="Visa Requirements" rows={2} value={visaReq} onChange={e => setVisaReq(e.target.value)} style={{ width: '100%', marginBottom: 10 }} />
          <textarea className="input" placeholder="Entry Guidelines" rows={2} value={entryGuide} onChange={e => setEntryGuide(e.target.value)} style={{ width: '100%', marginBottom: 10 }} />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 10 }}>
            <input className="input" placeholder="Official Link (URL)" value={officialLink} onChange={e => setOfficialLink(e.target.value)} />
            <input className="input" placeholder="Additional Notes" value={notes} onChange={e => setNotes(e.target.value)} />
          </div>
          <button className="btn btn-primary" onClick={handleCreate}>Save Document</button>
        </div>
      )}

      {/* DOCUMENTS TAB */}
      {activeTab === 'documents' && (
        <>
          {documents.length === 0 ? (
            <div className="empty-state">
              <Shield size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
              <h3>No documents yet</h3>
              <p>Add a country or pick one from the Country Library to get started</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              {documents.map(doc => (
                <div key={doc.id} className="card doc-card" style={{ padding: '20px 24px' }}>
                  {/* Header */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <span style={{ fontSize: 22 }}>🌍</span>
                      <div>
                        <span style={{ fontSize: 17, fontWeight: 700 }}>{doc.countryName}</span>
                        {doc.countryCode && <span className="badge badge-indigo" style={{ marginLeft: 8, fontSize: 10 }}>{doc.countryCode}</span>}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 6 }}>
                      {doc.officialLink && (
                        <a href={doc.officialLink} target="_blank" rel="noopener noreferrer" className="btn btn-ghost btn-sm">
                          <ExternalLink size={13} /> Official Site
                        </a>
                      )}
                      <button className="btn btn-danger btn-sm" onClick={async () => { await deleteDocument(doc.id, tripId); toast.success('Deleted'); }}>
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>

                  {/* Visa & Entry Info */}
                  {doc.visaRequirements && (
                    <div style={{ marginBottom: 10, padding: '8px 12px', borderRadius: 8, background: 'rgba(99,102,241,0.06)', border: '1px solid rgba(99,102,241,0.15)' }}>
                      <div style={{ fontSize: 11, fontWeight: 600, color: '#6366f1', marginBottom: 4 }}>📋 Visa Requirements</div>
                      <div style={{ fontSize: 13 }}>{doc.visaRequirements}</div>
                    </div>
                  )}
                  {doc.entryGuidelines && (
                    <div style={{ marginBottom: 10, padding: '8px 12px', borderRadius: 8, background: 'rgba(34,197,94,0.06)', border: '1px solid rgba(34,197,94,0.15)' }}>
                      <div style={{ fontSize: 11, fontWeight: 600, color: '#22c55e', marginBottom: 4 }}>🛂 Entry Guidelines</div>
                      <div style={{ fontSize: 13 }}>{doc.entryGuidelines}</div>
                    </div>
                  )}
                  {doc.additionalNotes && (
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 10, fontStyle: 'italic' }}>📝 {doc.additionalNotes}</div>
                  )}

                  {/* Checklist */}
                  <div style={{ marginBottom: 10 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                      <span style={{ fontSize: 13, fontWeight: 600 }}>✅ Checklist ({doc.checklistItems.filter(i => i.completed).length}/{doc.checklistItems.length})</span>
                      <button className="btn btn-ghost btn-sm" onClick={() => setShowAddChecklist(showAddChecklist === doc.id ? null : doc.id)}>
                        <Plus size={12} /> Item
                      </button>
                    </div>
                    {doc.checklistItems.map(item => (
                      <div key={item.id} style={{
                        display: 'flex', alignItems: 'center', gap: 8, padding: '6px 10px', borderRadius: 8,
                        background: item.completed ? 'rgba(34,197,94,0.06)' : 'var(--bg-input)', marginBottom: 4
                      }}>
                        <button className="btn btn-ghost" style={{ padding: 0 }} onClick={() => toggleChecklist(item.id, tripId)}>
                          {item.completed ? <CheckSquare size={16} style={{ color: '#22c55e' }} /> : <Square size={16} style={{ color: 'var(--text-muted)' }} />}
                        </button>
                        {getTypeIcon(item.itemType)}
                        <span style={{ fontSize: 13, flex: 1, textDecoration: item.completed ? 'line-through' : 'none', opacity: item.completed ? 0.6 : 1 }}>
                          {item.itemName}
                        </span>
                        {item.documentLink && <a href={item.documentLink} target="_blank" rel="noopener noreferrer"><ExternalLink size={12} /></a>}
                        <button className="btn btn-ghost" style={{ padding: 0 }} onClick={() => deleteChecklist(item.id, tripId)}>
                          <Trash2 size={12} style={{ color: '#ef4444' }} />
                        </button>
                      </div>
                    ))}
                    {showAddChecklist === doc.id && (
                      <div style={{ padding: 10, borderRadius: 8, background: 'var(--bg-input)', marginTop: 6 }}>
                        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 6, marginBottom: 6 }}>
                          <input className="input" placeholder="Item name *" value={checkItemName} onChange={e => setCheckItemName(e.target.value)} />
                          <select className="input" value={checkItemType} onChange={e => setCheckItemType(e.target.value)}>
                            <option value="VISA">Visa</option>
                            <option value="VACCINE">Vaccine</option>
                            <option value="FORM">Form</option>
                            <option value="INSURANCE">Insurance</option>
                            <option value="PASSPORT">Passport</option>
                            <option value="PERMIT">Permit</option>
                            <option value="OTHER">Other</option>
                          </select>
                        </div>
                        <input className="input" placeholder="Document link (optional)" value={checkLink} onChange={e => setCheckLink(e.target.value)} style={{ width: '100%', marginBottom: 6 }} />
                        <button className="btn btn-primary btn-sm" onClick={() => handleAddChecklist(doc.id)}>Add</button>
                      </div>
                    )}
                  </div>

                  {/* Reminders */}
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                      <span style={{ fontSize: 13, fontWeight: 600 }}>🔔 Reminders</span>
                      <button className="btn btn-ghost btn-sm" onClick={() => setShowAddReminder(showAddReminder === doc.id ? null : doc.id)}>
                        <Plus size={12} /> Reminder
                      </button>
                    </div>
                    {doc.reminders.map(rem => (
                      <div key={rem.id} style={{
                        display: 'flex', alignItems: 'center', gap: 8, padding: '6px 10px', borderRadius: 8,
                        background: rem.dismissed ? 'rgba(107,114,128,0.06)' : 'rgba(245,158,11,0.06)',
                        border: rem.dismissed ? 'none' : '1px solid rgba(245,158,11,0.2)', marginBottom: 4
                      }}>
                        <Calendar size={13} style={{ color: rem.dismissed ? '#6b7280' : '#f59e0b' }} />
                        <span style={{ fontSize: 12, fontWeight: 600, color: rem.dismissed ? '#6b7280' : 'var(--text-primary)' }}>{rem.reminderDate}</span>
                        <span style={{ fontSize: 13, flex: 1, opacity: rem.dismissed ? 0.5 : 1 }}>{rem.title}</span>
                        {!rem.dismissed && (
                          <button className="btn btn-ghost" style={{ padding: 0 }} onClick={() => dismissReminder(rem.id, tripId)}>
                            <BellOff size={13} style={{ color: '#6b7280' }} />
                          </button>
                        )}
                        <button className="btn btn-ghost" style={{ padding: 0 }} onClick={() => deleteReminder(rem.id, tripId)}>
                          <Trash2 size={12} style={{ color: '#ef4444' }} />
                        </button>
                      </div>
                    ))}
                    {showAddReminder === doc.id && (
                      <div style={{ padding: 10, borderRadius: 8, background: 'var(--bg-input)', marginTop: 6 }}>
                        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 6, marginBottom: 6 }}>
                          <input className="input" placeholder="Reminder title *" value={reminderTitle} onChange={e => setReminderTitle(e.target.value)} />
                          <input className="input" type="date" value={reminderDate} onChange={e => setReminderDate(e.target.value)} />
                        </div>
                        <input className="input" placeholder="Note (optional)" value={reminderNote} onChange={e => setReminderNote(e.target.value)} style={{ width: '100%', marginBottom: 6 }} />
                        <button className="btn btn-primary btn-sm" onClick={() => handleAddReminder(doc.id)}>Add Reminder</button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* LIBRARY TAB */}
      {activeTab === 'library' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 12 }}>
          {countryLibrary.map((entry, i) => (
            <div key={i} className="card doc-card" style={{ padding: '16px 20px', cursor: 'pointer' }}
              onClick={() => setSelectedCountry(selectedCountry?.countryCode === entry.countryCode ? null : entry)}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{ fontSize: 20 }}>🌍</span>
                  <span style={{ fontSize: 15, fontWeight: 700 }}>{entry.countryName}</span>
                  <span className="badge badge-indigo" style={{ fontSize: 10 }}>{entry.countryCode}</span>
                </div>
                <ChevronDown size={14} style={{ transform: selectedCountry?.countryCode === entry.countryCode ? 'rotate(180deg)' : 'none', transition: '0.2s' }} />
              </div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 8 }}>{entry.visaRequirements}</div>

              {selectedCountry?.countryCode === entry.countryCode && (
                <div className="animate-in" style={{ marginTop: 8 }}>
                  <div style={{ fontSize: 12, marginBottom: 6, padding: '6px 10px', borderRadius: 6, background: 'rgba(34,197,94,0.06)' }}>
                    <strong style={{ color: '#22c55e' }}>Entry:</strong> {entry.entryGuidelines}
                  </div>
                  <div style={{ fontSize: 12, marginBottom: 6 }}>
                    <strong>Vaccines:</strong>
                    {entry.suggestedVaccines.map((v, j) => <div key={j} style={{ paddingLeft: 12, color: 'var(--text-secondary)' }}>• {v}</div>)}
                  </div>
                  <div style={{ fontSize: 12, marginBottom: 8 }}>
                    <strong>Documents:</strong>
                    {entry.suggestedDocuments.map((d, j) => <div key={j} style={{ paddingLeft: 12, color: 'var(--text-secondary)' }}>• {d}</div>)}
                  </div>
                  <button className="btn btn-primary btn-sm" onClick={(e) => { e.stopPropagation(); handleAddFromLibrary(entry); }}>
                    <Plus size={12} /> Add to My Trip
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
