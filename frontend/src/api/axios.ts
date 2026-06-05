import axios from 'axios';
import { useOfflineStore } from '../store/offlineStore';

const api = axios.create({
  baseURL: 'http://localhost:8081/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else if (!error.response || !navigator.onLine) {
      // Network error / offline
      const config = error.config;
      if (config && ['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase() || '')) {
        // Parse entity type from URL (simple heuristic)
        const url = config.url || '';
        let entityType = 'UNKNOWN';
        if (url.includes('/itinerary')) entityType = 'ITINERARY_ITEM';
        else if (url.includes('/transport')) entityType = 'TRANSPORT';
        else if (url.includes('/stays')) entityType = 'STAY';
        else if (url.includes('/activities')) entityType = 'ACTIVITY';
        else if (url.includes('/packing')) entityType = 'PACKING_ITEM';

        // Add to offline queue
        useOfflineStore.getState().addPendingChange({
          entityType,
          entityId: config.method?.toLowerCase() !== 'post' ? parseInt(url.split('/').pop() || '0') : null,
          action: config.method?.toUpperCase() as 'CREATE' | 'UPDATE' | 'DELETE',
          payload: config.data || '{}',
          timestamp: new Date().toISOString(),
        });
      }
    }
    return Promise.reject(error);
  }
);

export default api;
