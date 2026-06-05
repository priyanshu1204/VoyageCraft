import { useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAnalyticsStore } from '../store/analyticsStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, BarChart3, PieChart, TrendingUp, Clock, Plane, Activity,
  DollarSign, Download, Zap, Target, AlertTriangle, CheckCircle,
} from 'lucide-react';

const STATUS_COLORS: Record<string, string> = {
  UNDER: '#22c55e', ON_TRACK: '#f59e0b', OVER: '#ef4444', NO_BUDGET: '#6366f1',
};

function HBar({ percent, color, maxPercent = 100 }: { percent: number; color: string; maxPercent?: number }) {
  const w = Math.min((percent / maxPercent) * 100, 100);
  return (
    <div style={{ width: '100%', height: 10, background: 'var(--bg-input)', borderRadius: 5, overflow: 'hidden' }}>
      <div style={{ width: `${w}%`, height: '100%', background: color, borderRadius: 5, transition: 'width 0.6s ease' }} />
    </div>
  );
}

function DonutChart({ segments, size = 160 }: { segments: { label: string; percent: number; color: string }[]; size?: number }) {
  const r = size / 2 - 12;
  const c = 2 * Math.PI * r;
  let offset = 0;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      {segments.map((s, i) => {
        const dash = (s.percent / 100) * c;
        const gap = c - dash;
        const el = (
          <circle key={i} cx={size / 2} cy={size / 2} r={r} fill="none" stroke={s.color} strokeWidth={20}
            strokeDasharray={`${dash} ${gap}`} strokeDashoffset={-offset}
            style={{ transition: 'stroke-dasharray 0.6s ease, stroke-dashoffset 0.6s ease' }}
          />
        );
        offset += dash;
        return el;
      })}
      <circle cx={size / 2} cy={size / 2} r={r - 14} fill="var(--bg-card)" />
    </svg>
  );
}

