export type StayType = 'HOTEL' | 'HOSTEL' | 'HOMESTAY' | 'AIRBNB' | 'RESORT' | 'APARTMENT' | 'OTHER';

export interface StayRequest {
  tripId: number;
  type: StayType;
  name: string;
  address?: string;
  city?: string;
  checkInDate: string;
  checkInTime?: string;
  checkOutDate: string;
  checkOutTime?: string;
  costPerNight?: number;
  totalCost?: number;
  currency?: string;
  bookingReference?: string;
  bookingUrl?: string;
  contactPhone?: string;
  contactEmail?: string;
  notes?: string;
  cancellationPolicy?: string;
  amenities?: string;
  starRating?: number;
}

export interface StayResponse {
  id: number;
  tripId: number;
  type: StayType;
  name: string;
  address?: string;
  city?: string;
  checkInDate: string;
  checkInTime?: string;
  checkOutDate: string;
  checkOutTime?: string;
  costPerNight?: number;
  totalCost?: number;
  currency: string;
  bookingReference?: string;
  bookingUrl?: string;
  contactPhone?: string;
  contactEmail?: string;
  notes?: string;
  cancellationPolicy?: string;
  amenities?: string;
  starRating: number;
  totalNights: number;
  createdAt: string;
}
