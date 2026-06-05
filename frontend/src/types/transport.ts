export type TransportType = 'FLIGHT' | 'TRAIN' | 'BUS' | 'CAR_RENTAL';

export interface TransportRequest {
  tripId: number;
  type: TransportType;
  provider?: string;
  flightNumber?: string;
  departureLocation: string;
  arrivalLocation: string;
  departureTime: string;
  arrivalTime: string;
  departureTimezone?: string;
  arrivalTimezone?: string;
  bookingReference?: string;
  cost?: number;
  notes?: string;
}

export interface TransportResponse {
  id: number;
  tripId: number;
  type: TransportType;
  provider?: string;
  flightNumber?: string;
  departureLocation: string;
  arrivalLocation: string;
  departureTime: string;
  arrivalTime: string;
  departureTimezone: string;
  arrivalTimezone: string;
  bookingReference?: string;
  cost?: number;
  notes?: string;
  durationMinutes: number;
  departureTimeFormatted: string;
  arrivalTimeFormatted: string;
  createdAt: string;
}

export interface TransportConflictResponse {
  severity: 'WARNING' | 'ERROR';
  type: 'OVERLAP' | 'TIGHT_CONNECTION' | 'IMPOSSIBLE_CONNECTION';
  message: string;
  segmentAId: number;
  segmentALabel: string;
  segmentBId: number;
  segmentBLabel: string;
  layoverMinutes: number;
}

export interface MockTransportOption {
  type: TransportType;
  provider: string;
  flightNumber?: string;
  departureLocation: string;
  arrivalLocation: string;
  departureTime: string;
  arrivalTime: string;
  departureTimezone: string;
  arrivalTimezone: string;
  durationMinutes: number;
  cost: number;
  stops: number;
}
