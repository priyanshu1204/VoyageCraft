import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useCollabStore } from '../store/collabStore';
import { useTripStore } from '../store/tripStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, Plus, X, BarChart3, MessageSquare, Clock, Trash2,
  Vote, Lock, Send, ChevronDown, ChevronUp, Shield,
} from 'lucide-react';
import type { PollRequest, CommentRequest } from '../types/collab';

const ACTION_ICONS: Record<string, string> = {
  CREATED: '🆕', UPDATED: '✏️', DELETED: '🗑️', VOTED: '🗳️',
  COMMENTED: '💬', BUDGET_SET: '💰', EXPENSE_ADDED: '💸',
  STATUS_CHANGED: '🔄', COLLABORATOR_ADDED: '👥',
};

// ── Create Poll Modal ────────────────────────────────────────────────
function CreatePollModal({ tripId, onClose }: { tripId: number; onClose: () => void }) {
  const { createPoll } = useCollabStore();
  const [form, setForm] = useState<PollRequest>({ tripId, question: '', options: ['', ''], allowMultipleVotes: false });
  const [saving, setSaving] = useState(false);

  const addOption = () => setForm({ ...form, options: [...form.options, ''] });
  const removeOption = (i: number) => {
    if (form.options.length <= 2) return;
    setForm({ ...form, options: form.options.filter((_, idx) => idx !== i) });
  };
  const updateOption = (i: number, val: string) => {
    const opts = [...form.options]; opts[i] = val;
    setForm({ ...form, options: opts });
  };

  const handleSave = async () => {
    if (!form.question.trim()) { toast.error('Enter a question'); return; }
    const validOpts = form.options.filter(o => o.trim());
    if (validOpts.length < 2) { toast.error('Need at least 2 options'); return; }
    setSaving(true);
    try { await createPoll({ ...form, options: validOpts }); toast.success('Poll created!'); onClose(); }
    catch { toast.error('Failed'); }
    finally { setSaving(false); }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 520 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
          <h3 style={{ margin: 0 }}>🗳️ Create Poll</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
        </div>
        <div className="input-group">
          <label>Question *</label>
          <input className="input" placeholder="e.g. Which activity should we do on Day 3?" value={form.question} onChange={e => setForm({ ...form, question: e.target.value })} />
        </div>
        <div className="input-group" style={{ marginTop: 12 }}>
          <label>Category</label>
          <select className="input" value={form.category || ''} onChange={e => setForm({ ...form, category: e.target.value })}>
            <option value="">General</option>
            <option value="activity">Activity</option>
            <option value="date">Date</option>
            <option value="stay">Stay</option>
            <option value="transport">Transport</option>
          </select>
        </div>
        <div style={{ marginTop: 16 }}>
          <label style={{ fontSize: 13, fontWeight: 600, marginBottom: 8, display: 'block' }}>Options *</label>
          {form.options.map((opt, i) => (
            <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
              <input className="input" style={{ flex: 1 }} placeholder={`Option ${i + 1}`} value={opt} onChange={e => updateOption(i, e.target.value)} />
              {form.options.length > 2 && <button className="btn btn-ghost btn-sm" onClick={() => removeOption(i)}><X size={14} /></button>}
            </div>
          ))}
          <button className="btn btn-ghost btn-sm" onClick={addOption} style={{ marginTop: 4 }}>+ Add Option</button>
        </div>
        <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 12, fontSize: 13 }}>
          <input type="checkbox" checked={form.allowMultipleVotes} onChange={e => setForm({ ...form, allowMultipleVotes: e.target.checked })} />
          Allow multiple votes per person
        </label>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 20 }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Creating...' : 'Create Poll'}</button>
        </div>
      </div>
    </div>
  );
}

