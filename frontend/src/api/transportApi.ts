import api from './axios';
import type {
  TransportRequest,
  TransportResponse,
  TransportConflictResponse,
  MockTransportOption,
  TransportType,
} from '../types/transport';
import type { ApiResponse } from '../types/trip';

export const transportApi = {
  add: (data: TransportRequest) =>
    api.post<ApiResponse<TransportResponse>>('/transports', data),

  update: (id: number, data: TransportRequest) =>
    api.put<ApiResponse<TransportResponse>>(`/transports/${id}`, data),

  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/transports/${id}`),

  getAll: (tripId: number) =>
    api.get<ApiResponse<TransportResponse[]>>(`/transports/trip/${tripId}`),

  getOne: (id: number) =>
    api.get<ApiResponse<TransportResponse>>(`/transports/${id}`),

  detectConflicts: (tripId: number) =>
    api.get<ApiResponse<TransportConflictResponse[]>>(`/transports/trip/${tripId}/conflicts`),

  searchMock: (params: {
    type: TransportType;
    from: string;
    to: string;
    date: string;
    fromTimezone?: string;
    toTimezone?: string;
  }) =>
    api.get<ApiResponse<MockTransportOption[]>>('/transports/search', { params }),
};
