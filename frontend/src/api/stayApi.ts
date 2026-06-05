import api from './axios';
import type { StayRequest, StayResponse } from '../types/stay';
import type { ApiResponse } from '../types/trip';

export const stayApi = {
  add: (data: StayRequest) =>
    api.post<ApiResponse<StayResponse>>('/stays', data),

  update: (id: number, data: StayRequest) =>
    api.put<ApiResponse<StayResponse>>(`/stays/${id}`, data),

  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/stays/${id}`),

  getAll: (tripId: number) =>
    api.get<ApiResponse<StayResponse[]>>(`/stays/trip/${tripId}`),

  getOne: (id: number) =>
    api.get<ApiResponse<StayResponse>>(`/stays/${id}`),
};
