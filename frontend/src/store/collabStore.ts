import { create } from 'zustand';
import { collabApi } from '../api/collabApi';
import type { PollResponse, PollRequest, CommentResponse, CommentRequest, ChangeLogResponse } from '../types/collab';

interface CollabState {
  polls: PollResponse[];
  comments: CommentResponse[];
  changeLog: ChangeLogResponse[];
  userRole: string;
  isLoading: boolean;

  fetchPolls: (tripId: number) => Promise<void>;
  createPoll: (data: PollRequest) => Promise<void>;
  vote: (optionId: number, tripId: number) => Promise<void>;
  closePoll: (pollId: number, tripId: number) => Promise<void>;
  deletePoll: (pollId: number, tripId: number) => Promise<void>;

  fetchComments: (tripId: number, itemType: string, itemId: number) => Promise<void>;
  addComment: (data: CommentRequest) => Promise<void>;
  deleteComment: (id: number, tripId: number, itemType: string, itemId: number) => Promise<void>;

  fetchChangeLog: (tripId: number) => Promise<void>;
  fetchRole: (tripId: number) => Promise<void>;
}

export const useCollabStore = create<CollabState>((set, get) => ({
  polls: [], comments: [], changeLog: [], userRole: 'VIEWER', isLoading: false,

  fetchPolls: async (tripId) => {
    set({ isLoading: true });
    try { const r = await collabApi.getPolls(tripId); set({ polls: r.data.data }); }
    finally { set({ isLoading: false }); }
  },
  createPoll: async (data) => {
    await collabApi.createPoll(data);
    await get().fetchPolls(data.tripId);
  },
  vote: async (optionId, tripId) => {
    await collabApi.vote(optionId);
    await get().fetchPolls(tripId);
  },
  closePoll: async (pollId, tripId) => {
    await collabApi.closePoll(pollId);
    await get().fetchPolls(tripId);
  },
  deletePoll: async (pollId, tripId) => {
    await collabApi.deletePoll(pollId);
    await get().fetchPolls(tripId);
  },

  fetchComments: async (tripId, itemType, itemId) => {
    try { const r = await collabApi.getComments(tripId, itemType, itemId); set({ comments: r.data.data }); }
    catch { /* ignore */ }
  },
  addComment: async (data) => {
    await collabApi.addComment(data);
    await get().fetchComments(data.tripId, data.itemType, data.itemId);
  },
  deleteComment: async (id, tripId, itemType, itemId) => {
    await collabApi.deleteComment(id);
    await get().fetchComments(tripId, itemType, itemId);
  },

  fetchChangeLog: async (tripId) => {
    try { const r = await collabApi.getChangeLog(tripId); set({ changeLog: r.data.data }); }
    catch { /* ignore */ }
  },
  fetchRole: async (tripId) => {
    try { const r = await collabApi.getRole(tripId); set({ userRole: r.data.data.role }); }
    catch { /* ignore */ }
  },
}));
