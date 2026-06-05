export interface ScheduleItem {
  id: number;
  title: string;
  location: string | null;
  startTime: string | null;
  endTime: string | null;
  orderIndex: number | null;
  category: string | null;
  type: 'ITINERARY_ITEM' | 'ACTIVITY' | 'TRANSPORT';
}

export interface CheckInItem {
  id: number;
  type: 'FLIGHT' | 'STAY';
  title: string;
  subtitle: string;
  bookingReference: string | null;
  dateTime: string;
  checkedIn: boolean;
}

export interface QuickNote {
  id: number;
  tripId: number;
  title: string;
  content: string | null;
  photoUrls: string | null;
  capturedLatitude: string | null;
  capturedLongitude: string | null;
  capturedLocationName: string | null;
  isSynced: boolean;
  isPinned: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface QuickActionDashboard {
  tripId: number;
  tripTitle: string;
  todaySchedule: ScheduleItem[];
  upcomingCheckIns: CheckInItem[];
  recentNotes: QuickNote[];
  unreadNotifications: number;
}

export interface QuickNoteRequest {
  tripId: number;
  title: string;
  content?: string;
  photoUrls?: string;
  capturedLatitude?: string;
  capturedLongitude?: string;
  capturedLocationName?: string;
  isSynced?: boolean;
  isPinned?: boolean;
}

export interface ReorderRequest {
  dayId: number;
  itemIds: number[];
}
