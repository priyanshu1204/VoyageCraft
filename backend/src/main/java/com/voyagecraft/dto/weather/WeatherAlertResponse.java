package com.voyagecraft.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeatherAlertResponse {
    private String locationName;
    private String forecastDate;
    private String condition;
    private String alertMessage;
    private String severity; // LOW, MEDIUM, HIGH
    private String suggestedSwap; // Alternative activity suggestion
}
