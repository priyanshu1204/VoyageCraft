export interface WeatherForecast {
  id: number;
  tripId: number;
  locationName: string;
  forecastDate: string;
  condition: string;
  conditionIcon: string;
  temperatureHigh: number;
  temperatureLow: number;
  humidityPercent: number;
  precipitationChance: number;
  windSpeedKmh: number;
  description: string;
  recommendation: string;
  isAlert: boolean;
  alertMessage: string | null;
}

export interface WeatherAlert {
  locationName: string;
  forecastDate: string;
  condition: string;
  alertMessage: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  suggestedSwap: string;
}

export interface SeasonalAdvice {
  locationName: string;
  season: string;
  temperatureRange: string;
  bestTimeToVisit: string;
  advice: string;
  recommendedActivities: string[];
  itemsToPack: string[];
}
