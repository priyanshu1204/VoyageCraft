import api from './axios';
import { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';
import { ApiResponse } from '../types/trip';

export const authApi = {
  register: (data: RegisterRequest) =>
    api.post<ApiResponse<AuthResponse>>('/auth/register', data),

  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthResponse>>('/auth/login', data),

  getMe: () =>
    api.get<ApiResponse<AuthResponse>>('/auth/me'),
};
