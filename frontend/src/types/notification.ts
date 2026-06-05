export interface NotificationData {
  id: number;
  tripId: number | null;
  tripTitle: string | null;
  notificationType: string;
  severity: string;
  severityIcon: string;
  title: string;
  message: string;
  actionUrl: string;
  readStatus: boolean;
  dismissed: boolean;
  scheduledFor: string | null;
  createdAt: string;
}

export interface NotificationPreference {
  id: number;
  departureReminders: boolean;
  checkinReminders: boolean;
  collaboratorAlerts: boolean;
  budgetAlerts: boolean;
  weatherAlerts: boolean;
  quietHoursEnabled: boolean;
  quietHoursStart: string | null;
  quietHoursEnd: string | null;
  budgetThresholdPercent: number;
  departureReminderHours: number;
}
