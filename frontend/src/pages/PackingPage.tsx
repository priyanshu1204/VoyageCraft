import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { usePackingStore } from '../store/packingStore';
import toast from 'react-hot-toast';
import { ArrowLeft, CheckSquare, Square, Plus, Trash2, Package, FileText, AlertTriangle, CloudSun, ShieldCheck, Clock } from 'lucide-react';
import type { PackingCategory, ClimateType, DocumentType, PackingItemRequest, TravelDocumentRequest } from '../types/packing';

const CATEGORIES: PackingCategory[] = ['CLOTHING','TOILETRIES','ELECTRONICS','DOCUMENTS','HEALTH','ACCESSORIES','FOOTWEAR','SNACKS','MISC'];
const CLIMATES: { value: ClimateType; label: string; icon: string }[] = [
  { value: 'TROPICAL', label: 'Tropical', icon: '🌴' },
  { value: 'COLD', label: 'Cold', icon: '❄️' },
  { value: 'TEMPERATE', label: 'Temperate', icon: '🌤️' },
  { value: 'DESERT', label: 'Desert', icon: '🏜️' },
  { value: 'BEACH', label: 'Beach', icon: '🏖️' },
  { value: 'MOUNTAIN', label: 'Mountain', icon: '🏔️' },
  { value: 'URBAN', label: 'Urban', icon: '🏙️' },
  { value: 'RAINY', label: 'Rainy', icon: '🌧️' },
];
const DOC_TYPES: { value: DocumentType; label: string }[] = [
  { value: 'PASSPORT', label: 'Passport' },
  { value: 'VISA', label: 'Visa' },
  { value: 'TRAVEL_INSURANCE', label: 'Travel Insurance' },
  { value: 'BOARDING_PASS', label: 'Boarding Pass' },
  { value: 'HOTEL_BOOKING', label: 'Hotel Booking' },
  { value: 'DRIVERS_LICENSE', label: "Driver's License" },
  { value: 'VACCINATION_CARD', label: 'Vaccination Card' },
  { value: 'ID_CARD', label: 'ID Card' },
  { value: 'OTHER', label: 'Other' },
];
const CAT_ICONS: Record<string, string> = {
  CLOTHING: '👕', TOILETRIES: '🧴', ELECTRONICS: '📱', DOCUMENTS: '📄',
  HEALTH: '💊', ACCESSORIES: '🎒', FOOTWEAR: '👟', SNACKS: '🍫', MISC: '📦',
};

