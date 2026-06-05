export interface TravelDocumentData {
  id: number;
  tripId: number;
  countryName: string;
  countryCode: string;
  visaRequirements: string;
  entryGuidelines: string;
  officialLink: string;
  additionalNotes: string;
  checklistItems: ChecklistItem[];
  reminders: Reminder[];
  createdAt: string;
  updatedAt: string;
}

export interface ChecklistItem {
  id: number;
  itemName: string;
  itemType: string;
  description: string;
  documentLink: string;
  completed: boolean;
}

export interface Reminder {
  id: number;
  title: string;
  note: string;
  reminderDate: string;
  dismissed: boolean;
}

export interface TravelDocumentRequest {
  countryName: string;
  countryCode: string;
  visaRequirements: string;
  entryGuidelines: string;
  officialLink: string;
  additionalNotes: string;
  checklistItems?: { itemName: string; itemType: string; description?: string; documentLink?: string; completed?: boolean }[];
  reminders?: { title: string; note?: string; reminderDate: string }[];
}

export interface CountryLibraryEntry {
  countryName: string;
  countryCode: string;
  visaRequirements: string;
  entryGuidelines: string;
  officialLink: string;
  suggestedVaccines: string[];
  suggestedDocuments: string[];
}
