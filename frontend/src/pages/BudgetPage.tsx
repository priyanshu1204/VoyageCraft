import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useBudgetStore } from '../store/budgetStore';
import { useTripStore } from '../store/tripStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, Plus, Trash2, X, DollarSign, TrendingUp, PieChart,
  Download, ChevronDown, ChevronUp, Wallet, AlertTriangle, CheckCircle,
} from 'lucide-react';
import type { ExpenseCategory, ExpenseRequest } from '../types/budget';

const CAT_META: Record<ExpenseCategory, { label: string; emoji: string; color: string }> = {
  TRANSPORT:     { label: 'Transport',     emoji: '✈️', color: '#3b82f6' },
  STAY:          { label: 'Stay',          emoji: '🏨', color: '#8b5cf6' },
  FOOD:          { label: 'Food',          emoji: '🍽️', color: '#f59e0b' },
  ACTIVITIES:    { label: 'Activities',    emoji: '🎯', color: '#ef4444' },
  SHOPPING:      { label: 'Shopping',      emoji: '🛍️', color: '#ec4899' },
  ENTERTAINMENT: { label: 'Entertainment', emoji: '🎭', color: '#a855f7' },
  INSURANCE:     { label: 'Insurance',     emoji: '🛡️', color: '#14b8a6' },
  VISA:          { label: 'Visa',          emoji: '📄', color: '#6366f1' },
  MISCELLANEOUS: { label: 'Misc',          emoji: '📦', color: '#64748b' },
};

const CURRENCIES = ['USD','EUR','GBP','INR','JPY','AUD','CAD','SGD','THB','MYR','AED','CHF','CNY','KRW','NZD'];

function ProgressBar({ percent, color }: { percent: number; color: string }) {
  const clamped = Math.min(percent, 100);
  return (
    <div style={{ width: '100%', height: 8, background: 'var(--bg-input)', borderRadius: 4, overflow: 'hidden' }}>
      <div style={{ width: `${clamped}%`, height: '100%', background: percent > 100 ? '#ef4444' : color, borderRadius: 4, transition: 'width 0.5s ease' }} />
    </div>
  );
}