export default function PackingPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const tripId = parseInt(id!);
  const {
    items, summary, documents, expiringDocs, isLoading,
    fetchItems, addItem, togglePacked, deleteItem, fetchSummary,
    applyTemplate, fetchDocuments, addDocument, deleteDocument, fetchExpiringDocs,
  } = usePackingStore();

  const [tab, setTab] = useState<'checklist' | 'documents'>('checklist');
  const [showAddItem, setShowAddItem] = useState(false);
  const [showAddDoc, setShowAddDoc] = useState(false);
  const [showTemplate, setShowTemplate] = useState(false);
  const [filterCat, setFilterCat] = useState<PackingCategory | 'ALL'>('ALL');

  // Add item form
  const [itemName, setItemName] = useState('');
  const [itemCat, setItemCat] = useState<PackingCategory>('CLOTHING');
  const [itemQty, setItemQty] = useState(1);
  const [itemNotes, setItemNotes] = useState('');

  // Add doc form
  const [docType, setDocType] = useState<DocumentType>('PASSPORT');
  const [docTitle, setDocTitle] = useState('');
  const [docNumber, setDocNumber] = useState('');
  const [docCountry, setDocCountry] = useState('');
  const [docIssueDate, setDocIssueDate] = useState('');
  const [docExpiryDate, setDocExpiryDate] = useState('');
  const [docNotes, setDocNotes] = useState('');

  useEffect(() => {
    if (tripId) {
      fetchItems(tripId); fetchSummary(tripId);
      fetchDocuments(tripId); fetchExpiringDocs(tripId);
    }
  }, [tripId]);

  const handleAddItem = async () => {
    if (!itemName.trim()) return toast.error('Item name required');
    const req: PackingItemRequest = { tripId, name: itemName, category: itemCat, quantity: itemQty, notes: itemNotes || undefined };
    try { await addItem(req); toast.success('Item added!'); setItemName(''); setItemQty(1); setItemNotes(''); setShowAddItem(false); } catch { toast.error('Failed'); }
  };

  const handleAddDoc = async () => {
    if (!docTitle.trim()) return toast.error('Title required');
    const req: TravelDocumentRequest = {
      tripId, documentType: docType, title: docTitle,
      documentNumber: docNumber || undefined, issuingCountry: docCountry || undefined,
      issueDate: docIssueDate || undefined, expiryDate: docExpiryDate || undefined,
      notes: docNotes || undefined,
    };
    try { await addDocument(req); toast.success('Document added!'); setDocTitle(''); setDocNumber(''); setDocCountry(''); setDocIssueDate(''); setDocExpiryDate(''); setDocNotes(''); setShowAddDoc(false); fetchExpiringDocs(tripId); } catch { toast.error('Failed'); }
  };

  const handleApplyTemplate = async (climate: ClimateType) => {
    try { await applyTemplate(tripId, climate); toast.success(`${climate} template applied!`); setShowTemplate(false); } catch { toast.error('Failed'); }
  };

  const grouped = items.reduce((acc, item) => {
    if (filterCat === 'ALL' || item.category === filterCat) {
      (acc[item.category] = acc[item.category] || []).push(item);
    }
    return acc;
  }, {} as Record<string, typeof items>);

  if (isLoading && items.length === 0) return <div className="spinner" />;

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${id}`)} style={{ marginBottom: 16 }}><ArrowLeft size={18} /> Back to Trip</button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 10 }}>
          <Package size={24} style={{ color: 'var(--accent-primary)' }} /> Packing & Documents
        </h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className={`btn btn-sm ${tab === 'checklist' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('checklist')}>
            <CheckSquare size={14} /> Checklist
          </button>
          <button className={`btn btn-sm ${tab === 'documents' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('documents')}>
            <FileText size={14} /> Documents
          </button>
        </div>
      </div>

      {/* ── Expiry Warnings ─────────────────────────── */}
      {expiringDocs.length > 0 && (
        <div style={{ marginBottom: 20, padding: '12px 16px', background: 'rgba(245,158,11,0.1)', border: '1px solid rgba(245,158,11,0.3)', borderRadius: 'var(--radius-md)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontWeight: 600, color: '#f59e0b', marginBottom: 6 }}>
            <AlertTriangle size={16} /> Expiry Reminders
          </div>
          {expiringDocs.map(d => (
            <div key={d.id} style={{ fontSize: 13, color: d.isExpired ? 'var(--danger)' : '#f59e0b', padding: '2px 0' }}>
              {d.isExpired ? '⛔' : '⚠️'} <strong>{d.title}</strong> ({d.documentType.replace('_',' ')})
              {d.isExpired ? ' — EXPIRED' : ` — expires in ${d.daysUntilExpiry} days`}
            </div>
          ))}
        </div>
      )}

      {/* ══════════════ PACKING CHECKLIST TAB ══════════════ */}
      {tab === 'checklist' && (
        <>
          {/* Summary Bar */}
          {summary && (
            <div className="card" style={{ marginBottom: 20, padding: '16px 20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                <span style={{ fontWeight: 600 }}>Packing Progress</span>
                <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{summary.packedItems}/{summary.totalItems} items packed</span>
              </div>
              <div className="poll-vote-bar" style={{ height: 10, marginBottom: 12 }}>
                <div className="poll-vote-fill" style={{
                  width: `${summary.packedPercent}%`,
                  background: summary.packedPercent === 100 ? '#22c55e' : 'var(--accent-gradient)',
                  transition: 'width 0.5s ease'
                }} />
              </div>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {summary.categoryBreakdown.map(c => (
                  <span key={c.category} style={{ fontSize: 11, padding: '2px 8px', background: 'var(--bg-input)', borderRadius: 12, color: 'var(--text-secondary)' }}>
                    {CAT_ICONS[c.category]} {c.packed}/{c.total}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Controls */}
          <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
            <button className="btn btn-primary btn-sm" onClick={() => setShowAddItem(!showAddItem)}><Plus size={14} /> Add Item</button>
            <button className="btn btn-secondary btn-sm" onClick={() => setShowTemplate(!showTemplate)}><CloudSun size={14} /> Apply Template</button>
            <select className="input" style={{ width: 140, fontSize: 12, padding: '6px 10px' }} value={filterCat} onChange={e => setFilterCat(e.target.value as any)}>
              <option value="ALL">All Categories</option>
              {CATEGORIES.map(c => <option key={c} value={c}>{CAT_ICONS[c]} {c}</option>)}
            </select>
          </div>

          {/* Template Picker */}
          {showTemplate && (
            <div className="card" style={{ marginBottom: 16 }}>
              <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>Select Climate / Destination Type</h4>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
                {CLIMATES.map(c => (
                  <button key={c.value} className="btn btn-secondary btn-sm" onClick={() => handleApplyTemplate(c.value)}
                    style={{ display: 'flex', alignItems: 'center', gap: 6, justifyContent: 'center' }}>
                    <span style={{ fontSize: 18 }}>{c.icon}</span> {c.label}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Add Item Form */}
          {showAddItem && (
            <div className="card" style={{ marginBottom: 16 }}>
              <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>Add Custom Item</h4>
              <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 80px', gap: 8, marginBottom: 8 }}>
                <input className="input" placeholder="Item name" value={itemName} onChange={e => setItemName(e.target.value)} />
                <select className="input" value={itemCat} onChange={e => setItemCat(e.target.value as PackingCategory)}>
                  {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
                <input className="input" type="number" min={1} value={itemQty} onChange={e => setItemQty(parseInt(e.target.value) || 1)} />
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <input className="input" placeholder="Notes (optional)" value={itemNotes} onChange={e => setItemNotes(e.target.value)} style={{ flex: 1 }} />
                <button className="btn btn-primary btn-sm" onClick={handleAddItem}>Add</button>
              </div>
            </div>
          )}

          {/* Grouped Items */}
          {Object.keys(grouped).length === 0 ? (
            <div className="empty-state"><Package size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} /><h3>No packing items yet</h3><p>Add items manually or apply a climate template</p></div>
          ) : (
            Object.entries(grouped).sort(([a],[b]) => a.localeCompare(b)).map(([cat, catItems]) => (
              <div key={cat} className="card" style={{ marginBottom: 12 }}>
                <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 10, display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{ fontSize: 18 }}>{CAT_ICONS[cat]}</span> {cat}
                  <span style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 400 }}>
                    ({catItems.filter(i => i.packed).length}/{catItems.length})
                  </span>
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                  {catItems.map(item => (
                    <div key={item.id} className={`packing-item-row ${item.packed ? 'packing-item-packed' : ''}`}>
                      <button className="btn btn-ghost" style={{ padding: 0, minWidth: 'auto' }} onClick={() => togglePacked(item.id)}>
                        {item.packed ? <CheckSquare size={18} style={{ color: '#22c55e' }} /> : <Square size={18} style={{ color: 'var(--text-muted)' }} />}
                      </button>
                      <span style={{ flex: 1, textDecoration: item.packed ? 'line-through' : 'none', fontSize: 14 }}>
                        {item.name}
                        {item.quantity > 1 && <span style={{ fontSize: 11, color: 'var(--text-muted)', marginLeft: 6 }}>×{item.quantity}</span>}
                      </span>
                      {item.isFromTemplate && <span className="template-badge">Template</span>}
                      {item.notes && <span style={{ fontSize: 11, color: 'var(--text-muted)', maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.notes}</span>}
                      <button className="btn btn-ghost btn-sm" style={{ padding: 2 }} onClick={() => deleteItem(item.id, tripId)}>
                        <Trash2 size={14} style={{ color: 'var(--danger)' }} />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))
          )}
        </>
      )}

      {/* ══════════════ DOCUMENTS TAB ══════════════ */}
      {tab === 'documents' && (
        <>
          <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
            <button className="btn btn-primary btn-sm" onClick={() => setShowAddDoc(!showAddDoc)}><Plus size={14} /> Add Document</button>
          </div>

          {/* Add Document Form */}
          {showAddDoc && (
            <div className="card" style={{ marginBottom: 16 }}>
              <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>Add Travel Document</h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 8 }}>
                <select className="input" value={docType} onChange={e => setDocType(e.target.value as DocumentType)}>
                  {DOC_TYPES.map(d => <option key={d.value} value={d.value}>{d.label}</option>)}
                </select>
                <input className="input" placeholder="Title (e.g. My Passport)" value={docTitle} onChange={e => setDocTitle(e.target.value)} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 8 }}>
                <input className="input" placeholder="Document Number" value={docNumber} onChange={e => setDocNumber(e.target.value)} />
                <input className="input" placeholder="Issuing Country" value={docCountry} onChange={e => setDocCountry(e.target.value)} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 8 }}>
                <div>
                  <label style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4, display: 'block' }}>Issue Date</label>
                  <input className="input" type="date" value={docIssueDate} onChange={e => setDocIssueDate(e.target.value)} />
                </div>
                <div>
                  <label style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4, display: 'block' }}>Expiry Date</label>
                  <input className="input" type="date" value={docExpiryDate} onChange={e => setDocExpiryDate(e.target.value)} />
                </div>
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <input className="input" placeholder="Notes (optional)" value={docNotes} onChange={e => setDocNotes(e.target.value)} style={{ flex: 1 }} />
                <button className="btn btn-primary btn-sm" onClick={handleAddDoc}>Save</button>
              </div>
            </div>
          )}

          {/* Document List */}
          {documents.length === 0 ? (
            <div className="empty-state"><FileText size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} /><h3>No documents yet</h3><p>Add your passport, visa, and travel insurance</p></div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 16 }}>
              {documents.map(doc => (
                <div key={doc.id} className={`card doc-card ${
                  doc.isExpired ? 'doc-status-expired' :
                  doc.isExpiringSoon ? 'doc-status-warning' :
                  'doc-status-valid'
                }`} style={{ padding: '16px 18px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                        <ShieldCheck size={16} style={{ color: 'var(--accent-primary)' }} />
                        <span style={{ fontWeight: 600, fontSize: 14 }}>{doc.title}</span>
                      </div>
                      <span className="badge badge-indigo" style={{ fontSize: 10 }}>{doc.documentType.replace(/_/g, ' ')}</span>
                    </div>
                    <button className="btn btn-ghost btn-sm" style={{ padding: 2 }} onClick={() => deleteDocument(doc.id, tripId)}>
                      <Trash2 size={14} style={{ color: 'var(--danger)' }} />
                    </button>
                  </div>

                  {doc.documentNumber && <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 4 }}>Number: <strong>{doc.documentNumber}</strong></div>}
                  {doc.issuingCountry && <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 4 }}>Country: {doc.issuingCountry}</div>}

                  {doc.expiryDate && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 8, fontSize: 12, fontWeight: 600,
                      color: doc.isExpired ? 'var(--danger)' : doc.isExpiringSoon ? '#f59e0b' : '#22c55e' }}>
                      <Clock size={12} />
                      {doc.isExpired ? `Expired (${Math.abs(doc.daysUntilExpiry!)} days ago)` :
                       doc.isExpiringSoon ? `Expires in ${doc.daysUntilExpiry} days` :
                       `Valid (${doc.daysUntilExpiry} days remaining)`}
                    </div>
                  )}

                  {doc.notes && <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 6, fontStyle: 'italic' }}>{doc.notes}</div>}
                  <div style={{ fontSize: 10, color: 'var(--text-muted)', marginTop: 6 }}>Added by {doc.userName}</div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
