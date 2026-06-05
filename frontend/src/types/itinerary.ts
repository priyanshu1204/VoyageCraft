export type ItineraryStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';
export type ActivityCategory =
  | 'ATTRACTION'
  | 'FOOD'
  | 'SHOPPING'
  | 'NATURE'
  | 'ADVENTURE'
  | 'LEISURE'
  | 'CULTURAL'
  | 'NIGHTLIFE'
  | 'TRANSPORT';

export interface GenerateItineraryRequest {
  tripId: number;
  pace: 'RELAXED' | 'STANDARD' | 'INTENSE';
  versionName?: string;
  preferredCategories?: string[];
}

export interface ItineraryItemResponse {
  id: number;
  title: string;
  description?: string;
  startTime?: string;
  endTime?: string;
  locationName?: string;
  locationAddress?: string;
  latitude?: number;
  longitude?: number;
  costEstimate?: number;
  category: ActivityCategory;
  orderIndex: number;
  openingHours?: string;
  bestSeason?: string;
  durationMinutes?: number;
}

export interface ItineraryDayResponse {
  id: number;
  dayIndex: number;
  dayDate: string;
  theme?: string;
  notes?: string;
  destinationName?: string;
  items: ItineraryItemResponse[];
}

export interface ItineraryResponse {
  id: number;
  tripId: number;
  tripTitle: string;
  versionName: string;
  versionNumber: number;
  status: ItineraryStatus;
  pace: 'RELAXED' | 'STANDARD' | 'INTENSE';
  isActive: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  days: ItineraryDayResponse[];
  totalDays: number;
  totalActivities: number;
}

export interface ItinerarySummaryResponse {
  id: number;
  versionName: string;
  versionNumber: number;
  status: ItineraryStatus;
  pace: 'RELAXED' | 'STANDARD' | 'INTENSE';
  isActive: boolean;
  totalDays: number;
  totalActivities: number;
  createdAt: string;
}

export interface ItineraryCompareResponse {
  versionA: ItinerarySummaryResponse;
  versionB: ItinerarySummaryResponse;
  daysA: ItineraryDayResponse[];
  daysB: ItineraryDayResponse[];
  paceComparison: string;
  activityDifference: number;
}