function AddExpenseModal({ tripId, baseCurrency, onClose }: { tripId: number; baseCurrency: string; onClose: () => void }) {
  const { addExpense } = useBudgetStore();
  const [form, setForm] = useState<ExpenseRequest>({
    tripId, title: '', category: 'FOOD', amount: 0, currency: baseCurrency,
    expenseDate: new Date().toISOString().split('T')[0], paidBy: '', notes: '',
    isReimbursable: false,
  });
  const [saving, setSaving] = useState(false);
  const handleSave = async () => {
    if (!form.title.trim()) { toast.error('Title required'); return; }
    if (!form.amount || form.amount <= 0) { toast.error('Enter a valid amount'); return; }
    if (!form.expenseDate) { toast.error('Date required'); return; }
    setSaving(true);
    try {
      // Sanitize: send null instead of empty strings for optional fields
      const payload: ExpenseRequest = {
        ...form,
        title: form.title.trim(),
        paidBy: form.paidBy?.trim() || undefined,
        notes: form.notes?.trim() || undefined,
        isReimbursable: form.isReimbursable ?? false,
      };
      await addExpense(payload);
      toast.success('Expense added!');
      onClose();
    } catch (e: any) {
      console.error('Add expense error:', e);
      toast.error(e.response?.data?.message || 'Failed to add expense');
    } finally {
      setSaving(false);
    }
  };
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 550 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
          <h3 style={{ margin: 0 }}>💸 Add Expense</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
          <div className="input-group" style={{ gridColumn: '1/-1' }}>
            <label>Title *</label>
            <input className="input" placeholder="e.g. Airport Taxi" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Category</label>
            <select className="input" value={form.category} onChange={e => setForm({ ...form, category: e.target.value as ExpenseCategory })}>
              {Object.entries(CAT_META).map(([k, v]) => <option key={k} value={k}>{v.emoji} {v.label}</option>)}
            </select>
          </div>
          <div className="input-group">
            <label>Date *</label>
            <input className="input" type="date" value={form.expenseDate} onChange={e => setForm({ ...form, expenseDate: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Amount *</label>
            <input className="input" type="number" step="0.01" placeholder="0.00" value={form.amount || ''} onChange={e => setForm({ ...form, amount: parseFloat(e.target.value) || 0 })} />
          </div>
          <div className="input-group">
            <label>Currency</label>
            <select className="input" value={form.currency} onChange={e => setForm({ ...form, currency: e.target.value })}>
              {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div className="input-group">
            <label>Paid By</label>
            <input className="input" placeholder="Name" value={form.paidBy} onChange={e => setForm({ ...form, paidBy: e.target.value })} />
          </div>
          <div className="input-group">
            <label>Reimbursable?</label>
            <select className="input" value={form.isReimbursable ? 'yes' : 'no'} onChange={e => setForm({ ...form, isReimbursable: e.target.value === 'yes' })}>
              <option value="no">No</option><option value="yes">Yes</option>
            </select>
          </div>
          <div className="input-group" style={{ gridColumn: '1/-1' }}>
            <label>Notes</label>
            <input className="input" placeholder="Optional notes" value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
          </div>
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 20 }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Saving...' : 'Add Expense'}</button>
        </div>
      </div>
    </div>
  );
}

function SetBudgetModal({ tripId, onClose }: { tripId: number; onClose: () => void }) {
  const { setCategoryBudget } = useBudgetStore();
  const [cat, setCat] = useState<ExpenseCategory>('TRANSPORT');
  const [amount, setAmount] = useState(0);
  const [saving, setSaving] = useState(false);
  const handleSave = async () => {
    if (amount <= 0) { toast.error('Enter a valid amount'); return; }
    setSaving(true);
    try { await setCategoryBudget({ tripId, category: cat, allocatedAmount: amount }); toast.success(`${CAT_META[cat].label} budget set!`); onClose(); }
    catch { toast.error('Failed'); }
    finally { setSaving(false); }
  };
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 420 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
          <h3 style={{ margin: 0 }}>📊 Set Category Budget</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
        </div>
        <div className="input-group">
          <label>Category</label>
          <select className="input" value={cat} onChange={e => setCat(e.target.value as ExpenseCategory)}>
            {Object.entries(CAT_META).map(([k, v]) => <option key={k} value={k}>{v.emoji} {v.label}</option>)}
          </select>
        </div>
        <div className="input-group" style={{ marginTop: 12 }}>
          <label>Allocated Amount</label>
          <input className="input" type="number" step="0.01" placeholder="0.00" value={amount || ''} onChange={e => setAmount(parseFloat(e.target.value) || 0)} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 20 }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Saving...' : 'Set Budget'}</button>
        </div>
      </div>
    </div>
  );
}

