package com.voyagecraft.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeatherForecastResponse {
    private Long id;
    private Long tripId;
    private String locationName;
    private LocalDate forecastDate;
    private String condition;
    private String conditionIcon;
    private Double temperatureHigh;
    private Double temperatureLow;
    private Integer humidityPercent;
    private Integer precipitationChance;
    private Double windSpeedKmh;
    private String description;
    private String recommendation;
    private Boolean isAlert;
    private String alertMessage;
}
