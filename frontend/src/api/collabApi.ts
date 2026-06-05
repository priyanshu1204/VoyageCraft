import api from './axios';
import type { PollRequest, PollResponse, CommentRequest, CommentResponse, ChangeLogResponse } from '../types/collab';
import type { ApiResponse } from '../types/trip';

export const collabApi = {
  // Polls
  createPoll: (data: PollRequest) =>
    api.post<ApiResponse<PollResponse>>('/collab/polls', data),
  getPolls: (tripId: number) =>
    api.get<ApiResponse<PollResponse[]>>(`/collab/polls/trip/${tripId}`),
  vote: (optionId: number) =>
    api.post<ApiResponse<PollResponse>>(`/collab/polls/vote/${optionId}`),
  closePoll: (pollId: number) =>
    api.put<ApiResponse<PollResponse>>(`/collab/polls/${pollId}/close`),
  deletePoll: (pollId: number) =>
    api.delete<ApiResponse<void>>(`/collab/polls/${pollId}`),

  // Comments
  addComment: (data: CommentRequest) =>
    api.post<ApiResponse<CommentResponse>>('/collab/comments', data),
  getComments: (tripId: number, itemType: string, itemId: number) =>
    api.get<ApiResponse<CommentResponse[]>>(`/collab/comments/trip/${tripId}?itemType=${itemType}&itemId=${itemId}`),
  deleteComment: (id: number) =>
    api.delete<ApiResponse<void>>(`/collab/comments/${id}`),

  // Change Log
  getChangeLog: (tripId: number) =>
    api.get<ApiResponse<ChangeLogResponse[]>>(`/collab/changelog/trip/${tripId}`),

  // Role
  getRole: (tripId: number) =>
    api.get<ApiResponse<{ role: string }>>(`/collab/role/trip/${tripId}`),
};
