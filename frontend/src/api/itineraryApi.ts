import api from './axios';
import type {
  GenerateItineraryRequest,
  ItineraryResponse,
  ItinerarySummaryResponse,
  ItineraryCompareResponse,
} from '../types/itinerary';
import type { ApiResponse } from '../types/trip';

export const itineraryApi = {
  generate: (data: GenerateItineraryRequest) =>
    api.post<ApiResponse<ItineraryResponse>>('/itineraries/generate', data),

  getHistory: (tripId: number) =>
    api.get<ApiResponse<ItinerarySummaryResponse[]>>(`/itineraries/trip/${tripId}/history`),

  getOne: (itineraryId: number) =>
    api.get<ApiResponse<ItineraryResponse>>(`/itineraries/${itineraryId}`),

  activate: (itineraryId: number) =>
    api.put<ApiResponse<ItineraryResponse>>(`/itineraries/${itineraryId}/activate`),

  delete: (itineraryId: number) =>
    api.delete<ApiResponse<void>>(`/itineraries/${itineraryId}`),

  compare: (versionAId: number, versionBId: number) =>
    api.get<ApiResponse<ItineraryCompareResponse>>(
      `/itineraries/compare?versionAId=${versionAId}&versionBId=${versionBId}`
    ),
};
