export type PackingCategory = 'CLOTHING' | 'TOILETRIES' | 'ELECTRONICS' | 'DOCUMENTS' | 'HEALTH' | 'ACCESSORIES' | 'FOOTWEAR' | 'SNACKS' | 'MISC';
export type ClimateType = 'TROPICAL' | 'COLD' | 'TEMPERATE' | 'DESERT' | 'BEACH' | 'MOUNTAIN' | 'URBAN' | 'RAINY';
export type DocumentType = 'PASSPORT' | 'VISA' | 'TRAVEL_INSURANCE' | 'BOARDING_PASS' | 'HOTEL_BOOKING' | 'DRIVERS_LICENSE' | 'VACCINATION_CARD' | 'ID_CARD' | 'OTHER';

export interface PackingItem {
  id: number;
  tripId: number;
  name: string;
  category: PackingCategory;
  quantity: number;
  packed: boolean;
  notes?: string;
  isFromTemplate: boolean;
  createdAt: string;
}

export interface PackingSummary {
  tripId: number;
  totalItems: number;
  packedItems: number;
  packedPercent: number;
  categoryBreakdown: CategorySummary[];
}

export interface CategorySummary {
  category: PackingCategory;
  total: number;
  packed: number;
}

export interface PackingTemplateItem {
  name: string;
  category: PackingCategory;
  quantity: number;
}

export interface TravelDocument {
  id: number;
  tripId: number;
  userId: number;
  userName: string;
  documentType: DocumentType;
  title: string;
  documentNumber?: string;
  issuingCountry?: string;
  issueDate?: string;
  expiryDate?: string;
  fileUrl?: string;
  notes?: string;
  isExpired: boolean;
  isExpiringSoon: boolean;
  daysUntilExpiry?: number;
  createdAt: string;
}

export interface PackingItemRequest {
  tripId: number;
  name: string;
  category: PackingCategory;
  quantity?: number;
  packed?: boolean;
  notes?: string;
}

export interface TravelDocumentRequest {
  tripId: number;
  documentType: DocumentType;
  title: string;
  documentNumber?: string;
  issuingCountry?: string;
  issueDate?: string;
  expiryDate?: string;
  fileUrl?: string;
  notes?: string;
}
