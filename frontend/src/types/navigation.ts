export interface NavigationRoute {
  id: number;
  tripId: number;
  fromLocation: string;
  fromLatitude: number | null;
  fromLongitude: number | null;
  toLocation: string;
  toLatitude: number | null;
  toLongitude: number | null;
  transportMode: string;
  transportModeIcon: string;
  distanceKm: number;
  estimatedMinutes: number;
  bufferMinutes: number;
  totalMinutes: number;
  directions: string;
  dayNumber: number;
  orderIndex: number;
  formattedTime: string;
}

export interface NavigationRouteRequest {
  fromLocation: string;
  fromLatitude?: number | null;
  fromLongitude?: number | null;
  toLocation: string;
  toLatitude?: number | null;
  toLongitude?: number | null;
  transportMode: string;
  bufferMinutes?: number;
  directions?: string;
  dayNumber: number;
  orderIndex?: number;
}

export interface DaySheet {
  dayNumber: number;
  date: string;
  destination: string;
  routes: NavigationRoute[];
  totalTravelMinutes: number;
  totalDistanceKm: number;
  summary: string;
}
