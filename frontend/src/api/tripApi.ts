import api from './axios';
import {
  TripRequest, TripResponse, TripDashboardResponse,
  DestinationRequest, DestinationResponse,
  CollaboratorResponse, TemplateResponse, ApiResponse
} from '../types/trip';

export const tripApi = {
  create: (data: TripRequest) =>
    api.post<ApiResponse<TripResponse>>('/trips', data),

  getAll: () =>
    api.get<ApiResponse<TripResponse[]>>('/trips'),

  getById: (id: number) =>
    api.get<ApiResponse<TripResponse>>(`/trips/${id}`),

  update: (id: number, data: TripRequest) =>
    api.put<ApiResponse<TripResponse>>(`/trips/${id}`, data),

  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/trips/${id}`),

  getDashboard: () =>
    api.get<ApiResponse<TripDashboardResponse>>('/trips/dashboard'),

  // Destinations
  addDestination: (tripId: number, data: DestinationRequest) =>
    api.post<ApiResponse<DestinationResponse>>(`/trips/${tripId}/destinations`, data),

  updateDestination: (tripId: number, destId: number, data: DestinationRequest) =>
    api.put<ApiResponse<DestinationResponse>>(`/trips/${tripId}/destinations/${destId}`, data),

  deleteDestination: (tripId: number, destId: number) =>
    api.delete<ApiResponse<void>>(`/trips/${tripId}/destinations/${destId}`),

  // Collaborators
  inviteCollaborator: (tripId: number, data: { email: string; role: string }) =>
    api.post<ApiResponse<CollaboratorResponse>>(`/trips/${tripId}/collaborators`, data),

  updateCollaborator: (tripId: number, collabId: number, data: Record<string, string>) =>
    api.put<ApiResponse<CollaboratorResponse>>(`/trips/${tripId}/collaborators/${collabId}`, data),

  removeCollaborator: (tripId: number, collabId: number) =>
    api.delete<ApiResponse<void>>(`/trips/${tripId}/collaborators/${collabId}`),

  getPendingInvitations: () =>
    api.get<ApiResponse<CollaboratorResponse[]>>('/trips/0/collaborators/pending'),

  respondToInvitation: (tripId: number, collabId: number, accept: boolean) =>
    api.put<ApiResponse<CollaboratorResponse>>(`/trips/${tripId}/collaborators/${collabId}`, { accept: String(accept) }),

  // Templates
  getTemplates: () =>
    api.get<ApiResponse<TemplateResponse[]>>('/templates'),

  createFromTemplate: (templateId: number, startDate: string) =>
    api.post<ApiResponse<TripResponse>>(`/templates/${templateId}/create-trip`, { startDate }),
};