export default function BudgetPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();
  const { expenses, summary, fetchExpenses, fetchSummary, deleteExpense, exportCsv, isLoading } = useBudgetStore();
  const { currentTrip, fetchTrip } = useTripStore();
  const [showAddExpense, setShowAddExpense] = useState(false);
  const [showSetBudget, setShowSetBudget] = useState(false);
  const [tab, setTab] = useState<'overview' | 'expenses'>('overview');
  const [filterCat, setFilterCat] = useState<string>('ALL');
  const [expandedId, setExpandedId] = useState<number | null>(null);

  useEffect(() => {
    fetchExpenses(tripId); fetchSummary(tripId);
    if (!currentTrip || currentTrip.id !== tripId) fetchTrip(tripId);
  }, [tripId]);

  const baseCurrency = summary?.baseCurrency || currentTrip?.currency || 'USD';
  const filtered = filterCat === 'ALL' ? expenses : expenses.filter(e => e.category === filterCat);

  const handleDelete = async (eid: number) => {
    try { await deleteExpense(eid, tripId); toast.success('Deleted'); } catch { toast.error('Failed'); }
  };
  const handleExport = async () => {
    try { await exportCsv(tripId); toast.success('CSV downloaded!'); } catch { toast.error('Export failed'); }
  };

  const budgetStatus = summary && summary.totalBudget > 0
    ? summary.overallPercentUsed > 100 ? 'over' : summary.overallPercentUsed > 80 ? 'warning' : 'good'
    : 'none';

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #22c55e, #14b8a6)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Wallet size={22} color="white" />
          </div>
          <div>
            <h2>Budget & Expenses</h2>
            <p>{currentTrip?.title || `Trip #${tripId}`} · Base: {baseCurrency}</p>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      {summary && (
        <div className="itinerary-stats-bar" style={{ marginBottom: 24 }}>
          <div className="i-stat"><DollarSign size={15} style={{ color: '#22c55e' }} /><span className="i-stat-val">{baseCurrency} {summary.totalBudget.toFixed(0)}</span><span className="i-stat-label">total budget</span></div>
          <div className="i-stat"><TrendingUp size={15} style={{ color: budgetStatus === 'over' ? '#ef4444' : budgetStatus === 'warning' ? '#f59e0b' : '#22c55e' }} /><span className="i-stat-val">{baseCurrency} {summary.totalSpent.toFixed(0)}</span><span className="i-stat-label">spent ({summary.overallPercentUsed.toFixed(1)}%)</span></div>
          <div className="i-stat">{budgetStatus === 'over' ? <AlertTriangle size={15} style={{ color: '#ef4444' }} /> : <CheckCircle size={15} style={{ color: '#22c55e' }} />}<span className="i-stat-val" style={{ color: summary.totalRemaining < 0 ? '#ef4444' : 'inherit' }}>{baseCurrency} {summary.totalRemaining.toFixed(0)}</span><span className="i-stat-label">remaining</span></div>
          <div className="i-stat"><PieChart size={15} style={{ color: '#6366f1' }} /><span className="i-stat-val">{summary.totalExpenses}</span><span className="i-stat-label">expenses</span></div>
        </div>
      )}

      {/* Overall Progress */}
      {summary && summary.totalBudget > 0 && (
        <div style={{ marginBottom: 24, padding: '16px 20px', background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: 13 }}>
            <span style={{ fontWeight: 600 }}>Overall Budget Usage</span>
            <span style={{ color: budgetStatus === 'over' ? '#ef4444' : 'var(--text-muted)' }}>{summary.overallPercentUsed.toFixed(1)}%</span>
          </div>
          <ProgressBar percent={summary.overallPercentUsed} color="#6366f1" />
        </div>
      )}

      {/* Tab Bar */}
      <div className="tab-row">
        <button className={`tab-btn ${tab === 'overview' ? 'active' : ''}`} onClick={() => setTab('overview')}><PieChart size={14} style={{ marginRight: 6 }} /> Overview</button>
        <button className={`tab-btn ${tab === 'expenses' ? 'active' : ''}`} onClick={() => setTab('expenses')}><DollarSign size={14} style={{ marginRight: 6 }} /> Expenses ({expenses.length})</button>
      </div>

      {/* OVERVIEW TAB */}
      {tab === 'overview' && (
        <div className="animate-in">
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginBottom: 16 }}>
            <button className="btn btn-secondary btn-sm" onClick={() => setShowSetBudget(true)}><PieChart size={13} /> Set Budget</button>
            <button className="btn btn-secondary btn-sm" onClick={handleExport}><Download size={13} /> Export CSV</button>
          </div>

          {/* Category Breakdown */}
          {summary && summary.categoryBreakdown.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {summary.categoryBreakdown.map(cb => {
                const meta = CAT_META[cb.category] || CAT_META.MISCELLANEOUS;
                return (
                  <div key={cb.category} style={{ padding: '14px 18px', background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span className="segment-type-badge" style={{ background: meta.color + '20', color: meta.color }}>{meta.emoji} {meta.label}</span>
                      </div>
                      <div style={{ textAlign: 'right', fontSize: 13 }}>
                        <span style={{ fontWeight: 600 }}>{baseCurrency} {cb.spentAmount.toFixed(0)}</span>
                        {cb.allocatedAmount > 0 && <span style={{ color: 'var(--text-muted)' }}> / {cb.allocatedAmount.toFixed(0)}</span>}
                      </div>
                    </div>
                    {cb.allocatedAmount > 0 && <ProgressBar percent={cb.percentUsed} color={meta.color} />}
                    {cb.allocatedAmount > 0 && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 6, fontSize: 11, color: 'var(--text-muted)' }}>
                        <span>Remaining: {baseCurrency} {cb.remainingAmount.toFixed(0)}</span>
                        <span style={{ color: cb.percentUsed > 100 ? '#ef4444' : 'inherit' }}>{cb.percentUsed.toFixed(1)}%</span>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="empty-state">
              <PieChart size={48} />
              <h3>No budget data yet</h3>
              <p>Set category budgets and add expenses to see your spending breakdown.</p>
              <div style={{ display: 'flex', gap: 8 }}>
                <button className="btn btn-primary" onClick={() => setShowSetBudget(true)}>Set Budget</button>
                <button className="btn btn-secondary" onClick={() => { setTab('expenses'); setShowAddExpense(true); }}>Add Expense</button>
              </div>
            </div>
          )}

          {/* Daily Spending */}
          {summary && summary.dailySpending.length > 0 && (
            <div style={{ marginTop: 24 }}>
              <h4 style={{ fontSize: 15, fontWeight: 600, marginBottom: 12 }}>📅 Daily Spending</h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                {summary.dailySpending.map(d => {
                  const maxAmount = Math.max(...summary.dailySpending.map(x => x.amount));
                  const pct = maxAmount > 0 ? (d.amount / maxAmount) * 100 : 0;
                  return (
                    <div key={d.date} style={{ display: 'flex', alignItems: 'center', gap: 12, fontSize: 13 }}>
                      <span style={{ minWidth: 90, color: 'var(--text-muted)' }}>{d.date}</span>
                      <div style={{ flex: 1, height: 6, background: 'var(--bg-input)', borderRadius: 3, overflow: 'hidden' }}>
                        <div style={{ width: `${pct}%`, height: '100%', background: '#6366f1', borderRadius: 3 }} />
                      </div>
                      <span style={{ minWidth: 80, textAlign: 'right', fontWeight: 600 }}>{baseCurrency} {d.amount.toFixed(0)}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}

      {/* EXPENSES TAB */}
      {tab === 'expenses' && (
        <div className="animate-in">
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
            <select className="input" style={{ width: 'auto' }} value={filterCat} onChange={e => setFilterCat(e.target.value)}>
              <option value="ALL">All Categories</option>
              {Object.entries(CAT_META).map(([k, v]) => <option key={k} value={k}>{v.emoji} {v.label}</option>)}
            </select>
            <button className="btn btn-primary btn-sm" onClick={() => setShowAddExpense(true)}><Plus size={14} /> Add Expense</button>
          </div>

          {isLoading ? <div className="spinner" /> : filtered.length === 0 ? (
            <div className="empty-state">
              <DollarSign size={48} /><h3>No expenses recorded</h3><p>Start tracking your spending by adding an expense.</p>
              <button className="btn btn-primary" onClick={() => setShowAddExpense(true)}><Plus size={16} /> Add Expense</button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {filtered.map(exp => {
                const meta = CAT_META[exp.category] || CAT_META.MISCELLANEOUS;
                const isOpen = expandedId === exp.id;
                return (
                  <div key={exp.id} className="activity-card">
                    <div className="stay-header" onClick={() => setExpandedId(isOpen ? null : exp.id)} style={{ cursor: 'pointer' }}>
                      <span className="segment-type-badge" style={{ background: meta.color + '20', color: meta.color }}>{meta.emoji} {meta.label}</span>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 600, fontSize: 14 }}>{exp.title}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{exp.expenseDate}</div>
                      </div>
                      <div style={{ textAlign: 'right', flexShrink: 0 }}>
                        <div style={{ fontWeight: 700, fontSize: 15 }}>{exp.currency} {exp.amount.toFixed(2)}</div>
                        {exp.currency !== baseCurrency && <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>≈ {baseCurrency} {exp.amountInBaseCurrency.toFixed(2)}</div>}
                      </div>
                      <button className="btn btn-danger btn-sm" onClick={e => { e.stopPropagation(); handleDelete(exp.id); }} style={{ flexShrink: 0 }}><Trash2 size={13} /></button>
                      {isOpen ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                    </div>
                    {isOpen && (
                      <div className="segment-details">
                        <div className="segment-detail-grid">
                          {exp.description && <div className="seg-detail" style={{ gridColumn: '1/-1' }}><span className="seg-label">Description</span><span>{exp.description}</span></div>}
                          <div className="seg-detail"><span className="seg-label">Exchange Rate</span><span>1 {exp.currency} = {exp.exchangeRate} {baseCurrency}</span></div>
                          {exp.paidBy && <div className="seg-detail"><span className="seg-label">Paid By</span><span>{exp.paidBy}</span></div>}
                          {exp.isReimbursable && <div className="seg-detail"><span className="seg-label">Reimbursable</span><span>{exp.isReimbursed ? '✅ Reimbursed' : '⏳ Pending'}</span></div>}
                          {exp.notes && <div className="seg-detail" style={{ gridColumn: '1/-1' }}><span className="seg-label">Notes</span><span>{exp.notes}</span></div>}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {showAddExpense && <AddExpenseModal tripId={tripId} baseCurrency={baseCurrency} onClose={() => setShowAddExpense(false)} />}
      {showSetBudget && <SetBudgetModal tripId={tripId} onClose={() => setShowSetBudget(false)} />}
    </div>
  );
}
