import { UserResponse } from './auth';

export type TripStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type TripPace = 'RELAXED' | 'STANDARD' | 'INTENSE';
export type CollaboratorRole = 'OWNER' | 'EDITOR' | 'VIEWER';
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export interface DestinationRequest {
  destinationName: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  arrivalDate?: string;
  departureDate?: string;
  orderIndex?: number;
  notes?: string;
}

export interface DestinationResponse {
  id: number;
  destinationName: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  arrivalDate?: string;
  departureDate?: string;
  orderIndex: number;
  notes?: string;
}

export interface CollaboratorResponse {
  id: number;
  user: UserResponse;
  role: CollaboratorRole;
  invitationStatus: InvitationStatus;
  invitedAt?: string;
  respondedAt?: string;
  tripId?: number;
  tripTitle?: string;
  invitedByName?: string;
}

export interface TripRequest {
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  budgetTotal?: number;
  currency?: string;
  coverImageUrl?: string;
  pace?: TripPace;
  destinations?: DestinationRequest[];
}

export interface TripResponse {
  id: number;
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  status: TripStatus;
  budgetTotal?: number;
  currency: string;
  coverImageUrl?: string;
  pace: TripPace;
  createdBy: UserResponse;
  createdAt: string;
  updatedAt: string;
  destinations: DestinationResponse[];
  collaborators: CollaboratorResponse[];
  daysUntilTrip: number;
  totalDays: number;
}

export interface TripDashboardResponse {
  totalTrips: number;
  planningTrips: number;
  activeTrips: number;
  completedTrips: number;
  upcomingTrips: TripResponse[];
  recentTrips: TripResponse[];
}

export interface TemplateResponse {
  id: number;
  name: string;
  description?: string;
  destinationsJson?: string;
  durationDays?: number;
  budgetEstimate?: number;
  category?: string;
  coverImageUrl?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