// ── Main Page ────────────────────────────────────────────────────────
export default function CollaborationPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();
  const { polls, comments, changeLog, userRole, isLoading, fetchPolls, fetchComments, fetchChangeLog, fetchRole, vote, closePoll, deletePoll, addComment, deleteComment } = useCollabStore();
  const { currentTrip, fetchTrip } = useTripStore();
  const [tab, setTab] = useState<'polls' | 'comments' | 'changelog'>('polls');
  const [showCreatePoll, setShowCreatePoll] = useState(false);
  const [commentItemType, setCommentItemType] = useState('general');
  const [commentItemId, setCommentItemId] = useState(0);
  const [commentText, setCommentText] = useState('');
  const [replyTo, setReplyTo] = useState<number | null>(null);
  const [replyText, setReplyText] = useState('');
  const [expandedPoll, setExpandedPoll] = useState<number | null>(null);

  useEffect(() => {
    fetchPolls(tripId); fetchChangeLog(tripId); fetchRole(tripId);
    fetchComments(tripId, commentItemType, commentItemId);
    if (!currentTrip || currentTrip.id !== tripId) fetchTrip(tripId);
  }, [tripId]);

  useEffect(() => { fetchComments(tripId, commentItemType, commentItemId); }, [commentItemType, commentItemId]);

  const canEdit = userRole === 'OWNER' || userRole === 'EDITOR';
  const isOwner = userRole === 'OWNER';

  const handleVote = async (optionId: number) => {
    try { await vote(optionId, tripId); } catch { toast.error('Vote failed'); }
  };
  const handleClosePoll = async (pollId: number) => {
    try { await closePoll(pollId, tripId); toast.success('Poll closed'); } catch { toast.error('Failed'); }
  };
  const handleDeletePoll = async (pollId: number) => {
    try { await deletePoll(pollId, tripId); toast.success('Poll deleted'); } catch { toast.error('Failed'); }
  };
  const handleAddComment = async () => {
    if (!commentText.trim()) return;
    try {
      await addComment({ tripId, itemType: commentItemType, itemId: commentItemId, content: commentText });
      setCommentText(''); toast.success('Comment added');
    } catch { toast.error('Failed'); }
  };
  const handleReply = async (parentId: number) => {
    if (!replyText.trim()) return;
    try {
      await addComment({ tripId, itemType: commentItemType, itemId: commentItemId, content: replyText, parentId });
      setReplyText(''); setReplyTo(null); toast.success('Reply added');
    } catch { toast.error('Failed'); }
  };
  const handleDeleteComment = async (cid: number) => {
    try { await deleteComment(cid, tripId, commentItemType, commentItemId); toast.success('Deleted'); } catch { toast.error('Failed'); }
  };

  const roleBadge = (
    <span className="segment-type-badge" style={{
      background: isOwner ? '#22c55e20' : canEdit ? '#3b82f620' : '#f59e0b20',
      color: isOwner ? '#22c55e' : canEdit ? '#3b82f6' : '#f59e0b'
    }}>
      <Shield size={12} /> {userRole}
    </span>
  );

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${tripId}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Vote size={22} color="white" />
          </div>
          <div>
            <h2 style={{ display: 'flex', alignItems: 'center', gap: 8 }}>Collaboration {roleBadge}</h2>
            <p>{currentTrip?.title || `Trip #${tripId}`}</p>
          </div>
        </div>
      </div>

      {/* Tab Bar */}
      <div className="tab-row">
        <button className={`tab-btn ${tab === 'polls' ? 'active' : ''}`} onClick={() => setTab('polls')}><BarChart3 size={14} /> Polls ({polls.length})</button>
        <button className={`tab-btn ${tab === 'comments' ? 'active' : ''}`} onClick={() => setTab('comments')}><MessageSquare size={14} /> Comments</button>
        <button className={`tab-btn ${tab === 'changelog' ? 'active' : ''}`} onClick={() => setTab('changelog')}><Clock size={14} /> Change Log ({changeLog.length})</button>
      </div>

      {/* POLLS TAB */}
      {tab === 'polls' && (
        <div className="animate-in">
          {canEdit && (
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
              <button className="btn btn-primary btn-sm" onClick={() => setShowCreatePoll(true)}><Plus size={14} /> Create Poll</button>
            </div>
          )}
          {isLoading ? <div className="spinner" /> : polls.length === 0 ? (
            <div className="empty-state">
              <BarChart3 size={48} /><h3>No polls yet</h3><p>Create a poll to gather votes from your group.</p>
              {canEdit && <button className="btn btn-primary" onClick={() => setShowCreatePoll(true)}><Plus size={16} /> Create Poll</button>}
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {polls.map(poll => {
                const isOpen = poll.status === 'OPEN';
                const expanded = expandedPoll === poll.id;
                return (
                  <div key={poll.id} className="activity-card">
                    <div className="stay-header" onClick={() => setExpandedPoll(expanded ? null : poll.id)} style={{ cursor: 'pointer' }}>
                      <span className="segment-type-badge" style={{ background: isOpen ? '#22c55e20' : '#64748b20', color: isOpen ? '#22c55e' : '#64748b' }}>
                        {isOpen ? '🟢 Open' : '🔒 Closed'}
                      </span>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 600, fontSize: 14 }}>{poll.question}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>by {poll.createdByName} · {poll.totalVotes} vote{poll.totalVotes !== 1 ? 's' : ''}{poll.category ? ` · ${poll.category}` : ''}</div>
                      </div>
                      {expanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                    </div>
                    {expanded && (
                      <div className="segment-details">
                        {poll.options.map(opt => (
                          <div key={opt.id} style={{ marginBottom: 10 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <button
                                  className={`btn btn-sm ${opt.currentUserVoted ? 'btn-primary' : 'btn-secondary'}`}
                                  onClick={() => isOpen && handleVote(opt.id)}
                                  disabled={!isOpen}
                                  style={{ minWidth: 32 }}
                                >
                                  {opt.currentUserVoted ? '✓' : '○'}
                                </button>
                                <span style={{ fontSize: 14 }}>{opt.optionText}</span>
                              </div>
                              <span style={{ fontSize: 13, fontWeight: 600 }}>{opt.voteCount} ({opt.votePercent.toFixed(0)}%)</span>
                            </div>
                            <div style={{ width: '100%', height: 6, background: 'var(--bg-input)', borderRadius: 3, overflow: 'hidden' }}>
                              <div style={{ width: `${opt.votePercent}%`, height: '100%', background: opt.currentUserVoted ? '#6366f1' : '#94a3b8', borderRadius: 3, transition: 'width 0.3s' }} />
                            </div>
                            {opt.voterNames.length > 0 && (
                              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 2 }}>
                                {opt.voterNames.join(', ')}
                              </div>
                            )}
                          </div>
                        ))}
                        {isOwner && (
                          <div style={{ display: 'flex', gap: 8, marginTop: 12, borderTop: '1px solid var(--border-subtle)', paddingTop: 12 }}>
                            {isOpen && <button className="btn btn-secondary btn-sm" onClick={() => handleClosePoll(poll.id)}><Lock size={13} /> Close Poll</button>}
                            <button className="btn btn-danger btn-sm" onClick={() => handleDeletePoll(poll.id)}><Trash2 size={13} /> Delete</button>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* COMMENTS TAB */}
      {tab === 'comments' && (
        <div className="animate-in">
          <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
            <select className="input" style={{ width: 'auto' }} value={commentItemType} onChange={e => { setCommentItemType(e.target.value); setCommentItemId(0); }}>
              <option value="general">💬 General</option>
              <option value="activity">🎯 Activity</option>
              <option value="stay">🏨 Stay</option>
              <option value="transport">✈️ Transport</option>
              <option value="itinerary">📋 Itinerary</option>
            </select>
            {commentItemType !== 'general' && (
              <input className="input" type="number" style={{ width: 120 }} placeholder="Item ID" value={commentItemId || ''} onChange={e => setCommentItemId(parseInt(e.target.value) || 0)} />
            )}
          </div>
          {/* Add comment */}
          {canEdit && (
            <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
              <input className="input" style={{ flex: 1 }} placeholder="Write a comment..." value={commentText} onChange={e => setCommentText(e.target.value)} onKeyDown={e => e.key === 'Enter' && handleAddComment()} />
              <button className="btn btn-primary btn-sm" onClick={handleAddComment}><Send size={14} /></button>
            </div>
          )}
          {!canEdit && <div style={{ padding: 12, background: 'var(--bg-input)', borderRadius: 'var(--radius-sm)', marginBottom: 16, fontSize: 13, color: 'var(--text-muted)' }}>🔒 You have VIEWER permissions — commenting is disabled.</div>}
          {comments.length === 0 ? (
            <div className="empty-state">
              <MessageSquare size={48} /><h3>No comments yet</h3><p>Start a discussion about this trip.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {comments.map(c => (
                <div key={c.id} style={{ padding: '12px 16px', background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                    <span style={{ fontWeight: 600, fontSize: 13 }}>👤 {c.authorName}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>{new Date(c.createdAt).toLocaleString()}</span>
                      {isOwner && <button className="btn btn-ghost btn-sm" onClick={() => handleDeleteComment(c.id)}><Trash2 size={12} /></button>}
                    </div>
                  </div>
                  <p style={{ margin: 0, fontSize: 14 }}>{c.content}</p>
                  {/* Replies */}
                  {c.replies && c.replies.length > 0 && (
                    <div style={{ marginTop: 10, marginLeft: 20, borderLeft: '2px solid var(--border-subtle)', paddingLeft: 12 }}>
                      {c.replies.map(r => (
                        <div key={r.id} style={{ marginBottom: 8 }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ fontWeight: 600, fontSize: 12 }}>↳ {r.authorName}</span>
                            <span style={{ fontSize: 10, color: 'var(--text-muted)' }}>{new Date(r.createdAt).toLocaleString()}</span>
                          </div>
                          <p style={{ margin: 0, fontSize: 13 }}>{r.content}</p>
                        </div>
                      ))}
                    </div>
                  )}
                  {/* Reply input */}
                  {canEdit && (
                    replyTo === c.id ? (
                      <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                        <input className="input" style={{ flex: 1, fontSize: 13 }} placeholder="Reply..." value={replyText} onChange={e => setReplyText(e.target.value)} onKeyDown={e => e.key === 'Enter' && handleReply(c.id)} autoFocus />
                        <button className="btn btn-primary btn-sm" onClick={() => handleReply(c.id)}><Send size={12} /></button>
                        <button className="btn btn-ghost btn-sm" onClick={() => { setReplyTo(null); setReplyText(''); }}><X size={12} /></button>
                      </div>
                    ) : (
                      <button className="btn btn-ghost btn-sm" onClick={() => setReplyTo(c.id)} style={{ marginTop: 6, fontSize: 12 }}>↩ Reply</button>
                    )
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* CHANGELOG TAB */}
      {tab === 'changelog' && (
        <div className="animate-in">
          {changeLog.length === 0 ? (
            <div className="empty-state">
              <Clock size={48} /><h3>No changes recorded</h3><p>Actions on this trip will appear here.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {changeLog.map(cl => (
                <div key={cl.id} style={{ display: 'flex', alignItems: 'flex-start', gap: 12, padding: '10px 14px', background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-sm)' }}>
                  <span style={{ fontSize: 18 }}>{ACTION_ICONS[cl.action] || '📝'}</span>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 13 }}>{cl.description}</div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 2 }}>{new Date(cl.createdAt).toLocaleString()}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {showCreatePoll && <CreatePollModal tripId={tripId} onClose={() => setShowCreatePoll(false)} />}
    </div>
  );
}