function SpendBarChart({ data, currency }: { data: { date: string; amount: number }[]; currency: string }) {
  if (data.length === 0) return null;
  const max = Math.max(...data.map(d => d.amount), 1);
  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', gap: 4, height: 130, padding: '0 4px' }}>
      {data.map((d, i) => (
        <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
          <span style={{ fontSize: 10, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
            {currency} {d.amount.toFixed(0)}
          </span>
          <div style={{
            width: '100%', maxWidth: 40, minHeight: 4,
            height: `${(d.amount / max) * 100}px`,
            background: 'linear-gradient(180deg, #6366f1, #818cf8)',
            borderRadius: '4px 4px 0 0',
            transition: 'height 0.5s ease',
          }} />
          <span style={{ fontSize: 9, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
            {d.date.slice(5)}
          </span>
        </div>
      ))}
    </div>
  );
}

export default function AnalyticsPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();
  const { analytics, isLoading, fetchAnalytics } = useAnalyticsStore();
  const printRef = useRef<HTMLDivElement>(null);

  useEffect(() => { fetchAnalytics(tripId); }, [tripId]);

  const handleExportPDF = () => {
    window.print();
  };

  const handleExportCSV = () => {
    if (!analytics) return;
    const rows: string[][] = [['Metric', 'Value']];
    rows.push(['Trip', analytics.tripTitle]);
    rows.push(['Total Days', String(analytics.totalDays)]);
    rows.push(['Total Budget', `${analytics.currency} ${analytics.totalBudget}`]);
    rows.push(['Total Spent', `${analytics.currency} ${analytics.totalSpent}`]);
    rows.push(['Remaining', `${analytics.currency} ${analytics.totalRemaining}`]);
    rows.push(['Budget Used %', `${analytics.budgetUsedPercent}%`]);
    rows.push(['Avg Daily Spend', `${analytics.currency} ${analytics.avgDailySpend}`]);
    rows.push(['Activities', String(analytics.totalActivities)]);
    rows.push(['Transport Legs', String(analytics.totalTransportLegs)]);
    rows.push(['Transit:Activity Ratio', analytics.transitActivityRatio]);
    rows.push([]);
    rows.push(['Category', 'Budgeted', 'Actual', 'Variance', '% Used', 'Status']);
    analytics.budgetVsActual.forEach(c => {
      rows.push([c.categoryLabel, String(c.budgeted), String(c.actual), String(c.variance), `${c.percentUsed}%`, c.status]);
    });
    rows.push([]);
    rows.push(['Activity Category', 'Minutes', '% of Total', 'Count']);
    analytics.timeByCategory.forEach(t => {
      rows.push([t.categoryLabel, String(t.totalMinutes), `${t.percent}%`, String(t.activityCount)]);
    });

    const csv = rows.map(r => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `trip_${tripId}_analytics.csv`; a.click();
    URL.revokeObjectURL(url);
    toast.success('CSV exported!');
  };

  if (isLoading) return <div className="animate-in" style={{ textAlign: 'center', padding: 60 }}><div className="spinner" /></div>;
  if (!analytics) return <div className="animate-in empty-state"><BarChart3 size={48} /><h3>No analytics data available</h3></div>;

  const a = analytics;
  const budgetStatus = a.budgetUsedPercent > 100 ? 'over' : a.budgetUsedPercent > 80 ? 'warning' : 'good';

  const timeDonutSegments = [
    { label: 'Activities', percent: a.activityPercent, color: '#6366f1' },
    { label: 'Transit', percent: a.transitPercent, color: '#f59e0b' },
    { label: 'Free Time', percent: a.freeTimePercent, color: '#22c55e33' },
  ];

  return (
    <div className="animate-in" ref={printRef}>
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      {/* Header */}
      <div className="page-header" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <BarChart3 size={22} color="white" />
          </div>
          <div>
            <h2>Trip Analytics</h2>
            <p>{a.tripTitle} · {a.totalDays} days · {a.currency}</p>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-secondary btn-sm" onClick={handleExportCSV}><Download size={14} /> Export CSV</button>
          <button className="btn btn-primary btn-sm" onClick={handleExportPDF}><Download size={14} /> Print / PDF</button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="itinerary-stats-bar" style={{ marginBottom: 24 }}>
        <div className="i-stat">
          <DollarSign size={15} style={{ color: '#22c55e' }} />
          <span className="i-stat-val">{a.currency} {a.totalBudget.toFixed(0)}</span>
          <span className="i-stat-label">total budget</span>
        </div>
        <div className="i-stat">
          <TrendingUp size={15} style={{ color: budgetStatus === 'over' ? '#ef4444' : budgetStatus === 'warning' ? '#f59e0b' : '#22c55e' }} />
          <span className="i-stat-val">{a.currency} {a.totalSpent.toFixed(0)}</span>
          <span className="i-stat-label">spent ({a.budgetUsedPercent}%)</span>
        </div>
        <div className="i-stat">
          <Zap size={15} style={{ color: '#6366f1' }} />
          <span className="i-stat-val">{a.totalActivities}</span>
          <span className="i-stat-label">activities</span>
        </div>
        <div className="i-stat">
          <Plane size={15} style={{ color: '#f59e0b' }} />
          <span className="i-stat-val">{a.totalTransportLegs}</span>
          <span className="i-stat-label">transport legs</span>
        </div>
        <div className="i-stat">
          <Target size={15} style={{ color: '#ec4899' }} />
          <span className="i-stat-val">{a.transitActivityRatio}</span>
          <span className="i-stat-label">transit:activity</span>
        </div>
      </div>

      {/* Budget vs Actual */}
      <div className="card" style={{ marginBottom: 24 }}>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 20 }}>
          <PieChart size={18} style={{ color: '#6366f1' }} /> Budget vs. Actual Spending
        </h3>

        {/* Overall progress */}
        <div style={{ marginBottom: 20, padding: '12px 16px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: 13 }}>
            <span style={{ fontWeight: 600 }}>Overall: {a.currency} {a.totalSpent.toFixed(0)} / {a.totalBudget.toFixed(0)}</span>
            <span style={{ color: budgetStatus === 'over' ? '#ef4444' : 'var(--text-muted)', fontWeight: 600 }}>{a.budgetUsedPercent}%</span>
          </div>
          <HBar percent={a.budgetUsedPercent} color={budgetStatus === 'over' ? '#ef4444' : budgetStatus === 'warning' ? '#f59e0b' : '#6366f1'} />
        </div>

        {a.budgetVsActual.length > 0 ? (
          <div style={{ display: 'grid', gap: 10 }}>
            {a.budgetVsActual.map(c => (
              <div key={c.category} style={{ display: 'grid', gridTemplateColumns: '140px 1fr 100px 70px', alignItems: 'center', gap: 12, padding: '8px 0', borderBottom: '1px solid var(--border-subtle)' }}>
                <span style={{ fontSize: 13, fontWeight: 500 }}>{c.emoji} {c.categoryLabel}</span>
                <div>
                  <HBar percent={c.percentUsed || (c.status === 'NO_BUDGET' ? 100 : 0)} color={STATUS_COLORS[c.status]} maxPercent={100} />
                </div>
                <span style={{ fontSize: 12, textAlign: 'right' }}>
                  {a.currency} {c.actual.toFixed(0)}{c.budgeted > 0 && ` / ${c.budgeted.toFixed(0)}`}
                </span>
                <span style={{ fontSize: 11, textAlign: 'right', color: STATUS_COLORS[c.status], fontWeight: 600 }}>
                  {c.status === 'OVER' ? '⚠️ Over' : c.status === 'ON_TRACK' ? '⚡ Track' : c.status === 'NO_BUDGET' ? '—' : '✅ Under'}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state" style={{ padding: 24 }}>
            <PieChart size={32} style={{ color: 'var(--text-muted)' }} />
            <p>No budget or expense data yet. Add expenses and set category budgets to see comparisons.</p>
          </div>
        )}
      </div>

      {/* Time Allocation + Transit Ratio Row */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 24 }}>
        {/* Time Allocation */}
        <div className="card">
          <h3 style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 20 }}>
            <Clock size={18} style={{ color: '#f59e0b' }} /> Time Allocation by Category
          </h3>
          {a.timeByCategory.length > 0 ? (
            <div style={{ display: 'flex', gap: 24, alignItems: 'center' }}>
              <DonutChart segments={a.timeByCategory.map((t, i) => ({
                label: t.categoryLabel,
                percent: t.percent,
                color: ['#6366f1', '#f59e0b', '#22c55e', '#ef4444', '#ec4899', '#14b8a6', '#a855f7', '#3b82f6'][i % 8],
              }))} />
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6 }}>
                {a.timeByCategory.map((t, i) => (
                  <div key={t.category} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13 }}>
                    <div style={{ width: 10, height: 10, borderRadius: '50%', background: ['#6366f1', '#f59e0b', '#22c55e', '#ef4444', '#ec4899', '#14b8a6', '#a855f7', '#3b82f6'][i % 8], flexShrink: 0 }} />
                    <span style={{ flex: 1 }}>{t.emoji} {t.categoryLabel}</span>
                    <span style={{ fontWeight: 600 }}>{t.percent}%</span>
                    <span style={{ color: 'var(--text-muted)', fontSize: 11 }}>{Math.floor(t.totalMinutes / 60)}h{t.totalMinutes % 60}m</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="empty-state" style={{ padding: 24 }}>
              <Clock size={32} style={{ color: 'var(--text-muted)' }} />
              <p>Add activities with start/end times to see time allocation.</p>
            </div>
          )}
        </div>

        {/* Transit vs Activity */}
        <div className="card">
          <h3 style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 20 }}>
            <Activity size={18} style={{ color: '#22c55e' }} /> Transit vs. Activity Ratio
          </h3>
          <div style={{ display: 'flex', gap: 24, alignItems: 'center' }}>
            <DonutChart segments={timeDonutSegments.filter(s => s.percent > 0)} size={140} />
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div style={{ padding: '10px 14px', borderRadius: 'var(--radius-md)', background: 'rgba(99, 102, 241, 0.1)' }}>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2 }}>🎯 Activities</div>
                  <div style={{ fontWeight: 700, fontSize: 18 }}>{a.activityPercent}%</div>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{a.avgActivityMinutesPerDay} min/day avg</div>
                </div>
                <div style={{ padding: '10px 14px', borderRadius: 'var(--radius-md)', background: 'rgba(245, 158, 11, 0.1)' }}>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2 }}>🚗 Transit</div>
                  <div style={{ fontWeight: 700, fontSize: 18 }}>{a.transitPercent}%</div>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{a.avgTransitMinutesPerDay} min/day avg</div>
                </div>
                <div style={{ padding: '10px 14px', borderRadius: 'var(--radius-md)', background: 'rgba(34, 197, 94, 0.1)' }}>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 2 }}>☀️ Free Time</div>
                  <div style={{ fontWeight: 700, fontSize: 18 }}>{a.freeTimePercent}%</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Spending Insights */}
      <div className="card" style={{ marginBottom: 24 }}>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 20 }}>
          <TrendingUp size={18} style={{ color: '#ec4899' }} /> Spending Insights
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: 20 }}>
          <div style={{ padding: '14px', borderRadius: 'var(--radius-md)', background: 'var(--bg-main)', textAlign: 'center' }}>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>💰 Avg Daily</div>
            <div style={{ fontWeight: 700, fontSize: 18 }}>{a.currency} {a.avgDailySpend.toFixed(0)}</div>
          </div>
          <div style={{ padding: '14px', borderRadius: 'var(--radius-md)', background: 'var(--bg-main)', textAlign: 'center' }}>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>🔥 Biggest Expense</div>
            <div style={{ fontWeight: 700, fontSize: 18 }}>{a.currency} {a.highestSingleExpense.toFixed(0)}</div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{a.highestExpenseTitle}</div>
          </div>
          <div style={{ padding: '14px', borderRadius: 'var(--radius-md)', background: 'var(--bg-main)', textAlign: 'center' }}>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>📊 Top Category</div>
            <div style={{ fontWeight: 700, fontSize: 16 }}>{a.topSpendingCategory}</div>
          </div>
          <div style={{ padding: '14px', borderRadius: 'var(--radius-md)', background: 'var(--bg-main)', textAlign: 'center' }}>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>
              {a.totalRemaining >= 0 ? <CheckCircle size={13} style={{ color: '#22c55e' }} /> : <AlertTriangle size={13} style={{ color: '#ef4444' }} />}
              {' '}Remaining
            </div>
            <div style={{ fontWeight: 700, fontSize: 18, color: a.totalRemaining < 0 ? '#ef4444' : '#22c55e' }}>
              {a.currency} {a.totalRemaining.toFixed(0)}
            </div>
          </div>
        </div>

        {/* Daily Spending Trend Bar Chart */}
        {a.dailySpendTrend.length > 0 && (
          <div>
            <h4 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>📈 Daily Spending Trend</h4>
            <SpendBarChart data={a.dailySpendTrend} currency={a.currency} />
          </div>
        )}
      </div>
    </div>
  );
}
