export type ActivityCategory = 'ATTRACTION' | 'FOOD' | 'SHOPPING' | 'NATURE' | 'ADVENTURE' | 'LEISURE' | 'CULTURAL' | 'NIGHTLIFE' | 'FAMILY' | 'WORKSHOP' | 'TRANSPORT';
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'WAITLISTED' | 'CANCELLED';

export interface ActivityRequest {
  tripId: number;
  name: string;
  description?: string;
  category: ActivityCategory;
  tags?: string;
  location?: string;
  address?: string;
  activityDate?: string;
  startTime?: string;
  endTime?: string;
  openingHours?: string;
  seasonalNotes?: string;
  cost?: number;
  currency?: string;
  reservationStatus?: ReservationStatus;
  bookingReference?: string;
  bookingUrl?: string;
  reminderAt?: string;
  notes?: string;
  alternatives?: string;
  priority?: number;
}

export interface ActivityResponse {
  id: number;
  tripId: number;
  name: string;
  description?: string;
  category: ActivityCategory;
  tags?: string;
  tagList: string[];
  location?: string;
  address?: string;
  activityDate?: string;
  startTime?: string;
  endTime?: string;
  openingHours?: string;
  seasonalNotes?: string;
  cost?: number;
  currency: string;
  reservationStatus: ReservationStatus;
  bookingReference?: string;
  bookingUrl?: string;
  reminderAt?: string;
  notes?: string;
  alternatives?: string;
  alternativeList: string[];
  priority: number;
  createdAt: string;
}
