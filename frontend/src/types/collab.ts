export type PollStatus = 'OPEN' | 'CLOSED';
export type ChangeAction = 'CREATED' | 'UPDATED' | 'DELETED' | 'VOTED' | 'COMMENTED' | 'BUDGET_SET' | 'EXPENSE_ADDED' | 'STATUS_CHANGED' | 'COLLABORATOR_ADDED';

export interface PollRequest {
  tripId: number;
  question: string;
  category?: string;
  options: string[];
  allowMultipleVotes?: boolean;
}

export interface PollOptionResponse {
  id: number;
  optionText: string;
  voteCount: number;
  votePercent: number;
  voterNames: string[];
  currentUserVoted: boolean;
}

export interface PollResponse {
  id: number;
  tripId: number;
  question: string;
  category?: string;
  status: PollStatus;
  allowMultipleVotes: boolean;
  createdByName: string;
  createdById: number;
  options: PollOptionResponse[];
  totalVotes: number;
  createdAt: string;
}

export interface CommentRequest {
  tripId: number;
  itemType: string;
  itemId: number;
  content: string;
  parentId?: number;
}

export interface CommentResponse {
  id: number;
  tripId: number;
  itemType: string;
  itemId: number;
  content: string;
  authorName: string;
  authorId: number;
  parentId?: number;
  replies: CommentResponse[];
  createdAt: string;
}

export interface ChangeLogResponse {
  id: number;
  action: ChangeAction;
  entityType: string;
  entityId: number;
  description: string;
  userName: string;
  userId: number;
  createdAt: string;
}
